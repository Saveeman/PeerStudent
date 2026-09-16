import { useState } from "react";
import type { ReactElement } from "react";
import { ApiError, authApi } from "../api/api";
import type { Utente } from "../types";

/**
 * Props del componente: il genitore (App) fornisce la callback da invocare
 * quando il login e' andato a buon fine. E' un esempio di delega al genitore:
 * il Login non sa cosa fare dell'utente autenticato, si limita a comunicarlo.
 */
interface LoginProps {
  onLoginRiuscito: (utente: Utente) => void;
}

export function Login({ onLoginRiuscito }: LoginProps): ReactElement {
  // Uno state per ciascun campo di input: e' il binding bidirezionale.
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [mostraPassword, setMostraPassword] = useState<boolean>(false);
  const [errore, setErrore] = useState<string>("");
  const [inCorso, setInCorso] = useState<boolean>(false);

  async function inviaCredenziali() {
    setErrore("");

    if (username.trim() === "" || password.trim() === "") {
      setErrore("Inserisci username e password");
      return;
    }

    setInCorso(true);
    try {
      const utente = await authApi.login({
        username: username.trim(),
        password: password.trim(),
      });
      onLoginRiuscito(utente);
    } catch (e) {
      if (e instanceof ApiError) {
        setErrore(e.message);
      } else {
        setErrore("Impossibile contattare il server");
      }
    } finally {
      setInCorso(false);
    }
  }

  return (
    <div className="login-contenitore">
      <div className="login-riquadro">
        <div className="login-marchio">
          <h1 className="login-titolo">PeerStudent</h1>
        </div>

        <label className="campo-etichetta" htmlFor="username">
          Username
        </label>
        <input
          id="username"
          className="campo-input"
          type="text"
          autoComplete="off"
          autoCorrect="off"
          autoCapitalize="none"
          spellCheck={false}
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") inviaCredenziali();
          }}
        />

        <label className="campo-etichetta" htmlFor="password">
          Password
        </label>
        <div className="campo-con-icona">
          <input
            id="password"
            className="campo-input campo-input-icona"
            type={mostraPassword ? "text" : "password"}
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") inviaCredenziali();
            }}
          />
          <button
            type="button"
            className="bottone-occhio"
            onClick={() => setMostraPassword(!mostraPassword)}
            aria-label={mostraPassword ? "Nascondi la password" : "Mostra la password"}
            title={mostraPassword ? "Nascondi la password" : "Mostra la password"}
          >
            {mostraPassword ? <IconaOcchioBarrato /> : <IconaOcchio />}
          </button>
        </div>

        {/* Lo spazio per il messaggio di errore richiesto dalla traccia */}
        {errore !== "" && <p className="campo-errore" role="alert">{errore}</p>}

        <button
          className="bottone-primario"
          onClick={inviaCredenziali}
          disabled={inCorso}
        >
          {inCorso ? "Accesso in corso..." : "Accedi"}
        </button>
      </div>
    </div>
  );
}

/** Icona "occhio": la password e' nascosta, cliccando la si mostra. */
function IconaOcchio(): ReactElement {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none"
         stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"
         strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s3.5-6.5 10-6.5S22 12 22 12s-3.5 6.5-10 6.5S2 12 2 12z" />
      <circle cx="12" cy="12" r="2.8" />
    </svg>
  );
}

/** Icona "occhio barrato": la password e' visibile, cliccando la si nasconde. */
function IconaOcchioBarrato(): ReactElement {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none"
         stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"
         strokeLinejoin="round" aria-hidden="true">
      <path d="M2 12s3.5-6.5 10-6.5c2 0 3.7.6 5.1 1.4" />
      <path d="M21.5 10.6c.3.6.5 1.1.5 1.4 0 0-3.5 6.5-10 6.5-1.4 0-2.6-.3-3.7-.7" />
      <path d="M9.6 9.8a2.8 2.8 0 0 0 3.9 3.9" />
      <line x1="3" y1="3" x2="21" y2="21" />
    </svg>
  );
}
