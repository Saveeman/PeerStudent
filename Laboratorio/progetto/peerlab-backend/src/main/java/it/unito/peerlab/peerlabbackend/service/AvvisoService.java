package it.unito.peerlab.peerlabbackend.service;

import it.unito.peerlab.peerlabbackend.dto.AvvisoDTO;
import it.unito.peerlab.peerlabbackend.dto.NuovoAvvisoRequest;
import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import it.unito.peerlab.peerlabbackend.exception.OperazioneNonValidaException;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import it.unito.peerlab.peerlabbackend.mapper.DTOMapper;
import it.unito.peerlab.peerlabbackend.model.Avviso;
import it.unito.peerlab.peerlabbackend.model.Prenotazione;
import it.unito.peerlab.peerlabbackend.model.Sessione;
import it.unito.peerlab.peerlabbackend.model.StatoPrenotazione;
import it.unito.peerlab.peerlabbackend.repository.AvvisoRepository;
import it.unito.peerlab.peerlabbackend.repository.PrenotazioneRepository;
import it.unito.peerlab.peerlabbackend.repository.SessioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic degli avvisi: le comunicazioni che il tutor invia a chi
 * partecipa a un suo appuntamento.
 *
 * Il destinatario non e' memorizzato: l'avviso appartiene alla sessione, e chi
 * lo riceve viene calcolato al momento della lettura a partire dalle
 * prenotazioni accettate. E' la stessa logica con cui il link della
 * videochiamata viene mostrato solo a chi ne ha diritto.
 */
@Service
public class AvvisoService {

    private final AvvisoRepository avvisoRepository;
    private final SessioneRepository sessioneRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final DTOMapper mapper;

    public AvvisoService(AvvisoRepository avvisoRepository,
                         SessioneRepository sessioneRepository,
                         PrenotazioneRepository prenotazioneRepository,
                         DTOMapper mapper) {
        this.avvisoRepository = avvisoRepository;
        this.sessioneRepository = sessioneRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.mapper = mapper;
    }

    /**
     * Il tutor invia un avviso ai partecipanti di un suo appuntamento.
     * Solo il tutor proprietario puo' farlo.
     */
    @Transactional
    public AvvisoDTO creaAvviso(NuovoAvvisoRequest richiesta, Long tutorId) {
        Sessione sessione = sessioneRepository.findById(richiesta.sessioneId())
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Appuntamento non trovato: " + richiesta.sessioneId()));

        if (!sessione.getTutor().getId().equals(tutorId)) {
            throw new NonAutorizzatoException(
                    "Puoi inviare avvisi solo per i tuoi appuntamenti");
        }
        if (richiesta.testo() == null || richiesta.testo().trim().isEmpty()) {
            throw new OperazioneNonValidaException("L'avviso non puo' essere vuoto");
        }

        Avviso avviso = new Avviso(
                richiesta.testo().trim(), LocalDateTime.now(), sessione);

        return mapper.toAvvisoDTO(avvisoRepository.save(avviso));
    }

    /**
     * Gli avvisi destinati allo studente: quelli degli appuntamenti a cui la
     * sua prenotazione e' stata accettata.
     */
    @Transactional(readOnly = true)
    public List<AvvisoDTO> getAvvisiPerStudente(Long studenteId) {
        // appuntamenti a cui lo studente e' stato ammesso
        List<Long> sessioniAmmesse = prenotazioneRepository
                .findByStudenteIdAndStatoOrderByDataRichiestaDesc(
                        studenteId, StatoPrenotazione.ACCETTATA)
                .stream()
                .map(Prenotazione::getSessione)
                .map(Sessione::getId)
                .toList();

        if (sessioniAmmesse.isEmpty()) {
            return List.of();
        }

        return avvisoRepository.findBySessioneIdInOrderByDataDesc(sessioniAmmesse).stream()
                .map(mapper::toAvvisoDTO)
                .toList();
    }

    /** Avvisi inviati su un singolo appuntamento: visibili al tutor proprietario. */
    @Transactional(readOnly = true)
    public List<AvvisoDTO> getAvvisiDiSessione(Long sessioneId, Long tutorId) {
        Sessione sessione = sessioneRepository.findById(sessioneId)
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Appuntamento non trovato: " + sessioneId));

        if (!sessione.getTutor().getId().equals(tutorId)) {
            throw new NonAutorizzatoException("Non sei il tutor di questo appuntamento");
        }

        return avvisoRepository.findBySessioneIdOrderByDataDesc(sessioneId).stream()
                .map(mapper::toAvvisoDTO)
                .toList();
    }
}
