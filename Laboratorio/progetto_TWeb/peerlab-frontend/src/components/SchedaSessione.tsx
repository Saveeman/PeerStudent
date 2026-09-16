import { useState } from "react";
import type { ReactElement } from "react";
import type { Prenotazione, Sessione, Utente } from "../types";

/**
 * Scheda di una singola sessione, con il pulsante per prenotarsi.
 *
 * Riceve dal genitore:
 *  - la sessione da mostrare
 *  - l'eventuale prenotazione gia' inviata dall'utente per questa sessione
 *  - la callback da invocare quando l'utente vuole prenotarsi
 *
 * Nota: il componente non chiama direttamente la API. Espone l'evento
 * "prenotazione richiesta" al genitore, che ha la visione d'insieme e puo'
 * aggiornare anche gli altri componenti della pagina.
 */
interface SchedaSessioneProps {
  sessione: Sessione;
  prenotazioneEsistente: Prenotazione | undefined;
  utenteId: number;
  onPrenota: (sessioneId: number, messaggio: string, email: string) => Promise<void>;
  onApriDettaglio: (sessioneId: number) => void;
  onApriProfiloTutor: (tutor: Utente) => void;
}

/** Formatta una data ISO nel formato italiano leggibile. */
function formattaData(dataIso: string): string {
  const data = new Date(dataIso);
  return data.toLocaleString("it-IT", {
    weekday: "long",
    day: "numeric",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function SchedaSessione({
  sessione,
  prenotazioneEsistente,
  utenteId,
  onPrenota,
  onApriDettaglio,
  onApriProfiloTutor,
}: SchedaSessioneProps): ReactElement {
  const [messaggio, setMessaggio] = useState<string>("");
  const [email, setEmail] = useState<string>("");
  const [erroreForm, setErroreForm] = useState<string>("");
  const [formAperto, setFormAperto] = useState<boolean>(false);
  const [inCorso, setInCorso] = useState<boolean>(false);

  const eOnline = sessione.modalita === "ONLINE";

  const eMiaSessione = sessione.tutor.id === utenteId;
  const esaurita = sessione.postiDisponibili <= 0;

  async function confermaPrenotazione() {
    setErroreForm("");

    // La motivazione e' sempre obbligatoria.
    if (messaggio.trim() === "") {
      setErroreForm("Devi motivare la tua richiesta di partecipazione");
      return;
    }

    // Per le sessioni online serve anche l'email istituzionale: e' l'indirizzo
    // a cui il tutor invia il link della videochiamata.
    if (eOnline) {
      const indirizzo = email.trim().toLowerCase();
      if (indirizzo === "") {
        setErroreForm("Per le sessioni online devi indicare la tua email istituzionale");
        return;
      }
      if (
        !indirizzo.endsWith("@edu.unito.it") &&
        !indirizzo.endsWith("@unito.it")
      ) {
        setErroreForm("Usa un'email istituzionale (@edu.unito.it o @unito.it)");
        return;
      }
    }

    setInCorso(true);
    try {
      await onPrenota(sessione.id, messaggio.trim(), eOnline ? email.trim() : "");
      setMessaggio("");
      setEmail("");
      setFormAperto(false);
    } finally {
      setInCorso(false);
    }
  }

  /** Decide cosa mostrare nell'angolo in basso a destra della scheda. */
  function azione(): ReactElement {
    if (eMiaSessione) {
      return <span className="stato-tenue">Sessione creata da te</span>;
    }

    if (prenotazioneEsistente) {
      const etichette: Record<string, string> = {
        IN_ATTESA: "Richiesta inviata",
        ACCETTATA: "Sei iscritto",
        RIFIUTATA: "Richiesta rifiutata",
        RITIRATA: "Richiesta ritirata",
      };
      const classi: Record<string, string> = {
        IN_ATTESA: "stato-attesa",
        ACCETTATA: "stato-ok",
        RIFIUTATA: "stato-no",
        RITIRATA: "stato-tenue",
      };
      return (
        <span className={classi[prenotazioneEsistente.stato]}>
          {etichette[prenotazioneEsistente.stato]}
        </span>
      );
    }

    if (esaurita) {
      return <span className="stato-tenue">Al completo</span>;
    }

    if (!formAperto) {
      return (
        <button className="bottone-secondario" onClick={() => setFormAperto(true)}>
          Prenota
        </button>
      );
    }

    return <span className="stato-tenue">Compila la richiesta</span>;
  }

  return (
    <article className="scheda">
      <div className="scheda-testa">
        <div>
          <h3 className="scheda-titolo">
            <button
              className="link-titolo"
              onClick={() => onApriDettaglio(sessione.id)}
            >
              {sessione.titolo}
            </button>
          </h3>
          <p className="scheda-meta">
            <button
              className="link-tutor"
              onClick={() => onApriProfiloTutor(sessione.tutor)}
            >
              {sessione.tutor.nome} {sessione.tutor.cognome}
            </button>
            {" · "}
            {sessione.materia.nome}
          </p>
        </div>
        <div className="scheda-posti">
          <span className={esaurita ? "posti-esauriti" : "posti-liberi"}>
            {sessione.postiDisponibili} di {sessione.postiTotali} posti
          </span>
        </div>
      </div>

      {sessione.descrizione && (
        <p className="scheda-descrizione">{sessione.descrizione}</p>
      )}

      <div className="scheda-dettagli">
        <span>{formattaData(sessione.dataOra)}</span>
        <span>·</span>
        <span>{sessione.tutor.email}</span>
        <span>·</span>
        <span>
          {eOnline ? (
            <>
              Online <em className="nota">(il link della call arriva per email)</em>
            </>
          ) : (
            sessione.luogo
          )}
        </span>
      </div>

      {sessione.argomenti.length > 0 && (
        <div className="scheda-argomenti">
          {sessione.argomenti.map((a) => (
            <span key={a.id} className="etichetta">
              {a.nome}
            </span>
          ))}
        </div>
      )}

      <div className="scheda-piede">{azione()}</div>

      {formAperto && (
        <div className="scheda-form">
          <label className="campo-etichetta" htmlFor={`msg-${sessione.id}`}>
            Perche' vuoi partecipare? <span className="obbligatorio">*</span>
          </label>
          <textarea
            id={`msg-${sessione.id}`}
            className="campo-textarea"
            value={messaggio}
            onChange={(e) => setMessaggio(e.target.value)}
            placeholder="Es. sono in difficolta' con gli esercizi del secondo tipo"
          />

          {eOnline && (
            <>
              <label className="campo-etichetta" htmlFor={`mail-${sessione.id}`}>
                Email istituzionale <span className="obbligatorio">*</span>
              </label>
              <input
                id={`mail-${sessione.id}`}
                className="campo-input"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="nome.cognome@edu.unito.it"
              />
              <p className="nota-form">
                La sessione si svolge online: il link della videochiamata verra'
                inviato a questo indirizzo.
              </p>
            </>
          )}

          {erroreForm !== "" && <p className="campo-errore" role="alert">{erroreForm}</p>}

          <div className="riga-bottoni">
            <button
              className="bottone-primario"
              onClick={confermaPrenotazione}
              disabled={inCorso}
            >
              {inCorso ? "Invio..." : "Invia richiesta"}
            </button>
            <button
              className="bottone-secondario"
              onClick={() => {
                setFormAperto(false);
                setMessaggio("");
                setEmail("");
                setErroreForm("");
              }}
            >
              Annulla
            </button>
          </div>
        </div>
      )}
    </article>
  );
}
