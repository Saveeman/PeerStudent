package it.unito.peerlab.peerlabbackend.config;

import it.unito.peerlab.peerlabbackend.controller.SessioneUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtro di autenticazione.
 *
 * Attraverso questo filtro passano TUTTE le richieste HTTP prima di giungere
 * ai request handler. Il filtro decide se inoltrarle nella filter chain oppure
 * respingerle con uno status code di errore.
 *
 * La regola applicata e' semplice: se nella sessione HTTP non c'e' l'attributo
 * che identifica l'utente autenticato, la richiesta viene respinta con
 * 401 Unauthorized. Fanno eccezione le richieste elencate in
 * "passaLiberamente".
 *
 * Il filtro e' annotato @Component: e' cosi' che Spring Boot lo inserisce
 * nella filter chain. @Order(1) stabilisce che venga eseguito per primo fra i
 * filtri applicativi.
 */
@Component
@Order(1)
public class FiltroAutenticazione implements Filter {

    /** Origine dell'applicazione client-side, la stessa dichiarata nei @CrossOrigin. */
    private static final String ORIGINE_CONSENTITA = "http://localhost:5173";

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        // Gli oggetti arrivano nella forma "generica": per accedere alle
        // informazioni specifiche di HTTP serve il type casting.
        HttpServletRequest richiesta = (HttpServletRequest) servletRequest;
        HttpServletResponse risposta = (HttpServletResponse) servletResponse;

        String percorso = richiesta.getRequestURI();
        String metodo = richiesta.getMethod();

        if (passaLiberamente(metodo, percorso)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Attenzione: getSession(false) NON crea una nuova sessione se non
        // esiste. Con getSession() ne verrebbe creata una vuota a ogni
        // richiesta anonima, e il controllo sull'attributo sarebbe comunque
        // negativo, ma si sprecherebbero sessioni inutili.
        HttpSession sessione = richiesta.getSession(false);
        boolean autenticato = sessione != null
                && sessione.getAttribute(SessioneUtils.ATTRIBUTO_UTENTE) != null;

        if (autenticato) {
            // richiesta autorizzata: passa il testimone al prossimo elemento
            // della catena (o, se i filtri sono finiti, al request handler)
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // Richiesta bloccata: non si invoca la chain e si risponde con un
            // errore.
            //
            // Nota importante: bloccando qui, la richiesta non raggiunge mai
            // Spring MVC, quindi le annotazioni @CrossOrigin dei controller non
            // vengono applicate. Senza gli header CORS il browser impedirebbe
            // al codice JavaScript di leggere questa risposta, e il front-end
            // vedrebbe un errore di rete generico invece di un 401. Li
            // aggiungiamo quindi esplicitamente.
            risposta.setHeader("Access-Control-Allow-Origin", ORIGINE_CONSENTITA);
            risposta.setHeader("Access-Control-Allow-Credentials", "true");
            risposta.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Devi effettuare il login");
        }
    }

    /**
     * Richieste che devono passare in ogni caso, anche senza autenticazione.
     *
     * - OPTIONS: e' la preflight del CORS. Il browser la invia PRIMA della
     *   richiesta vera e non vi allega i cookie: se la bloccassimo, il browser
     *   riterrebbe vietata anche la richiesta successiva e nemmeno il login
     *   funzionerebbe.
     * - POST /api/auth/login: e' la richiesta con cui ci si autentica, quindi
     *   per definizione non puo' richiedere di essere gia' autenticati.
     * - POST /api/auth/logout e GET /api/auth/me: devono poter essere invocate
     *   anche da chi non ha una sessione valida, per permettere al front-end di
     *   verificare lo stato dell'autenticazione. La /me risponde comunque 401
     *   attraverso il controller, che e' il comportamento atteso dal client.
     */
    private boolean passaLiberamente(String metodo, String percorso) {
        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            return true;
        }
        return percorso.startsWith("/api/auth/");
    }
}
