package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import it.unito.peerlab.peerlabbackend.exception.OperazioneNonValidaException;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Gestione centralizzata degli errori.
 *
 * Invece di ripetere un try/catch in ogni request handler, questa classe
 * intercetta le eccezioni lanciate dai @Service e le traduce nello status code
 * HTTP appropriato, con un piccolo body JSON che contiene il messaggio.
 *
 * Corrispondenze:
 *   RisorsaNonTrovataException   -> 404 Not Found
 *   OperazioneNonValidaException -> 400 Bad Request
 *   NonAutorizzatoException      -> 401 Unauthorized
 *   qualsiasi altra eccezione    -> 500 Internal Server Error
 */
@RestControllerAdvice
public class GestoreErrori {

    @ExceptionHandler(RisorsaNonTrovataException.class)
    public ResponseEntity<Map<String, String>> risorsaNonTrovata(RisorsaNonTrovataException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("errore", e.getMessage()));
    }

    @ExceptionHandler(OperazioneNonValidaException.class)
    public ResponseEntity<Map<String, String>> operazioneNonValida(OperazioneNonValidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errore", e.getMessage()));
    }

    @ExceptionHandler(NonAutorizzatoException.class)
    public ResponseEntity<Map<String, String>> nonAutorizzato(NonAutorizzatoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("errore", e.getMessage()));
    }

    /**
     * Rete di sicurezza: qualunque eccezione non prevista diventa un 500.
     * La stack trace viene stampata a video per permettere il debugging,
     * come suggerito dalla traccia del progetto.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroreGenerico(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("errore", "Errore interno del server"));
    }
}
