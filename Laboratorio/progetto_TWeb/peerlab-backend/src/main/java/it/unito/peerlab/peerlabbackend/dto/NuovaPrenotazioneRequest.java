package it.unito.peerlab.peerlabbackend.dto;

/**
 * Body della POST /api/prenotazioni.
 *
 * La motivazione e' sempre obbligatoria; emailContatto lo e' soltanto per le
 * sessioni in modalita' ONLINE, perche' e' l'indirizzo a cui verra' inviato il
 * link della videochiamata.
 */
public record NuovaPrenotazioneRequest(
        Long sessioneId,
        String messaggio,
        String emailContatto
) {
}
