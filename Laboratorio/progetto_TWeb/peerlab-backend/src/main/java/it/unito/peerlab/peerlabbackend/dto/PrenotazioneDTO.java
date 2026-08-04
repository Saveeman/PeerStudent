package it.unito.peerlab.peerlabbackend.dto;

import it.unito.peerlab.peerlabbackend.model.StatoPrenotazione;

import java.time.LocalDateTime;

/**
 * Vista di una Prenotazione. Include qualche dato "denormalizzato" della
 * sessione (titolo, data) perche' il front-end li mostra nella lista delle
 * proprie prenotazioni senza doverli chiedere con una seconda richiesta.
 */
public record PrenotazioneDTO(
        Long id,
        StatoPrenotazione stato,
        LocalDateTime dataRichiesta,
        String messaggio,
        String emailContatto,
        UtenteDTO studente,
        Long sessioneId,
        String sessioneTitolo,
        LocalDateTime sessioneDataOra,
        String tutorNome,
        boolean haFeedback
) {
}
