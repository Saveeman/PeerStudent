package it.unito.peerlab.peerlabbackend.dto;

/** Body della POST /api/avvisi */
public record NuovoAvvisoRequest(
        Long sessioneId,
        String testo
) {
}
