package it.unito.peerlab.peerlabbackend.dto;

import java.time.LocalDateTime;

public record AvvisoDTO(
        Long id,
        String testo,
        LocalDateTime data,
        Long sessioneId,
        String sessioneTitolo,
        String tutorNome
) {
}
