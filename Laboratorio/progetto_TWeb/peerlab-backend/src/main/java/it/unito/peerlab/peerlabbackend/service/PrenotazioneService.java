package it.unito.peerlab.peerlabbackend.service;

import it.unito.peerlab.peerlabbackend.dto.NuovaPrenotazioneRequest;
import it.unito.peerlab.peerlabbackend.dto.PrenotazioneDTO;
import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import it.unito.peerlab.peerlabbackend.exception.OperazioneNonValidaException;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import it.unito.peerlab.peerlabbackend.mapper.DTOMapper;
import it.unito.peerlab.peerlabbackend.model.*;
import it.unito.peerlab.peerlabbackend.repository.PrenotazioneRepository;
import it.unito.peerlab.peerlabbackend.repository.SessioneRepository;
import it.unito.peerlab.peerlabbackend.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic delle prenotazioni: e' il cuore dell'applicazione, perche'
 * qui vivono le regole che governano il flusso richiesta -> accettazione.
 *
 * Regole implementate:
 *  - non ci si puo' prenotare due volte alla stessa sessione
 *  - non ci si puo' prenotare a una propria sessione
 *  - la sessione deve essere APERTA
 *  - solo il tutor proprietario puo' accettare o rifiutare
 *  - non si puo' accettare oltre il numero di posti disponibili
 *  - solo lo studente che ha prenotato puo' ritirare la propria richiesta
 */
@Service
public class PrenotazioneService {

