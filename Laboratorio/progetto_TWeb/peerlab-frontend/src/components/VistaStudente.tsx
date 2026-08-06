import { useCallback, useEffect, useState } from "react";
import type { ReactElement } from "react";
import { avvisiApi, catalogoApi, prenotazioniApi, sessioniApi } from "../api/api";
import { FiltroMaterie } from "./FiltroMaterie";
import { ListaSessioni } from "./ListaSessioni";
import { MiePrenotazioni } from "./MiePrenotazioni";
import { PannelloNotifiche } from "./PannelloNotifiche";
import { FormFeedback } from "./FormFeedback";
import { BarraRicerca } from "./BarraRicerca";
import { DettaglioSessione } from "./DettaglioSessione";
import { ProfiloTutor } from "./ProfiloTutor";
import type { Avviso, Materia, Prenotazione, Sessione, Utente } from "../types";

/**
 * Vista dello studente: coordina tre componenti figli che non si conoscono fra
 * loro.
 *
 * Qui vivono due requisiti importanti del progetto:
 *
 * 1. COMUNICAZIONE FRA FRATELLI: FiltroMaterie e ListaSessioni sono fratelli.
 *    Il filtro non conosce la lista; comunica la materia scelta a questo
 *    genitore, che aggiorna il proprio stato e passa alla lista sessioni
 *    diverse.
 *
 * 2. AGGIORNAMENTO INCROCIATO: quando l'utente prenota una sessione dalla
 *    lista, questo componente ricarica sia le sessioni sia le prenotazioni.
 *    Cambiano quindi ANCHE i dati mostrati da MiePrenotazioni, che e' un altro
 *    componente presente contemporaneamente sulla pagina.
 */
interface VistaStudenteProps {
  utente: Utente;
}

