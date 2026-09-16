package it.unito.peerlab.peerlabbackend.dto;

import java.time.LocalDateTime;

public record FeedbackDTO(
        Long id,
        Integer voto,
        String commento,
        LocalDateTime data,
        Long prenotazioneId,
        String studenteNome,
        String sessioneTitolo
) {
}