    private final PrenotazioneRepository prenotazioneRepository;
    private final SessioneRepository sessioneRepository;
    private final UtenteRepository utenteRepository;
    private final DTOMapper mapper;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository,
                               SessioneRepository sessioneRepository,
                               UtenteRepository utenteRepository,
                               DTOMapper mapper) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.sessioneRepository = sessioneRepository;
        this.utenteRepository = utenteRepository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- LETTURA

    /** Le prenotazioni di uno studente: la sua area "Le mie prenotazioni". */
    @Transactional(readOnly = true)
    public List<PrenotazioneDTO> getPrenotazioniStudente(Long studenteId) {
        if (!utenteRepository.existsById(studenteId)) {
            throw new RisorsaNonTrovataException("Utente non trovato: " + studenteId);
        }
        return prenotazioneRepository.findByStudenteIdOrderByDataRichiestaDesc(studenteId).stream()
                .map(mapper::toPrenotazioneDTO)
                .toList();
    }

    /** Le richieste ricevute su una specifica sessione: visibili solo al tutor proprietario. */
    @Transactional(readOnly = true)
    public List<PrenotazioneDTO> getPrenotazioniDiSessione(Long sessioneId, Long tutorId) {
        Sessione sessione = sessioneRepository.findById(sessioneId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Sessione non trovata: " + sessioneId));
        if (!sessione.getTutor().getId().equals(tutorId)) {
            throw new NonAutorizzatoException("Non sei il tutor di questa sessione");
        }
        return prenotazioneRepository.findBySessioneIdOrderByDataRichiestaAsc(sessioneId).stream()
                .map(mapper::toPrenotazioneDTO)
                .toList();
    }

    /**
     * Tutte le richieste ancora in attesa rivolte a un tutor, su qualunque sua
     * sessione: alimenta il pannello "richieste da gestire".
     */
    @Transactional(readOnly = true)
    public List<PrenotazioneDTO> getRichiesteInAttesa(Long tutorId) {
        return prenotazioneRepository
                .findBySessioneTutorIdAndStatoOrderByDataRichiestaAsc(tutorId, StatoPrenotazione.IN_ATTESA)
                .stream()
                .map(mapper::toPrenotazioneDTO)
                .toList();
    }

    // -------------------------------------------------------------- SCRITTURA

    /** Crea una richiesta di partecipazione, in stato IN_ATTESA. */
    @Transactional
    public PrenotazioneDTO creaPrenotazione(NuovaPrenotazioneRequest richiesta, Long studenteId) {
        Utente studente = utenteRepository.findById(studenteId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato: " + studenteId));

        Sessione sessione = sessioneRepository.findById(richiesta.sessioneId())
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Sessione non trovata: " + richiesta.sessioneId()));

        if (sessione.getStato() != StatoSessione.APERTA) {
            throw new OperazioneNonValidaException("Questa sessione non accetta prenotazioni");
        }
        if (sessione.getTutor().getId().equals(studenteId)) {
            throw new OperazioneNonValidaException("Non puoi prenotarti a una tua sessione");
        }
        /* Se esiste gia' una richiesta di questo studente per questa sessione, si
           distinguono due casi: se e' stata RITIRATA lo studente ha il diritto di
           ripensarci, altrimenti la richiesta e' un duplicato. */
        Prenotazione precedente = prenotazioneRepository
                .findByStudenteIdAndSessioneId(studenteId, sessione.getId())
                .orElse(null);

        if (precedente != null && precedente.getStato() != StatoPrenotazione.RITIRATA) {
            throw new OperazioneNonValidaException(
                    "Hai gia' inviato una richiesta per questo appuntamento");
        }

        if (postiDisponibili(sessione) <= 0) {
            throw new OperazioneNonValidaException("La sessione ha esaurito i posti disponibili");
        }

        // La motivazione e' sempre obbligatoria: serve al tutor per decidere
        // se accettare la richiesta.
        if (richiesta.messaggio() == null || richiesta.messaggio().trim().isEmpty()) {
            throw new OperazioneNonValidaException(
                    "Devi motivare la tua richiesta di partecipazione");
        }

        // Per le sessioni online serve anche l'email istituzionale, perche' e'
        // l'indirizzo a cui verra' inviato il link della videochiamata.
        String email = richiesta.emailContatto() == null
                ? null : richiesta.emailContatto().trim();

        if (sessione.getModalita() == ModalitaSessione.ONLINE) {
            if (email == null || email.isEmpty()) {
                throw new OperazioneNonValidaException(
                        "Per le sessioni online devi indicare la tua email istituzionale");
            }
            if (!emailIstituzionale(email)) {
                throw new OperazioneNonValidaException(
                        "Inserisci un'email istituzionale (@edu.unito.it o @unito.it)");
            }
        } else {
            // per le sessioni in presenza l'email non serve
            email = null;
        }

        /* Richiesta ritirata in precedenza: la si riporta in attesa aggiornando
           motivazione, email e data, invece di creare una seconda riga per la
           stessa coppia studente-sessione. Essendo il metodo @Transactional,
           l'entita' e' managed e le modifiche vengono scritte al commit. */
        if (precedente != null) {
            precedente.setStato(StatoPrenotazione.IN_ATTESA);
            precedente.setDataRichiesta(LocalDateTime.now());
            precedente.setMessaggio(richiesta.messaggio().trim());
            precedente.setEmailContatto(email);
            return mapper.toPrenotazioneDTO(precedente);
        }

        Prenotazione prenotazione = new Prenotazione(
                StatoPrenotazione.IN_ATTESA,
                LocalDateTime.now(),
                richiesta.messaggio().trim(),
                email,
                studente,
                sessione
        );

        return mapper.toPrenotazioneDTO(prenotazioneRepository.save(prenotazione));
    }

    /**
     * Il tutor accetta una richiesta. Se con questa accettazione i posti si
     * esauriscono, la sessione passa automaticamente a CHIUSA.
     */
    @Transactional
    public PrenotazioneDTO accettaPrenotazione(Long prenotazioneId, Long tutorId) {
        Prenotazione prenotazione = getEntityVerificandoTutor(prenotazioneId, tutorId);

        if (prenotazione.getStato() != StatoPrenotazione.IN_ATTESA) {
            throw new OperazioneNonValidaException("Questa richiesta e' gia' stata gestita");
        }

        Sessione sessione = prenotazione.getSessione();
        if (postiDisponibili(sessione) <= 0) {
            throw new OperazioneNonValidaException("Non ci sono piu' posti disponibili");
        }

        prenotazione.setStato(StatoPrenotazione.ACCETTATA);

        if (postiDisponibili(sessione) <= 0) {
            sessione.setStato(StatoSessione.CHIUSA);
        }

        return mapper.toPrenotazioneDTO(prenotazione);
    }

    @Transactional
    public PrenotazioneDTO rifiutaPrenotazione(Long prenotazioneId, Long tutorId) {
        Prenotazione prenotazione = getEntityVerificandoTutor(prenotazioneId, tutorId);
        if (prenotazione.getStato() != StatoPrenotazione.IN_ATTESA) {
            throw new OperazioneNonValidaException("Questa richiesta e' gia' stata gestita");
        }
        prenotazione.setStato(StatoPrenotazione.RIFIUTATA);
        return mapper.toPrenotazioneDTO(prenotazione);
    }

    /** Lo studente ritira la propria richiesta. */
    @Transactional
    public PrenotazioneDTO ritiraPrenotazione(Long prenotazioneId, Long studenteId) {
        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Prenotazione non trovata: " + prenotazioneId));

        if (!prenotazione.getStudente().getId().equals(studenteId)) {
            throw new NonAutorizzatoException("Puoi ritirare solo le tue prenotazioni");
        }
        if (prenotazione.getStato() == StatoPrenotazione.RITIRATA) {
            throw new OperazioneNonValidaException("Prenotazione gia' ritirata");
        }

        prenotazione.setStato(StatoPrenotazione.RITIRATA);
        return mapper.toPrenotazioneDTO(prenotazione);
    }

    // ---------------------------------------------------------------- SUPPORTO

    private Prenotazione getEntityVerificandoTutor(Long prenotazioneId, Long tutorId) {
        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Prenotazione non trovata: " + prenotazioneId));
        if (!prenotazione.getSessione().getTutor().getId().equals(tutorId)) {
            throw new NonAutorizzatoException("Non sei il tutor di questa sessione");
        }
        return prenotazione;
    }

    /** Verifica che l'indirizzo appartenga a un dominio dell'ateneo. */
    private boolean emailIstituzionale(String email) {
        String e = email.toLowerCase();
        return e.endsWith("@edu.unito.it") || e.endsWith("@unito.it");
    }

    private long postiDisponibili(Sessione sessione) {
        long occupati = prenotazioneRepository
                .countBySessioneIdAndStato(sessione.getId(), StatoPrenotazione.ACCETTATA);
        return sessione.getPostiTotali() - occupati;
    }
}
