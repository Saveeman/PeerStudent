package it.unito.peerlab.peerlabbackend.dto;

/** Body della POST /api/feedback */
public record NuovoFeedbackRequest(
        Long prenotazioneId,
        Integer voto,
        String commento
) {
}
