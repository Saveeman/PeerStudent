package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.dto.*;
import it.unito.peerlab.peerlabbackend.service.SessioneService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route relative alle sessioni di tutoraggio.
 *
 * Mostra i tre modi di ricevere input previsti dalle slide:
 *  - query string   -> GET /api/sessioni?materiaId=3
 *  - path variable  -> GET /api/sessioni/12
 *  - body           -> POST /api/sessioni
 */
@RestController
@RequestMapping("/api/sessioni")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SessioneController {

    private final SessioneService sessioneService;

    public SessioneController(SessioneService sessioneService) {
        this.sessioneService = sessioneService;
    }

    /**
     * GET /api/sessioni
     * GET /api/sessioni?materiaId=3
     * GET /api/sessioni?q=integrali
     *
     * Elenco delle sessioni aperte, opzionalmente filtrate per materia o per
     * testo contenuto nel titolo. I parametri sono nella QUERY STRING e sono
     * facoltativi (required = false).
     */
    @GetMapping
    public ResponseEntity<List<SessioneDTO>> elencaSessioni(
            @RequestParam(required = false) Long materiaId,
            @RequestParam(required = false) String q) {

        if (materiaId != null) {
            return ResponseEntity.ok(sessioneService.getSessioniPerMateria(materiaId));
        }
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(sessioneService.cercaPerTitolo(q));
        }
        return ResponseEntity.ok(sessioneService.getSessioniAperte());
    }

    /**
     * GET /api/sessioni/{id}
     * Dettaglio di una sessione: l'id e' un SEGMENTO PARAMETRICO del path.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessioneDTO> dettaglioSessione(@PathVariable Long id) {
        return ResponseEntity.ok(sessioneService.getById(id));
    }

    /**
     * GET /api/sessioni/tutor/{tutorId}
     * Tutte le sessioni create da un tutor.
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<SessioneDTO>> sessioniDelTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(sessioneService.getSessioniDelTutor(tutorId));
    }

    /**
     * POST /api/sessioni
     * Creazione di una sessione: i dati arrivano nel BODY della richiesta,
     * l'autore viene dalla sessione HTTP (non dal body: non ci si puo' fidare
     * di un id di utente inviato dal client).
     */
    @PostMapping
    public ResponseEntity<SessioneDTO> creaSessione(@RequestBody NuovaSessioneRequest richiesta,
                                                    HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        SessioneDTO creata = sessioneService.creaSessione(richiesta, tutorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creata);
    }

    /** POST /api/sessioni/{id}/chiudi -- il tutor chiude le iscrizioni. */
    @PostMapping("/{id}/chiudi")
    public ResponseEntity<SessioneDTO> chiudiIscrizioni(@PathVariable Long id,
                                                        HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(sessioneService.chiudiIscrizioni(id, tutorId));
    }

    /** POST /api/sessioni/{id}/completa -- la sessione si e' svolta. */
    @PostMapping("/{id}/completa")
    public ResponseEntity<SessioneDTO> completaSessione(@PathVariable Long id,
                                                        HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(sessioneService.completaSessione(id, tutorId));
    }

    /** POST /api/sessioni/{id}/annulla */
    @PostMapping("/{id}/annulla")
    public ResponseEntity<SessioneDTO> annullaSessione(@PathVariable Long id,
                                                       HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(sessioneService.annullaSessione(id, tutorId));
    }

    /** GET /api/sessioni/{id}/materiali */
    @GetMapping("/{id}/materiali")
    public ResponseEntity<List<MaterialeDidatticoDTO>> materiali(@PathVariable Long id) {
        return ResponseEntity.ok(sessioneService.getMaterialiDiSessione(id));
    }

    /**
     * POST /api/sessioni/{id}/materiali
     * Combina path variable (quale sessione) e body (i dati del materiale).
     */
    @PostMapping("/{id}/materiali")
    public ResponseEntity<MaterialeDidatticoDTO> aggiungiMateriale(
            @PathVariable Long id,
            @RequestBody NuovoMaterialeRequest richiesta,
            HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        MaterialeDidatticoDTO creato = sessioneService.aggiungiMateriale(id, richiesta, tutorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creato);
    }
}
