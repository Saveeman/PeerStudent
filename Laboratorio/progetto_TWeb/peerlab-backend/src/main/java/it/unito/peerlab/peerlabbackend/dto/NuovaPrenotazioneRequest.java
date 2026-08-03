package it.unito.peerlab.peerlabbackend.dto;

/** Body della POST /api/prenotazioni (richiesta di partecipazione a una sessione) */
public record NuovaPrenotazioneRequest(
        Long sessioneId,
        String messaggio
) {
}
