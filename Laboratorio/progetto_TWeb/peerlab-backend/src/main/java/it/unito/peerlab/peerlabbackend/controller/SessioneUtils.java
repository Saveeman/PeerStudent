package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import jakarta.servlet.http.HttpSession;

/**
 * Piccola utility per leggere l'utente autenticato dalla sessione HTTP.
 *
 * L'identita' dell'utente non viaggia nelle richieste: e' conservata
 * server-side nella sessione, identificata dal cookie di sessione.
 */
public final class SessioneUtils {

    /** Nome dell'attributo di sessione in cui memorizziamo l'id dell'utente autenticato. */
    public static final String ATTRIBUTO_UTENTE = "utenteId";

    private SessioneUtils() {
    }

    /** Restituisce l'id dell'utente autenticato, o lancia NonAutorizzatoException se non c'e'. */
    public static Long getUtenteId(HttpSession sessione) {
        Object id = sessione.getAttribute(ATTRIBUTO_UTENTE);
        if (id == null) {
            throw new NonAutorizzatoException("Devi effettuare il login");
        }
        return (Long) id;
    }
}
