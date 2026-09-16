package it.unito.peerlab.peerlabbackend.dto;

public record MateriaDTO(
        Long id,
        String nome,
        String codiceEsame,
        Integer annoCorso
) {
}
