package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.dto.NuovaPrenotazioneRequest;
import it.unito.peerlab.peerlabbackend.dto.PrenotazioneDTO;
import it.unito.peerlab.peerlabbackend.service.PrenotazioneService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route relative alle prenotazioni.
 *
 * E' qui che si vede la differenza di permessi fra i due ruoli: le route
 * /richieste, /accetta e /rifiuta sono utilizzabili solo dal tutor
 * proprietario della sessione (il controllo e' nel service), mentre /mie e
 * /ritira riguardano lo studente.
 */
@RestController
@RequestMapping("/api/prenotazioni")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PrenotazioneController {

    private final PrenotazioneService prenotazioneService;

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        this.prenotazioneService = prenotazioneService;
    }

    /**
     * POST /api/prenotazioni
     * Lo studente richiede di partecipare a una sessione.
     */
    @PostMapping
    public ResponseEntity<PrenotazioneDTO> prenota(@RequestBody NuovaPrenotazioneRequest richiesta,
                                                   HttpSession sessione) {
        Long studenteId = SessioneUtils.getUtenteId(sessione);
        PrenotazioneDTO creata = prenotazioneService.creaPrenotazione(richiesta, studenteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creata);
    }

    /**
     * GET /api/prenotazioni/mie
     * Le prenotazioni dell'utente autenticato.
     */
    @GetMapping("/mie")
    public ResponseEntity<List<PrenotazioneDTO>> miePrenotazioni(HttpSession sessione) {
        Long studenteId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(prenotazioneService.getPrenotazioniStudente(studenteId));
    }

    /**
     * GET /api/prenotazioni/richieste
     * Le richieste in attesa rivolte al tutor autenticato, su tutte le sue
     * sessioni. Alimenta il pannello "richieste da gestire".
     */
    @GetMapping("/richieste")
    public ResponseEntity<List<PrenotazioneDTO>> richiesteInAttesa(HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(prenotazioneService.getRichiesteInAttesa(tutorId));
    }

    /**
     * GET /api/prenotazioni/sessione/{sessioneId}
     * Tutte le richieste ricevute su una specifica sessione.
     */
    @GetMapping("/sessione/{sessioneId}")
    public ResponseEntity<List<PrenotazioneDTO>> prenotazioniDiSessione(
            @PathVariable Long sessioneId,
            HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(
                prenotazioneService.getPrenotazioniDiSessione(sessioneId, tutorId));
    }

    /** POST /api/prenotazioni/{id}/accetta -- solo il tutor proprietario. */
    @PostMapping("/{id}/accetta")
    public ResponseEntity<PrenotazioneDTO> accetta(@PathVariable Long id,
                                                   HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(prenotazioneService.accettaPrenotazione(id, tutorId));
    }

    /** POST /api/prenotazioni/{id}/rifiuta -- solo il tutor proprietario. */
    @PostMapping("/{id}/rifiuta")
    public ResponseEntity<PrenotazioneDTO> rifiuta(@PathVariable Long id,
                                                   HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(prenotazioneService.rifiutaPrenotazione(id, tutorId));
    }

    /** POST /api/prenotazioni/{id}/ritira -- solo lo studente titolare. */
    @PostMapping("/{id}/ritira")
    public ResponseEntity<PrenotazioneDTO> ritira(@PathVariable Long id,
                                                  HttpSession sessione) {
        Long studenteId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(prenotazioneService.ritiraPrenotazione(id, studenteId));
    }
}
