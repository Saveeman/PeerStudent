import type { ReactElement } from "react";
import type { Prenotazione } from "../types";

/**
 * Pannello delle richieste in attesa rivolte al tutor.
 *
 * E' il componente che rende visibile la differenza di permessi: uno studente
 * questa schermata non ce l'ha proprio, perche' App monta VistaTutor solo per
 * gli utenti con ruolo TUTOR (e il back-end verifica comunque che chi accetta
 * sia il tutor proprietario della sessione).
 */
interface PannelloRichiesteProps {
  richieste: Prenotazione[];
  onAccetta: (prenotazioneId: number) => Promise<void>;
  onRifiuta: (prenotazioneId: number) => Promise<void>;
}

function formattaData(dataIso: string): string {
  return new Date(dataIso).toLocaleString("it-IT", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function PannelloRichieste({
  richieste,
  onAccetta,
  onRifiuta,
}: PannelloRichiesteProps): ReactElement {
  return (
    <aside className="pannello">
      <h2 className="pannello-titolo">
        Richieste da gestire
        {richieste.length > 0 && (
          <span className="contatore">{richieste.length}</span>
        )}
      </h2>

      {richieste.length === 0 ? (
        <p className="messaggio-vuoto-piccolo">
          Nessuna richiesta in attesa.
        </p>
      ) : (
        <ul className="elenco">
          {richieste.map((r) => (
            <li key={r.id} className="elenco-voce">
              <div className="elenco-voce-testa">
                <span className="elenco-voce-titolo">
                  {r.studente.nome} {r.studente.cognome}
                </span>
              </div>

              <p className="elenco-voce-meta">
                {r.sessioneTitolo} · {formattaData(r.sessioneDataOra)}
              </p>

              {r.messaggio && (
                <p className="citazione">{r.messaggio}</p>
              )}

              <div className="elenco-voce-azioni">
                <button
                  className="bottone-secondario"
                  onClick={() => onAccetta(r.id)}
                >
                  Accetta
                </button>
                <button
                  className="bottone-testo"
                  onClick={() => onRifiuta(r.id)}
                >
                  Rifiuta
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
}
