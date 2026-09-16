package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.dto.LoginRequest;
import it.unito.peerlab.peerlabbackend.dto.UtenteDTO;
import it.unito.peerlab.peerlabbackend.service.UtenteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Gestione di login, logout e verifica dello stato di autenticazione.
 *
 * Questo controller e' escluso dal conteggio dei @RestController richiesti
 * dalla traccia, perche' si occupa esclusivamente della sessione.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final UtenteService utenteService;

    public AuthController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    /**
     * POST /api/auth/login
     * Verifica le credenziali e, se corrette, memorizza l'id dell'utente nella
     * sessione: da quel momento il cookie di sessione identifica l'utente.
     */
    @PostMapping("/login")
    public ResponseEntity<UtenteDTO> login(@RequestBody LoginRequest richiesta,
                                           HttpSession sessione) {
        UtenteDTO utente = utenteService.login(richiesta);
        sessione.setAttribute(SessioneUtils.ATTRIBUTO_UTENTE, utente.id());
        sessione.setMaxInactiveInterval(60 * 60); // un'ora di inattivita'
        return ResponseEntity.ok(utente);
    }

    /**
     * POST /api/auth/logout
     * Invalida la sessione: il cookie non e' piu' valido.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession sessione) {
        sessione.invalidate();
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/auth/me
     * Permette al front-end di sapere se c'e' un utente autenticato e chi e'.
     * Deve essere accessibile anche ai non autenticati: risponde 401 se nessuno
     * ha effettuato il login.
     */
    @GetMapping("/me")
    public ResponseEntity<UtenteDTO> utenteCorrente(HttpSession sessione) {
        Long utenteId = SessioneUtils.getUtenteId(sessione);
        return ResponseEntity.ok(utenteService.getById(utenteId));
    }
}
