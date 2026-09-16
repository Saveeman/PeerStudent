import type { ReactElement } from "react";
import type { Sessione } from "../types";

/**
 * Elenco delle sessioni create dal tutor collegato, con le azioni per
 * gestirne il ciclo di vita (chiudere le iscrizioni, segnare come svolta,
 * annullare).
 */
interface MieSessioniProps {
  sessioni: Sessione[];
  inCaricamento: boolean;
  onChiudi: (sessioneId: number) => Promise<void>;
  onCompleta: (sessioneId: number) => Promise<void>;
  onAnnulla: (sessioneId: number) => Promise<void>;
  onApriDettaglio: (sessioneId: number) => void;
}

const ETICHETTE_STATO: Record<string, string> = {
  APERTA: "Aperta",
  CHIUSA: "Iscrizioni chiuse",
  COMPLETATA: "Svolta",
  ANNULLATA: "Annullata",
};

const CLASSI_STATO: Record<string, string> = {
  APERTA: "stato-ok",
  CHIUSA: "stato-attesa",
  COMPLETATA: "stato-tenue",
  ANNULLATA: "stato-no",
};

function formattaData(dataIso: string): string {
  return new Date(dataIso).toLocaleString("it-IT", {
    weekday: "short",
    day: "numeric",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function MieSessioni({
  sessioni,
  inCaricamento,
  onChiudi,
  onCompleta,
  onAnnulla,
  onApriDettaglio,
}: MieSessioniProps): ReactElement {
  if (inCaricamento) {
    return <p className="messaggio-vuoto">Caricamento…</p>;
  }

  if (sessioni.length === 0) {
    return (
      <p className="messaggio-vuoto">
        Non hai ancora creato sessioni. Usa il pulsante qui sopra per iniziare.
      </p>
    );
  }

  return (
    <div className="lista-sessioni">
      {sessioni.map((s) => (
        <article key={s.id} className="scheda">
          <div className="scheda-testa">
            <div>
              <h3 className="scheda-titolo">
                <button
                  className="link-titolo"
                  onClick={() => onApriDettaglio(s.id)}
                >
                  {s.titolo}
                </button>
              </h3>
              <p className="scheda-meta">{s.materia.nome}</p>
            </div>
            <span className={CLASSI_STATO[s.stato]}>
              {ETICHETTE_STATO[s.stato]}
            </span>
          </div>

          <div className="scheda-dettagli">
            <span>{formattaData(s.dataOra)}</span>
            <span>·</span>
            <span>{s.modalita === "ONLINE" ? "Online" : s.luogo}</span>
            <span>·</span>
            <span>
              {s.postiOccupati} iscritti su {s.postiTotali}
            </span>
          </div>

          <div className="scheda-piede">
            {s.stato === "APERTA" && (
              <button
                className="bottone-secondario"
                onClick={() => onChiudi(s.id)}
              >
                Chiudi iscrizioni
              </button>
            )}
            {(s.stato === "APERTA" || s.stato === "CHIUSA") && (
              <>
                <button
                  className="bottone-secondario"
                  onClick={() => onCompleta(s.id)}
                >
                  Segna come svolta
                </button>
                <button
                  className="bottone-testo"
                  onClick={() => onAnnulla(s.id)}
                >
                  Annulla
                </button>
              </>
            )}
          </div>
        </article>
      ))}
    </div>
  );
}
