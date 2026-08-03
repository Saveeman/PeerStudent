package it.unito.peerlab.peerlabbackend.exception;

/** Lanciata quando una risorsa richiesta non esiste. Il controller la traduce in 404 Not Found. */
public class RisorsaNonTrovataException extends RuntimeException {
    public RisorsaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}
