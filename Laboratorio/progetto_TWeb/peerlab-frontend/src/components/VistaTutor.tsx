import { useCallback, useEffect, useState } from "react";
import type { ReactElement } from "react";
import { prenotazioniApi, sessioniApi } from "../api/api";
import { FormNuovaSessione } from "./FormNuovaSessione";
import { MieSessioni } from "./MieSessioni";
import { PannelloRichieste } from "./PannelloRichieste";
import { DettaglioSessione } from "./DettaglioSessione";
import type { Prenotazione, Sessione, Utente } from "../types";

/**
 * Vista del tutor.
 *
 * Qui si realizza l'AGGIORNAMENTO INCROCIATO fra componenti richiesto dalla
 * traccia: quando il tutor accetta una richiesta dal PannelloRichieste, il
 * numero di iscritti cambia in MieSessioni, che e' un componente diverso
 * presente contemporaneamente sulla pagina. I due non si conoscono: e' questo
 * genitore che, ricevuto l'evento, ricarica entrambi i set di dati.
 */
interface VistaTutorProps {
  utente: Utente;
}

export function VistaTutor({ utente }: VistaTutorProps): ReactElement {
  const [sessioni, setSessioni] = useState<Sessione[]>([]);
  const [richieste, setRichieste] = useState<Prenotazione[]>([]);
  const [inCaricamento, setInCaricamento] = useState<boolean>(true);
  const [formAperto, setFormAperto] = useState<boolean>(false);
  const [errore, setErrore] = useState<string>("");

  /** Sessione di cui e' aperto il dettaglio: da li' il tutor allega materiali
      e consulta tutte le richieste ricevute. */
  const [sessioneDettaglio, setSessioneDettaglio] = useState<number | null>(null);

  /** Ricarica insieme le sessioni del tutor e le richieste in attesa. */
  const ricaricaTutto = useCallback(async () => {
    try {
      const [nuoveSessioni, nuoveRichieste] = await Promise.all([
        sessioniApi.delTutor(utente.id),
        prenotazioniApi.richiesteInAttesa(),
      ]);
      setSessioni(nuoveSessioni);
      setRichieste(nuoveRichieste);
    } catch {
      setErrore("Impossibile caricare i dati");
    } finally {
      setInCaricamento(false);
    }
  }, [utente.id]);

  useEffect(() => {
    ricaricaTutto();
  }, [ricaricaTutto]);

  /* ------------------------------------------------------------- AZIONI */

  async function accetta(prenotazioneId: number) {
    setErrore("");
    try {
      await prenotazioniApi.accetta(prenotazioneId);
      // ricarica ENTRAMBI: cambia la lista richieste E il conteggio iscritti
      await ricaricaTutto();
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore nell'accettazione");
    }
  }

  async function rifiuta(prenotazioneId: number) {
    setErrore("");
    try {
      await prenotazioniApi.rifiuta(prenotazioneId);
      await ricaricaTutto();
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore nel rifiuto");
    }
  }

  async function chiudi(sessioneId: number) {
    setErrore("");
    try {
      await sessioniApi.chiudi(sessioneId);
      await ricaricaTutto();
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore");
    }
  }

  async function completa(sessioneId: number) {
    setErrore("");
    try {
      await sessioniApi.completa(sessioneId);
      await ricaricaTutto();
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore");
    }
  }

  async function annulla(sessioneId: number) {
    setErrore("");
    try {
      await sessioniApi.annulla(sessioneId);
      await ricaricaTutto();
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore");
    }
  }

  async function sessioneCreata() {
    setFormAperto(false);
    await ricaricaTutto();
  }

  /* ------------------------------------------------------------ RENDERING */

  return (
    <div className="vista">
      <div className="barra-azioni">
        <button className="bottone-primario bottone-stretto" onClick={() => setFormAperto(true)}>
          Crea una nuova sessione
        </button>
      </div>

      {errore !== "" && <p className="campo-errore">{errore}</p>}

      <div className="colonne">
        <section className="colonna-principale">
          <h2 className="pannello-titolo">Le mie sessioni</h2>
          <MieSessioni
            sessioni={sessioni}
            inCaricamento={inCaricamento}
            onChiudi={chiudi}
            onCompleta={completa}
            onAnnulla={annulla}
            onApriDettaglio={(id) => setSessioneDettaglio(id)}
          />
        </section>

        <PannelloRichieste
          richieste={richieste}
          onAccetta={accetta}
          onRifiuta={rifiuta}
        />
      </div>

      {sessioneDettaglio !== null && (
        <DettaglioSessione
          sessioneId={sessioneDettaglio}
          utenteId={utente.id}
          onChiudi={() => {
            setSessioneDettaglio(null);
            ricaricaTutto();
          }}
        />
      )}

      {formAperto && (
        <FormNuovaSessione
          onCreata={sessioneCreata}
          onAnnulla={() => setFormAperto(false)}
        />
      )}
    </div>
  );
}
