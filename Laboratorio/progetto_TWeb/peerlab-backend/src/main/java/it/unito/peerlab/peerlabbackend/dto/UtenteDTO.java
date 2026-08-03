package it.unito.peerlab.peerlabbackend.dto;

import it.unito.peerlab.peerlabbackend.model.Ruolo;

/**
 * Vista di un Utente destinata al front-end.
 * NON contiene la password: e' il motivo principale per cui non si espone
 * direttamente l'entita' Utente.
 */
public record UtenteDTO(
        Long id,
        String nome,
        String cognome,
        String username,
        String email,
        Ruolo ruolo,
        String bio
) {
}
