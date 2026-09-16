package it.unito.peerlab.peerlabbackend.dto;

import it.unito.peerlab.peerlabbackend.model.Ruolo;

import java.time.LocalDate;

/**
 * Vista di un Utente destinata al front-end.
 *
 * NON contiene la password: e' il motivo principale per cui non si espone
 * direttamente l'entita' Utente.
 *
 * Il campo eta' non esiste nell'entita': viene calcolato dal DTOMapper a
 * partire dalla data di nascita.
 */
public record UtenteDTO(
        Long id,
        String nome,
        String cognome,
        String username,
        String email,
        String matricola,
        Ruolo ruolo,
        LocalDate dataNascita,
        Integer eta,
        String bio
) {
}
