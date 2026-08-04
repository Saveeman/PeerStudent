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
      const utente = await authApi.login({ username, password });
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
        <h1 className="login-titolo">PeerLab</h1>
        <p className="login-sottotitolo">
          Sessioni di ripasso tra studenti
        </p>

        <label className="campo-etichetta" htmlFor="username">
          Username
        </label>
        <input
          id="username"
          className="campo-input"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") inviaCredenziali();
          }}
        />

        <label className="campo-etichetta" htmlFor="password">
          Password
        </label>
        <input
          id="password"
          className="campo-input"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") inviaCredenziali();
          }}
        />

        {/* Lo spazio per il messaggio di errore richiesto dalla traccia */}
        {errore !== "" && <p className="campo-errore">{errore}</p>}

        <button
          className="bottone-primario"
          onClick={inviaCredenziali}
          disabled={inCorso}
        >
          {inCorso ? "Accesso in corso..." : "Accedi"}
        </button>

        <div className="login-aiuto">
          <p className="login-aiuto-titolo">Utenti di prova</p>
          <p>tutor: giulia / giulia123 &nbsp;·&nbsp; davide / davide123</p>
          <p>studenti: marco / marco123 &nbsp;·&nbsp; sara / sara123</p>
        </div>
      </div>
    </div>
  );
}
