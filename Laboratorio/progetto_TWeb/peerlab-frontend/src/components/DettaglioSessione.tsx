import { useCallback, useEffect, useState } from "react";
import type { ReactElement } from "react";
import {
  ApiError,
  feedbackApi,
  prenotazioniApi,
  sessioniApi,
} from "../api/api";
import type {
  Feedback,
  MaterialeDidattico,
  Prenotazione,
  Sessione,
} from "../types";

/**
 * Finestra modale con il dettaglio completo di una sessione.
 *
 * Mostra contenuti diversi a seconda di chi la apre:
 *  - a chiunque: descrizione, argomenti, materiali, valutazioni ricevute
 *  - al solo tutor proprietario: l'elenco COMPLETO delle richieste (non solo
 *    quelle in attesa) e il form per allegare un materiale didattico
 *
 * Utilizza quattro route del back-end:
 *   GET  /api/sessioni/{id}
 *   GET  /api/sessioni/{id}/materiali
 *   POST /api/sessioni/{id}/materiali
 *   GET  /api/feedback/sessione/{id}
 *   GET  /api/prenotazioni/sessione/{id}   (solo per il tutor)
 */
interface DettaglioSessioneProps {
  sessioneId: number;
  utenteId: number;
  onChiudi: () => void;
}

const ETICHETTE_PRENOTAZIONE: Record<string, string> = {
  IN_ATTESA: "In attesa",
  ACCETTATA: "Accettata",
  RIFIUTATA: "Rifiutata",
  RITIRATA: "Ritirata",
};

const CLASSI_PRENOTAZIONE: Record<string, string> = {
  IN_ATTESA: "stato-attesa",
  ACCETTATA: "stato-ok",
  RIFIUTATA: "stato-no",
  RITIRATA: "stato-tenue",
};

