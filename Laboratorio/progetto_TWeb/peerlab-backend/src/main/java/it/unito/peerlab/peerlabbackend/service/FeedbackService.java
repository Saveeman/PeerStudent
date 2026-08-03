package it.unito.peerlab.peerlabbackend.service;

import it.unito.peerlab.peerlabbackend.dto.FeedbackDTO;
import it.unito.peerlab.peerlabbackend.dto.NuovoFeedbackRequest;
import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import it.unito.peerlab.peerlabbackend.exception.OperazioneNonValidaException;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import it.unito.peerlab.peerlabbackend.mapper.DTOMapper;
import it.unito.peerlab.peerlabbackend.model.*;
import it.unito.peerlab.peerlabbackend.repository.FeedbackRepository;
import it.unito.peerlab.peerlabbackend.repository.PrenotazioneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business logic delle valutazioni lasciate dopo una sessione.
 *
 * Il Feedback e' collegato alla Prenotazione (non direttamente allo studente):
 * questo rende strutturalmente impossibile valutare una sessione a cui non si
 * e' partecipato, perche' senza prenotazione accettata non c'e' nulla da
 * valutare.
 */
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final DTOMapper mapper;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           PrenotazioneRepository prenotazioneRepository,
                           DTOMapper mapper) {
        this.feedbackRepository = feedbackRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.mapper = mapper;
    }

    /**
     * Crea una valutazione. Regole:
     *  - solo lo studente titolare della prenotazione
     *  - la prenotazione deve essere ACCETTATA
     *  - la sessione deve essere COMPLETATA
     *  - una sola valutazione per prenotazione
     *  - voto compreso fra 1 e 5
     */
    @Transactional
    public FeedbackDTO creaFeedback(NuovoFeedbackRequest richiesta, Long studenteId) {
        Prenotazione prenotazione = prenotazioneRepository.findById(richiesta.prenotazioneId())
                .orElseThrow(() -> new RisorsaNonTrovataException(
                        "Prenotazione non trovata: " + richiesta.prenotazioneId()));

        if (!prenotazione.getStudente().getId().equals(studenteId)) {
            throw new NonAutorizzatoException("Puoi valutare solo le sessioni a cui hai partecipato");
        }
        if (prenotazione.getStato() != StatoPrenotazione.ACCETTATA) {
            throw new OperazioneNonValidaException(
                    "Puoi valutare solo le sessioni a cui sei stato ammesso");
        }
        if (prenotazione.getSessione().getStato() != StatoSessione.COMPLETATA) {
            throw new OperazioneNonValidaException("La sessione non si e' ancora svolta");
        }
        if (feedbackRepository.existsByPrenotazioneId(prenotazione.getId())) {
            throw new OperazioneNonValidaException("Hai gia' valutato questa sessione");
        }
        if (richiesta.voto() == null || richiesta.voto() < 1 || richiesta.voto() > 5) {
            throw new OperazioneNonValidaException("Il voto deve essere compreso fra 1 e 5");
        }

        Feedback feedback = new Feedback(
                richiesta.voto(),
                richiesta.commento(),
                LocalDateTime.now(),
                prenotazione
        );

        return mapper.toFeedbackDTO(feedbackRepository.save(feedback));
    }

    /** Tutte le valutazioni ricevute da un tutor: costituiscono la sua reputazione. */
    @Transactional(readOnly = true)
    public List<FeedbackDTO> getFeedbackDelTutor(Long tutorId) {
        return feedbackRepository.findByPrenotazioneSessioneTutorIdOrderByDataDesc(tutorId).stream()
                .map(mapper::toFeedbackDTO)
                .toList();
    }

    /** Media dei voti di un tutor, arrotondata a una cifra decimale. 0 se non ha valutazioni. */
    @Transactional(readOnly = true)
    public double getMediaVotiTutor(Long tutorId) {
        List<Feedback> valutazioni =
                feedbackRepository.findByPrenotazioneSessioneTutorIdOrderByDataDesc(tutorId);
        if (valutazioni.isEmpty()) {
            return 0.0;
        }
        double media = valutazioni.stream()
                .mapToInt(Feedback::getVoto)
                .average()
                .orElse(0.0);
        return Math.round(media * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTO> getFeedbackDiSessione(Long sessioneId) {
        return feedbackRepository.findByPrenotazioneSessioneIdOrderByDataDesc(sessioneId).stream()
                .map(mapper::toFeedbackDTO)
                .toList();
    }
}
