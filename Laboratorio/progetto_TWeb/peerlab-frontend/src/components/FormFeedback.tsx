import { useState } from "react";
import type { ReactElement } from "react";
import { useChiusuraConEsc } from "../hooks/useChiusuraConEsc";
import { ApiError, feedbackApi } from "../api/api";
import type { Prenotazione } from "../types";

/**
 * Finestra modale per lasciare una valutazione.
 *
 * E' il secondo form che genera una richiesta POST (il primo e' la creazione
 * di una sessione).
 *
 * Segue il modello "modal dialog" descritto nelle slide: la visibilita' e'
 * responsabilita' del genitore, e la delega avviene sia alla conferma sia
 * all'annullamento, cosi' il genitore puo' far scomparire la finestra.
 */
interface FormFeedbackProps {
  prenotazione: Prenotazione;
  onInviato: () => void;
  onAnnulla: () => void;
}

export function FormFeedback({
  prenotazione,
  onInviato,
  onAnnulla,
}: FormFeedbackProps): ReactElement {
  // chiusura con il tasto Esc, oltre che con il click fuori
  useChiusuraConEsc(onAnnulla);

  const [voto, setVoto] = useState<number>(5);
  const [commento, setCommento] = useState<string>("");
  const [errore, setErrore] = useState<string>("");
  const [inCorso, setInCorso] = useState<boolean>(false);

  async function invia() {
    setErrore("");
    setInCorso(true);
    try {
      await feedbackApi.crea({
        prenotazioneId: prenotazione.id,
        voto,
        commento,
      });
      onInviato();
    } catch (e) {
      setErrore(
        e instanceof ApiError ? e.message : "Impossibile inviare la valutazione"
      );
    } finally {
      setInCorso(false);
    }
  }

  return (
    /* Il "glasspane": cliccando fuori dalla finestra si annulla. */
    <div className="sovrapposizione" onClick={onAnnulla}>
      {/* stopPropagation impedisce che il click sulla finestra risalga fino al
          glasspane e la chiuda: e' il bubbling degli eventi del DOM. */}
      <div className="modale"
        role="dialog"
        aria-modal="true"
        aria-labelledby="titolo-valutazione"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="modale-titolo" id="titolo-valutazione">Valuta la sessione</h2>
        <p className="modale-sottotitolo">{prenotazione.sessioneTitolo}</p>

        <label className="campo-etichetta">Voto</label>
        <div className="riga-voti">
          {[1, 2, 3, 4, 5].map((n) => (
            <button
              key={n}
              className={n === voto ? "voto voto-attivo" : "voto"}
              onClick={() => setVoto(n)}
            >
              {n}
            </button>
          ))}
        </div>

        <label className="campo-etichetta" htmlFor="commento">
          Commento
        </label>
        <textarea
          id="commento"
          className="campo-textarea"
          value={commento}
          onChange={(e) => setCommento(e.target.value)}
          placeholder="Com'e' andata la sessione?"
        />

        {errore !== "" && <p className="campo-errore" role="alert">{errore}</p>}

        <div className="riga-bottoni">
          <button
            className="bottone-primario"
            onClick={invia}
            disabled={inCorso}
          >
            {inCorso ? "Invio…" : "Invia valutazione"}
          </button>
          <button className="bottone-secondario" onClick={onAnnulla}>
            Annulla
          </button>
        </div>
      </div>
    </div>
  );
}
