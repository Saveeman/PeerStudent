import type { ReactElement } from "react";
import { SchedaSessione } from "./SchedaSessione";
import type { Prenotazione, Sessione, Utente } from "../types";

/**
 * Elenco delle sessioni disponibili.
 *
 * Riceve tutto dal genitore: le sessioni da mostrare (gia' filtrate), le
 * prenotazioni dell'utente (per sapere quali sessioni ha gia' richiesto) e la
 * callback di prenotazione, che si limita a inoltrare alle singole schede.
 */
interface ListaSessioniProps {
  sessioni: Sessione[];
  prenotazioni: Prenotazione[];
  utenteId: number;
  inCaricamento: boolean;
  onPrenota: (sessioneId: number, messaggio: string) => Promise<void>;
  onApriDettaglio: (sessioneId: number) => void;
  onApriProfiloTutor: (tutor: Utente) => void;
}

export function ListaSessioni({
  sessioni,
  prenotazioni,
  utenteId,
  inCaricamento,
  onPrenota,
  onApriDettaglio,
  onApriProfiloTutor,
}: ListaSessioniProps): ReactElement {
  if (inCaricamento) {
    return <p className="messaggio-vuoto">Caricamento delle sessioni…</p>;
  }

  if (sessioni.length === 0) {
    return (
      <p className="messaggio-vuoto">
        Nessuna sessione disponibile per questa materia.
      </p>
    );
  }

  return (
    <div className="lista-sessioni">
      {sessioni.map((sessione) => (
        <SchedaSessione
          key={sessione.id}
          sessione={sessione}
          /* cerca fra le prenotazioni dell'utente quella relativa a questa
             sessione: se esiste, la scheda mostrera' lo stato invece del
             pulsante "Prenota" */
          prenotazioneEsistente={prenotazioni.find(
            (p) => p.sessioneId === sessione.id && p.stato !== "RITIRATA"
          )}
          utenteId={utenteId}
          onPrenota={onPrenota}
          onApriDettaglio={onApriDettaglio}
          onApriProfiloTutor={onApriProfiloTutor}
        />
      ))}
    </div>
  );
}
