package it.unito.peerlab.peerlabbackend.dto;

public record ArgomentoDTO(
        Long id,
        String nome,
        Long materiaId,
        String materiaNome
) {
}
