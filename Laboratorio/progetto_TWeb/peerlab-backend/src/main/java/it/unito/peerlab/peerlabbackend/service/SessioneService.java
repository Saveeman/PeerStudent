package it.unito.peerlab.peerlabbackend.service;

import it.unito.peerlab.peerlabbackend.dto.*;
import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import it.unito.peerlab.peerlabbackend.exception.OperazioneNonValidaException;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import it.unito.peerlab.peerlabbackend.mapper.DTOMapper;
import it.unito.peerlab.peerlabbackend.model.*;
import it.unito.peerlab.peerlabbackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic delle sessioni di tutoraggio.
 *
 * Responsabilita' principali:
 *  - creazione di una sessione (solo da parte di un TUTOR)
 *  - ricerca e filtro delle sessioni
 *  - calcolo dei posti occupati/disponibili
 *  - transizioni di stato (chiusura, completamento, annullamento)
 *  - gestione dei materiali didattici allegati
 */
@Service
public class SessioneService {

    private final SessioneRepository sessioneRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final MateriaRepository materiaRepository;
    private final ArgomentoRepository argomentoRepository;
    private final MaterialeDidatticoRepository materialeRepository;
    private final UtenteRepository utenteRepository;
    private final DTOMapper mapper;

    public SessioneService(SessioneRepository sessioneRepository,
                           PrenotazioneRepository prenotazioneRepository,
                           MateriaRepository materiaRepository,
                           ArgomentoRepository argomentoRepository,
                           MaterialeDidatticoRepository materialeRepository,
                           UtenteRepository utenteRepository,
                           DTOMapper mapper) {
        this.sessioneRepository = sessioneRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.materiaRepository = materiaRepository;
        this.argomentoRepository = argomentoRepository;
        this.materialeRepository = materialeRepository;
        this.utenteRepository = utenteRepository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- LETTURA

    /** Tutte le sessioni aperte, ordinate per data. E' la vista principale dello studente. */
    @Transactional(readOnly = true)
    public List<SessioneDTO> getSessioniAperte(Long richiedenteId) {
        return sessioneRepository.findByStatoOrderByDataOraAsc(StatoSessione.APERTA).stream()
                .map(sessione -> convertiConPosti(sessione, richiedenteId))
                .toList();
    }

    /** Sessioni aperte di una specifica materia: alimenta il filtro per materia. */
    @Transactional(readOnly = true)
    public List<SessioneDTO> getSessioniPerMateria(Long materiaId, Long richiedenteId) {
        if (!materiaRepository.existsById(materiaId)) {
            throw new RisorsaNonTrovataException("Materia non trovata: " + materiaId);
        }
        return sessioneRepository
                .findByMateriaIdAndStatoOrderByDataOraAsc(materiaId, StatoSessione.APERTA).stream()
                .map(sessione -> convertiConPosti(sessione, richiedenteId))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessioneDTO getById(Long id, Long richiedenteId) {
        return convertiConPosti(getEntityById(id), richiedenteId);
    }

    /** Tutte le sessioni create da un tutor: e' la sua area personale. */
    @Transactional(readOnly = true)
    public List<SessioneDTO> getSessioniDelTutor(Long tutorId, Long richiedenteId) {
        if (!utenteRepository.existsById(tutorId)) {
            throw new RisorsaNonTrovataException("Utente non trovato: " + tutorId);
        }
        return sessioneRepository.findByTutorIdOrderByDataOraDesc(tutorId).stream()
                .map(sessione -> convertiConPosti(sessione, richiedenteId))
                .toList();
    }

    /** Ricerca testuale per titolo. */
    @Transactional(readOnly = true)
    public List<SessioneDTO> cercaPerTitolo(String testo, Long richiedenteId) {
        return sessioneRepository.findByTitoloContainsIgnoreCaseOrderByDataOraAsc(testo).stream()
                .map(sessione -> convertiConPosti(sessione, richiedenteId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialeDidatticoDTO> getMaterialiDiSessione(Long sessioneId) {
        if (!sessioneRepository.existsById(sessioneId)) {
            throw new RisorsaNonTrovataException("Sessione non trovata: " + sessioneId);
        }
        return materialeRepository.findBySessioneIdOrderByTitoloAsc(sessioneId).stream()
                .map(mapper::toMaterialeDTO)
                .toList();
    }

    // -------------------------------------------------------------- SCRITTURA

    /**
     * Crea una nuova sessione. Regole applicate:
     *  - l'autore deve avere ruolo TUTOR
     *  - la data deve essere futura
     *  - i posti totali devono essere almeno 1
     */
    @Transactional
    public SessioneDTO creaSessione(NuovaSessioneRequest richiesta, Long tutorId) {
        Utente tutor = utenteRepository.findById(tutorId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato: " + tutorId));

        if (tutor.getRuolo() != Ruolo.TUTOR) {
            throw new NonAutorizzatoException("Solo un tutor puo' creare una sessione");
        }
        if (richiesta.dataOra() == null || richiesta.dataOra().isBefore(LocalDateTime.now())) {
            throw new OperazioneNonValidaException("La data della sessione deve essere futura");
        }
        if (richiesta.postiTotali() == null || richiesta.postiTotali() < 1) {
            throw new OperazioneNonValidaException("La sessione deve prevedere almeno un posto");
        }

        if (richiesta.modalita() == ModalitaSessione.ONLINE
                && (richiesta.linkIncontro() == null
                    || richiesta.linkIncontro().trim().isEmpty())) {
            throw new OperazioneNonValidaException(
                    "Per un appuntamento online devi indicare il link della videochiamata");
        }

        Materia materia = materiaRepository.findById(richiesta.materiaId())
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Materia non trovata: " + richiesta.materiaId()));

        Sessione sessione = new Sessione(
                richiesta.titolo(),
                richiesta.descrizione(),
                richiesta.dataOra(),
                richiesta.luogo(),
                richiesta.modalita(),
                richiesta.postiTotali(),
                StatoSessione.APERTA,
                tutor,
                materia
        );

        if (richiesta.modalita() == ModalitaSessione.ONLINE) {
            sessione.setLinkIncontro(richiesta.linkIncontro().trim());
        }

        if (richiesta.argomentiIds() != null && !richiesta.argomentiIds().isEmpty()) {
            Set<Argomento> argomenti = new HashSet<>(
                    argomentoRepository.findAllById(richiesta.argomentiIds()));
            sessione.setArgomenti(argomenti);
        }

        Sessione salvata = sessioneRepository.save(sessione);
        return convertiConPosti(salvata, tutorId);
    }

    /** Chiude le iscrizioni: la sessione resta valida ma non accetta nuove richieste. */
    @Transactional
    public SessioneDTO chiudiIscrizioni(Long sessioneId, Long tutorId) {
        Sessione sessione = getEntityVerificandoProprietario(sessioneId, tutorId);
        if (sessione.getStato() != StatoSessione.APERTA) {
            throw new OperazioneNonValidaException("La sessione non e' aperta");
        }
        sessione.setStato(StatoSessione.CHIUSA);
        return convertiConPosti(sessione, tutorId);
    }

    /** Segna la sessione come svolta: da qui in poi gli studenti possono lasciare feedback. */
    @Transactional
    public SessioneDTO completaSessione(Long sessioneId, Long tutorId) {
        Sessione sessione = getEntityVerificandoProprietario(sessioneId, tutorId);
        if (sessione.getStato() == StatoSessione.ANNULLATA) {
            throw new OperazioneNonValidaException("Una sessione annullata non puo' essere completata");
        }
        sessione.setStato(StatoSessione.COMPLETATA);
        return convertiConPosti(sessione, tutorId);
    }

    @Transactional
    public SessioneDTO annullaSessione(Long sessioneId, Long tutorId) {
        Sessione sessione = getEntityVerificandoProprietario(sessioneId, tutorId);
        if (sessione.getStato() == StatoSessione.COMPLETATA) {
            throw new OperazioneNonValidaException("Una sessione completata non puo' essere annullata");
        }
        sessione.setStato(StatoSessione.ANNULLATA);
        return convertiConPosti(sessione, tutorId);
    }

    @Transactional
    public MaterialeDidatticoDTO aggiungiMateriale(Long sessioneId,
                                                   NuovoMaterialeRequest richiesta,
                                                   Long tutorId) {
        Sessione sessione = getEntityVerificandoProprietario(sessioneId, tutorId);
        MaterialeDidattico materiale = new MaterialeDidattico(
                richiesta.titolo(), richiesta.url(), richiesta.tipo(), sessione);
        return mapper.toMaterialeDTO(materialeRepository.save(materiale));
    }

    // ---------------------------------------------------------------- SUPPORTO

    @Transactional(readOnly = true)
    public Sessione getEntityById(Long id) {
        return sessioneRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Sessione non trovata: " + id));
    }

    /** Verifica che chi effettua l'operazione sia il tutor proprietario della sessione. */
    private Sessione getEntityVerificandoProprietario(Long sessioneId, Long tutorId) {
        Sessione sessione = getEntityById(sessioneId);
        if (!sessione.getTutor().getId().equals(tutorId)) {
            throw new NonAutorizzatoException("Solo il tutor che ha creato la sessione puo' modificarla");
        }
        return sessione;
    }

    /**
     * Converte una Sessione nel suo DTO calcolando i posti occupati.
     * Il conteggio non e' memorizzato nell'entita': si ottiene contando le
     * prenotazioni in stato ACCETTATA.
     */
    private SessioneDTO convertiConPosti(Sessione sessione, Long richiedenteId) {
        long occupati = prenotazioneRepository
                .countBySessioneIdAndStato(sessione.getId(), StatoPrenotazione.ACCETTATA);

        /* Il link della videochiamata e' riservato: lo vede il tutor che ha
           creato l'appuntamento e chi e' stato ammesso a parteciparvi. */
        boolean eIlTutor = richiedenteId != null
                && sessione.getTutor().getId().equals(richiedenteId);
        boolean eAmmesso = richiedenteId != null
                && prenotazioneRepository.existsByStudenteIdAndSessioneIdAndStato(
                        richiedenteId, sessione.getId(), StatoPrenotazione.ACCETTATA);

        return mapper.toSessioneDTO(sessione, occupati, eIlTutor || eAmmesso);
    }
}
