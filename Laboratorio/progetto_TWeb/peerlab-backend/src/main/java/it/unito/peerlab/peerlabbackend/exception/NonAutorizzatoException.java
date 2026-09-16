package it.unito.peerlab.peerlabbackend.exception;

/** Lanciata quando l'utente non ha i permessi per l'operazione. Il controller la traduce in 401 Unauthorized. */
public class NonAutorizzatoException extends RuntimeException {
    public NonAutorizzatoException(String messaggio) {
        super(messaggio);
    }
}
