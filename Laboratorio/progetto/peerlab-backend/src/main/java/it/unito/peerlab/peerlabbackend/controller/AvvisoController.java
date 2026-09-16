package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.dto.AvvisoDTO;
import it.unito.peerlab.peerlabbackend.dto.NuovoAvvisoRequest;
import it.unito.peerlab.peerlabbackend.service.AvvisoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route relative agli avvisi inviati dai tutor ai partecipanti.
 */
@RestController
@RequestMapping("/api/avvisi")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AvvisoController {

    private final AvvisoService avvisoService;

    public AvvisoController(AvvisoService avvisoService) {
        this.avvisoService = avvisoService;
    }

    /** POST /api/avvisi -- il tutor invia un avviso ai partecipanti. */
    @PostMapping
    public ResponseEntity<AvvisoDTO> inviaAvviso(@RequestBody NuovoAvvisoRequest richiesta,
                                                 HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        AvvisoDTO creato = avvisoService.creaAvviso(richiesta, tutorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creato);
    }

    /** GET /api/avvisi/miei -- gli avvisi destinati allo studente collegato. */
    @GetMapping("/miei")
    public ResponseEntity<List<AvvisoDTO>> mieiAvvisi(HttpSession sessione) {
        Long studenteId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(avvisoService.getAvvisiPerStudente(studenteId));
    }

    /** GET /api/avvisi/sessione/{sessioneId} -- avvisi di un appuntamento. */
    @GetMapping("/sessione/{sessioneId}")
    public ResponseEntity<List<AvvisoDTO>> avvisiDiSessione(@PathVariable Long sessioneId,
                                                            HttpSession sessione) {
        Long tutorId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(avvisoService.getAvvisiDiSessione(sessioneId, tutorId));
    }
}
