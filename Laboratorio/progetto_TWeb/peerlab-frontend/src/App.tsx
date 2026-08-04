import { useEffect, useState } from "react";
import type { ReactElement } from "react";
import { authApi } from "./api/api";
import { Login } from "./components/Login";
import { VistaStudente } from "./components/VistaStudente";
import { VistaTutor } from "./components/VistaTutor";
import type { Utente } from "./types";
import "./App.css";

/**
 * Componente radice dell'applicazione.
 *
 * Mantiene lo stato piu' importante di tutti: chi e' l'utente collegato.
 * In base a quello monta la schermata di login oppure l'applicazione vera.
 * Essendo una Single Page Application, non c'e' mai una vera navigazione:
 * App smonta un figlio e ne monta un altro.
 */
export default function App(): ReactElement {
  const [utente, setUtente] = useState<Utente | null>(null);
  const [caricamentoIniziale, setCaricamentoIniziale] = useState<boolean>(true);

  /**
   * Effetto eseguito una sola volta al primo rendering (array di dipendenze
   * vuoto): chiede al back-end se esiste gia' una sessione attiva.
   *
   * Serve perche' il cookie di sessione sopravvive al ricaricamento della
   * pagina, mentre lo stato di React no: senza questo controllo, premendo F5
   * l'utente si ritroverebbe alla schermata di login pur essendo ancora
   * autenticato lato server.
   */
  useEffect(() => {
    let annullato = false;

    authApi
      .utenteCorrente()
      .then((u) => {
        if (!annullato) setUtente(u);
      })
      .catch(() => {
        // 401: nessuna sessione attiva, resta la schermata di login
      })
      .finally(() => {
        if (!annullato) setCaricamentoIniziale(false);
      });

    // Funzione di cleanup: se il componente venisse smontato prima che la
    // risposta arrivi, evitiamo di aggiornare uno stato non piu' esistente.
    return () => {
      annullato = true;
    };
  }, []);

  async function esegui_logout() {
    try {
      await authApi.logout();
    } finally {
      setUtente(null);
    }
  }

  if (caricamentoIniziale) {
    return <div className="schermata-attesa">Caricamento…</div>;
  }

  if (utente === null) {
    return <Login onLoginRiuscito={(u) => setUtente(u)} />;
  }

  return (
    <div className="app">
      <header className="intestazione">
        <span className="intestazione-logo">PeerLab</span>
        <div className="intestazione-utente">
          <span>
            {utente.nome} {utente.cognome}
          </span>
          <span className="badge-ruolo">
            {utente.ruolo === "TUTOR" ? "tutor" : "studente"}
          </span>
          <button className="bottone-secondario" onClick={esegui_logout}>
            Esci
          </button>
        </div>
      </header>

      <main className="contenuto">
        {/* In base al ruolo dell'utente viene montata una vista diversa:
            e' qui che si concretizza la differenza di permessi fra studente e
            tutor. Essendo una SPA, non c'e' navigazione: App smonta un figlio
            e ne monta un altro. */}
        {utente.ruolo === "TUTOR" ? (
          <VistaTutor utente={utente} />
        ) : (
          <VistaStudente utente={utente} />
        )}
      </main>
    </div>
  );
}