export function VistaStudente({ utente }: VistaStudenteProps): ReactElement {
  const [materie, setMaterie] = useState<Materia[]>([]);
  const [sessioni, setSessioni] = useState<Sessione[]>([]);
  const [prenotazioni, setPrenotazioni] = useState<Prenotazione[]>([]);
  const [avvisi, setAvvisi] = useState<Avviso[]>([]);

  const [materiaSelezionata, setMateriaSelezionata] = useState<number | null>(null);
  const [inCaricamento, setInCaricamento] = useState<boolean>(true);
  const [errore, setErrore] = useState<string>("");

  /** Prenotazione per cui si sta scrivendo una valutazione (null = form chiuso). */
  const [prenotazioneDaValutare, setPrenotazioneDaValutare] =
    useState<Prenotazione | null>(null);

  /** Testo di ricerca attualmente applicato ("" = nessuna ricerca). */
  const [ricerca, setRicerca] = useState<string>("");

  /** Titoli di TUTTE le sessioni, usati per i suggerimenti di ricerca.
      Si caricano una volta sola: se usassimo la lista filtrata, i suggerimenti
      si restringerebbero via via che l'utente digita. */
  const [titoliPerSuggerimenti, setTitoliPerSuggerimenti] = useState<string[]>([]);

  /** Sessione di cui e' aperto il dettaglio (null = nessuna). */
  const [sessioneDettaglio, setSessioneDettaglio] = useState<number | null>(null);

  /** Tutor di cui e' aperto il profilo (null = nessuno). */
  const [tutorProfilo, setTutorProfilo] = useState<Utente | null>(null);

  /* ---------------------------------------------------------- CARICAMENTI */

  /** Materie per il filtro e titoli per i suggerimenti: entrambi statici,
      si caricano una volta sola all'avvio della vista. */
  useEffect(() => {
    let annullato = false;

    Promise.all([catalogoApi.materie(), sessioniApi.elenco()])
      .then(([m, tutte]) => {
        if (!annullato) {
          setMaterie(m);
          setTitoliPerSuggerimenti(tutte.map((s) => s.titolo));
        }
      })
      .catch(() => {
        if (!annullato) setErrore("Impossibile caricare i dati iniziali");
      });

    return () => {
      annullato = true;
    };
  }, []);

  /**
   * Le sessioni si ricaricano ogni volta che cambia la materia selezionata.
   * La dipendenza [materiaSelezionata] fa si' che l'effetto venga rieseguito
   * esattamente quando serve.
   */
  useEffect(() => {
    let annullato = false;
    setInCaricamento(true);

    sessioniApi
      .elenco(materiaSelezionata ?? undefined, ricerca || undefined)
      .then((s) => {
        if (!annullato) setSessioni(s);
      })
      .catch(() => {
        if (!annullato) setErrore("Impossibile caricare le sessioni");
      })
      .finally(() => {
        if (!annullato) setInCaricamento(false);
      });

    /* Funzione di cleanup: se la materia cambia di nuovo prima che la risposta
       precedente arrivi, la ignoriamo. Senza questo, una risposta lenta
       relativa alla materia vecchia potrebbe sovrascrivere quella corretta. */
    return () => {
      annullato = true;
    };
  }, [materiaSelezionata, ricerca]);

  /** Ricarica le prenotazioni dell'utente. */
  /* Prenotazioni e avvisi si caricano insieme: gli avvisi che lo studente puo'
     vedere dipendono dagli appuntamenti a cui e' stato ammesso. */
  const caricaPrenotazioni = useCallback(async () => {
    try {
      const [p, a] = await Promise.all([
        prenotazioniApi.mie(),
        avvisiApi.miei(),
      ]);
      setPrenotazioni(p);
      setAvvisi(a);
    } catch {
      setErrore("Impossibile caricare le tue richieste");
    }
  }, []);

  useEffect(() => {
    caricaPrenotazioni();
  }, [caricaPrenotazioni]);

  /* ------------------------------------------------------------- AZIONI */

  /**
   * Invia una richiesta di prenotazione e RICARICA ENTRAMBE le liste:
   * le sessioni (per aggiornare i posti) e le prenotazioni (per far comparire
   * la nuova richiesta nel pannello laterale).
   */
  async function prenota(sessioneId: number, messaggio: string, email: string) {
    setErrore("");
    try {
      await prenotazioniApi.crea({ sessioneId, messaggio, emailContatto: email });
      const [nuoveSessioni] = await Promise.all([
        sessioniApi.elenco(materiaSelezionata ?? undefined, ricerca || undefined),
        caricaPrenotazioni(),
      ]);
      setSessioni(nuoveSessioni);
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore nella prenotazione");
    }
  }

  async function ritira(prenotazioneId: number) {
    setErrore("");
    try {
      await prenotazioniApi.ritira(prenotazioneId);
      const [nuoveSessioni] = await Promise.all([
        sessioniApi.elenco(materiaSelezionata ?? undefined, ricerca || undefined),
        caricaPrenotazioni(),
      ]);
      setSessioni(nuoveSessioni);
    } catch (e) {
      setErrore(e instanceof Error ? e.message : "Errore nel ritiro");
    }
  }

  async function feedbackInviato() {
    setPrenotazioneDaValutare(null);
    await caricaPrenotazioni();
  }

  /* ------------------------------------------------------------ RENDERING */

  return (
    <div className="vista">
      <BarraRicerca
        onCerca={(t) => setRicerca(t)}
        suggerimenti={titoliPerSuggerimenti}
      />

      <FiltroMaterie
        materie={materie}
        materiaSelezionata={materiaSelezionata}
        onCambiaMateria={(id) => setMateriaSelezionata(id)}
      />

      {errore !== "" && <p className="campo-errore" role="alert">{errore}</p>}

      <div className="colonne">
        <section className="colonna-principale">
          <h2 className="pannello-titolo">Sessioni disponibili</h2>
          <ListaSessioni
            sessioni={sessioni}
            prenotazioni={prenotazioni}
            utenteId={utente.id}
            inCaricamento={inCaricamento}
            onPrenota={prenota}
            onApriDettaglio={(id) => setSessioneDettaglio(id)}
            onApriProfiloTutor={(t) => setTutorProfilo(t)}
          />
        </section>

        <div className="colonna-laterale">
          <MiePrenotazioni
            prenotazioni={prenotazioni}
            onRitira={ritira}
            onValuta={(p) => setPrenotazioneDaValutare(p)}
          />

          <PannelloNotifiche prenotazioni={prenotazioni} avvisi={avvisi} />
        </div>
      </div>

      {/* Finestra modale: e' il genitore a decidere se mostrarla, perche' un
          componente non visibile non potrebbe modificare il proprio stato. */}
      {sessioneDettaglio !== null && (
        <DettaglioSessione
          sessioneId={sessioneDettaglio}
          utenteId={utente.id}
          onChiudi={() => setSessioneDettaglio(null)}
        />
      )}

      {tutorProfilo !== null && (
        <ProfiloTutor tutor={tutorProfilo} onChiudi={() => setTutorProfilo(null)} />
      )}

      {prenotazioneDaValutare && (
        <FormFeedback
          prenotazione={prenotazioneDaValutare}
          onInviato={feedbackInviato}
          onAnnulla={() => setPrenotazioneDaValutare(null)}
        />
      )}
    </div>
  );
}