function formattaData(dataIso: string): string {
  return new Date(dataIso).toLocaleString("it-IT", {
    weekday: "long",
    day: "numeric",
    month: "long",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function DettaglioSessione({
  sessioneId,
  utenteId,
  onChiudi,
}: DettaglioSessioneProps): ReactElement {
  const [sessione, setSessione] = useState<Sessione | null>(null);
  const [materiali, setMateriali] = useState<MaterialeDidattico[]>([]);
  const [valutazioni, setValutazioni] = useState<Feedback[]>([]);
  const [richieste, setRichieste] = useState<Prenotazione[]>([]);
  const [inCaricamento, setInCaricamento] = useState<boolean>(true);
  const [errore, setErrore] = useState<string>("");

  // campi del form di aggiunta materiale (visibile solo al tutor)
  const [titoloMateriale, setTitoloMateriale] = useState<string>("");
  const [urlMateriale, setUrlMateriale] = useState<string>("");
  const [formMaterialeAperto, setFormMaterialeAperto] = useState<boolean>(false);

  const carica = useCallback(async () => {
    try {
      const dettaglio = await sessioniApi.dettaglio(sessioneId);
      setSessione(dettaglio);

      const [m, v] = await Promise.all([
        sessioniApi.materiali(sessioneId),
        feedbackApi.diSessione(sessioneId),
      ]);
      setMateriali(m);
      setValutazioni(v);

      // le richieste sono visibili solo al tutor proprietario: il back-end
      // risponderebbe 401 a chiunque altro, quindi non le chiediamo nemmeno
      if (dettaglio.tutor.id === utenteId) {
        const r = await prenotazioniApi.diSessione(sessioneId);
        setRichieste(r);
      }
    } catch (e) {
      setErrore(
        e instanceof ApiError ? e.message : "Impossibile caricare la sessione"
      );
    } finally {
      setInCaricamento(false);
    }
  }, [sessioneId, utenteId]);

  useEffect(() => {
    carica();
  }, [carica]);

  async function aggiungiMateriale() {
    setErrore("");
    if (titoloMateriale.trim() === "" || urlMateriale.trim() === "") {
      setErrore("Titolo e link sono obbligatori");
      return;
    }
    try {
      await sessioniApi.aggiungiMateriale(sessioneId, {
        titolo: titoloMateriale,
        url: urlMateriale,
        tipo: "LINK",
      });
      setTitoloMateriale("");
      setUrlMateriale("");
      setFormMaterialeAperto(false);
      // ricarica i soli materiali: il resto della finestra non cambia
      setMateriali(await sessioniApi.materiali(sessioneId));
    } catch (e) {
      setErrore(
        e instanceof ApiError ? e.message : "Impossibile aggiungere il materiale"
      );
    }
  }

  const sonoIlTutor = sessione !== null && sessione.tutor.id === utenteId;

  return (
    <div className="sovrapposizione" onClick={onChiudi}>
      <div className="modale modale-larga" onClick={(e) => e.stopPropagation()}>
        {inCaricamento || sessione === null ? (
          <p className="stato-tenue">Caricamento…</p>
        ) : (
          <>
            <h2 className="modale-titolo">{sessione.titolo}</h2>
            <p className="modale-sottotitolo">
              {sessione.tutor.nome} {sessione.tutor.cognome} ·{" "}
              {sessione.materia.nome}
            </p>

            {sessione.descrizione && (
              <p className="dettaglio-descrizione">{sessione.descrizione}</p>
            )}

            <div className="dettaglio-righe">
              <div className="dettaglio-riga">
                <span className="dettaglio-chiave">Quando</span>
                <span>{formattaData(sessione.dataOra)}</span>
              </div>
              <div className="dettaglio-riga">
                <span className="dettaglio-chiave">Dove</span>
                <span>
                  {sessione.modalita === "ONLINE"
                    ? "Online — " + (sessione.luogo || "link comunicato agli iscritti")
                    : sessione.luogo}
                </span>
              </div>
              <div className="dettaglio-riga">
                <span className="dettaglio-chiave">Posti</span>
                <span>
                  {sessione.postiOccupati} occupati su {sessione.postiTotali}
                </span>
              </div>
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

            {/* ------------------------------------------- MATERIALI */}
            <h3 className="sezione-titolo">Materiali didattici</h3>
            {materiali.length === 0 ? (
              <p className="messaggio-vuoto-piccolo">
                Nessun materiale allegato.
              </p>
            ) : (
              <ul className="elenco-materiali">
                {materiali.map((m) => (
                  <li key={m.id}>
                    <a href={m.url} target="_blank" rel="noreferrer" className="link-materiale">
                      {m.titolo}
                    </a>
                  </li>
                ))}
              </ul>
            )}

            {sonoIlTutor && !formMaterialeAperto && (
              <button
                className="bottone-testo"
                onClick={() => setFormMaterialeAperto(true)}
              >
                Aggiungi un materiale
              </button>
            )}

            {sonoIlTutor && formMaterialeAperto && (
              <div className="form-inline">
                <label className="campo-etichetta" htmlFor="titolo-mat">
                  Titolo
                </label>
                <input
                  id="titolo-mat"
                  className="campo-input"
                  type="text"
                  value={titoloMateriale}
                  onChange={(e) => setTitoloMateriale(e.target.value)}
                  placeholder="Es. Esercizi svolti"
                />
                <label className="campo-etichetta" htmlFor="url-mat">
                  Link
                </label>
                <input
                  id="url-mat"
                  className="campo-input"
                  type="text"
                  value={urlMateriale}
                  onChange={(e) => setUrlMateriale(e.target.value)}
                  placeholder="https://…"
                />
                <div className="riga-bottoni">
                  <button className="bottone-primario bottone-stretto" onClick={aggiungiMateriale}>
                    Allega
                  </button>
                  <button
                    className="bottone-secondario"
                    onClick={() => setFormMaterialeAperto(false)}
                  >
                    Annulla
                  </button>
                </div>
              </div>
            )}

            {/* ------------------------- RICHIESTE (solo tutor) */}
            {sonoIlTutor && (
              <>
                <h3 className="sezione-titolo">
                  Tutte le richieste ricevute
                </h3>
                {richieste.length === 0 ? (
                  <p className="messaggio-vuoto-piccolo">
                    Nessuna richiesta per questa sessione.
                  </p>
                ) : (
                  <ul className="elenco">
                    {richieste.map((r) => (
                      <li key={r.id} className="elenco-voce">
                        <div className="elenco-voce-testa">
                          <span className="elenco-voce-titolo">
                            {r.studente.nome} {r.studente.cognome}
                          </span>
                          <span className={CLASSI_PRENOTAZIONE[r.stato]}>
                            {ETICHETTE_PRENOTAZIONE[r.stato]}
                          </span>
                        </div>
                        {r.messaggio && <p className="citazione">{r.messaggio}</p>}
                      </li>
                    ))}
                  </ul>
                )}
              </>
            )}

            {/* ------------------------------------------ VALUTAZIONI */}
            <h3 className="sezione-titolo">Valutazioni</h3>
            {valutazioni.length === 0 ? (
              <p className="messaggio-vuoto-piccolo">
                Questa sessione non ha ancora valutazioni.
              </p>
            ) : (
              <ul className="elenco">
                {valutazioni.map((v) => (
                  <li key={v.id} className="elenco-voce">
                    <div className="elenco-voce-testa">
                      <span className="elenco-voce-titolo">{v.studenteNome}</span>
                      <span className="voto-piccolo">{v.voto}/5</span>
                    </div>
                    {v.commento && <p className="citazione">{v.commento}</p>}
                  </li>
                ))}
              </ul>
            )}

            {errore !== "" && <p className="campo-errore">{errore}</p>}

            <div className="riga-bottoni">
              <button className="bottone-secondario" onClick={onChiudi}>
                Chiudi
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
