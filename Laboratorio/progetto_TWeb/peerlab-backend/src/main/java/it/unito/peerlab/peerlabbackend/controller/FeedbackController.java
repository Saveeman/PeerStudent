package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.dto.FeedbackDTO;
import it.unito.peerlab.peerlabbackend.dto.NuovoFeedbackRequest;
import it.unito.peerlab.peerlabbackend.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route relative alle valutazioni lasciate dopo una sessione.
 */
@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /** POST /api/feedback -- lo studente valuta una sessione a cui ha partecipato. */
    @PostMapping
    public ResponseEntity<FeedbackDTO> creaFeedback(@RequestBody NuovoFeedbackRequest richiesta,
                                                    HttpSession sessione) {
        Long studenteId = SessioneUtils.getUtenteId(sessione);
        FeedbackDTO creato = feedbackService.creaFeedback(richiesta, studenteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creato);
    }

    /** GET /api/feedback/tutor/{tutorId} -- le valutazioni ricevute da un tutor. */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<FeedbackDTO>> feedbackDelTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(feedbackService.getFeedbackDelTutor(tutorId));
    }

    /** GET /api/feedback/tutor/{tutorId}/media -- la media dei voti di un tutor. */
    @GetMapping("/tutor/{tutorId}/media")
    public ResponseEntity<Double> mediaVotiTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(feedbackService.getMediaVotiTutor(tutorId));
    }

    /** GET /api/feedback/sessione/{sessioneId} */
    @GetMapping("/sessione/{sessioneId}")
    public ResponseEntity<List<FeedbackDTO>> feedbackDiSessione(@PathVariable Long sessioneId) {
        return ResponseEntity.ok(feedbackService.getFeedbackDiSessione(sessioneId));
    }
}
