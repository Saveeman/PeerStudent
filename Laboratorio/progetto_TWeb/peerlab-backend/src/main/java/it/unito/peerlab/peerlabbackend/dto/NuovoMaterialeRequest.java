package it.unito.peerlab.peerlabbackend.dto;

/** Body della POST /api/sessioni/{id}/materiali */
public record NuovoMaterialeRequest(
        String titolo,
        String url,
        String tipo
) {
}
