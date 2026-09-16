package it.unito.peerlab.peerlabbackend.exception;

/** Lanciata quando l'operazione viola una regola di business. Il controller la traduce in 400 Bad Request. */
public class OperazioneNonValidaException extends RuntimeException {
    public OperazioneNonValidaException(String messaggio) {
        super(messaggio);
    }
}
