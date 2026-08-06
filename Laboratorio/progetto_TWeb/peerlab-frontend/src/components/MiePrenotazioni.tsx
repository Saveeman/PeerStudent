import type { ReactElement } from "react";
import type { Prenotazione } from "../types";

/**
 * Pannello con le prenotazioni dell'utente collegato.
 *
 * E' il componente "fratello" della lista sessioni: quando l'utente prenota
 * una sessione nella lista, il genitore ricarica i dati e questo pannello si
 * aggiorna, pur non avendo alcun collegamento diretto con la lista.
 */
interface MiePrenotazioniProps {
  prenotazioni: Prenotazione[];
  onRitira: (prenotazioneId: number) => Promise<void>;
  onValuta: (prenotazione: Prenotazione) => void;
}

const ETICHETTE_STATO: Record<string, string> = {
  IN_ATTESA: "In attesa",
  ACCETTATA: "Accettata",
  RIFIUTATA: "Rifiutata",
  RITIRATA: "Ritirata",
};

const CLASSI_STATO: Record<string, string> = {
  IN_ATTESA: "stato-attesa",
  ACCETTATA: "stato-ok",
  RIFIUTATA: "stato-no",
  RITIRATA: "stato-tenue",
};

function formattaData(dataIso: string): string {
  return new Date(dataIso).toLocaleString("it-IT", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function MiePrenotazioni({
  prenotazioni,
  onRitira,
  onValuta,
}: MiePrenotazioniProps): ReactElement {
  if (prenotazioni.length === 0) {
    return (
      <aside className="pannello">
        <h2 className="pannello-titolo">Le mie richieste</h2>
        <p className="messaggio-vuoto-piccolo">
          Non hai ancora inviato richieste.
        </p>
      </aside>
    );
  }

  return (
    <aside className="pannello">
      <h2 className="pannello-titolo">Le mie richieste</h2>

      <ul className="elenco">
        {prenotazioni.map((p) => (
          <li key={p.id} className="elenco-voce">
            <div className="elenco-voce-testa">
              <span className="elenco-voce-titolo">{p.sessioneTitolo}</span>
              <span className={CLASSI_STATO[p.stato]}>
                {ETICHETTE_STATO[p.stato]}
              </span>
            </div>

            <p className="elenco-voce-meta">
              {p.tutorNome} · {formattaData(p.sessioneDataOra)}
            </p>


            <div className="elenco-voce-azioni">
              {p.stato === "IN_ATTESA" && (
                <button
                  className="bottone-testo"
                  onClick={() => onRitira(p.id)}
                >
                  Ritira richiesta
                </button>
              )}

              {p.stato === "ACCETTATA" && !p.haFeedback && (
                <button className="bottone-testo" onClick={() => onValuta(p)}>
                  Lascia una valutazione
                </button>
              )}

              {p.haFeedback && (
                <span className="stato-tenue">Valutazione inviata</span>
              )}
            </div>
          </li>
        ))}
      </ul>
    </aside>
  );
}
