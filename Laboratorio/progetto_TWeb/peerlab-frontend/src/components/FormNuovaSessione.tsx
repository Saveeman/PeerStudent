import { useEffect, useState } from "react";
import type { ReactElement } from "react";
import { ApiError, catalogoApi, sessioniApi } from "../api/api";
import type { Argomento, Materia, ModalitaSessione } from "../types";

/**
 * Form di creazione di una nuova sessione: e' il primo dei due form che
 * generano una richiesta POST.
 *
 * Ogni campo e' legato a una variabile di stato tramite l'attributo value e
 * l'evento onChange: e' il binding bidirezionale (property binding dallo stato
 * verso la view, event binding dalla view verso lo stato).
 */
interface FormNuovaSessioneProps {
  onCreata: () => void;
  onAnnulla: () => void;
}

export function FormNuovaSessione({
  onCreata,
  onAnnulla,
}: FormNuovaSessioneProps): ReactElement {
  const [materie, setMaterie] = useState<Materia[]>([]);
  const [argomenti, setArgomenti] = useState<Argomento[]>([]);

  const [titolo, setTitolo] = useState<string>("");
  const [descrizione, setDescrizione] = useState<string>("");
  const [dataOra, setDataOra] = useState<string>("");
  const [luogo, setLuogo] = useState<string>("");
  const [modalita, setModalita] = useState<ModalitaSessione>("PRESENZA");
  const [postiTotali, setPostiTotali] = useState<number>(4);
  const [materiaId, setMateriaId] = useState<number | null>(null);
  const [argomentiScelti, setArgomentiScelti] = useState<number[]>([]);

  const [errore, setErrore] = useState<string>("");
  const [inCorso, setInCorso] = useState<boolean>(false);

  /* Le materie servono per il menu a tendina: si caricano una volta sola. */
  useEffect(() => {
    let annullato = false;
    catalogoApi.materie().then((m) => {
      if (!annullato) setMaterie(m);
    });
    return () => {
      annullato = true;
    };
  }, []);

  /* Gli argomenti dipendono dalla materia scelta: l'effetto si riesegue a ogni
     cambio di materiaId. */
  useEffect(() => {
    if (materiaId === null) {
      setArgomenti([]);
      return;
    }
    let annullato = false;
    catalogoApi.argomenti(materiaId).then((a) => {
      if (!annullato) {
        setArgomenti(a);
        setArgomentiScelti([]);
      }
    });
    return () => {
      annullato = true;
    };
  }, [materiaId]);

  function alternaArgomento(id: number) {
    setArgomentiScelti((precedenti) =>
      precedenti.includes(id)
        ? precedenti.filter((x) => x !== id)
        : [...precedenti, id]
    );
  }

  async function invia() {
    setErrore("");

    if (titolo.trim() === "") {
      setErrore("Il titolo e' obbligatorio");
      return;
    }
    if (materiaId === null) {
      setErrore("Scegli una materia");
      return;
    }
    if (dataOra === "") {
      setErrore("Indica data e ora della sessione");
      return;
    }

    setInCorso(true);
    try {
      await sessioniApi.crea({
        titolo,
        descrizione,
        // l'input datetime-local produce "2026-09-10T14:30": il formato che
        // Spring si aspetta per un LocalDateTime
        dataOra: dataOra.length === 16 ? dataOra + ":00" : dataOra,
        luogo,
        modalita,
        postiTotali,
        materiaId,
        argomentiIds: argomentiScelti,
      });
      onCreata();
    } catch (e) {
      setErrore(
        e instanceof ApiError ? e.message : "Impossibile creare la sessione"
      );
    } finally {
      setInCorso(false);
    }
  }

  return (
    <div className="sovrapposizione" onClick={onAnnulla}>
      <div className="modale modale-larga" onClick={(e) => e.stopPropagation()}>
        <h2 className="modale-titolo">Nuova sessione</h2>

        <label className="campo-etichetta" htmlFor="titolo">
          Titolo
        </label>
        <input
          id="titolo"
          className="campo-input"
          type="text"
          value={titolo}
          onChange={(e) => setTitolo(e.target.value)}
          placeholder="Es. Integrali per parti: esercizi d'esame"
        />

        <label className="campo-etichetta" htmlFor="materia">
          Materia
        </label>
        <select
          id="materia"
          className="campo-select"
          value={materiaId ?? ""}
          onChange={(e) =>
            setMateriaId(e.target.value === "" ? null : Number(e.target.value))
          }
        >
          <option value="">Scegli una materia…</option>
          {materie.map((m) => (
            <option key={m.id} value={m.id}>
              {m.nome}
            </option>
          ))}
        </select>

        {argomenti.length > 0 && (
          <>
            <label className="campo-etichetta">Argomenti trattati</label>
            <div className="riga-chip">
              {argomenti.map((a) => (
                <button
                  key={a.id}
                  className={
                    argomentiScelti.includes(a.id) ? "chip chip-attivo" : "chip"
                  }
                  onClick={() => alternaArgomento(a.id)}
                >
                  {a.nome}
                </button>
              ))}
            </div>
          </>
        )}

        <label className="campo-etichetta" htmlFor="descrizione">
          Descrizione
        </label>
        <textarea
          id="descrizione"
          className="campo-textarea"
          value={descrizione}
          onChange={(e) => setDescrizione(e.target.value)}
          placeholder="Cosa farete durante la sessione?"
        />

        <div className="riga-doppia">
          <div>
            <label className="campo-etichetta" htmlFor="dataora">
              Data e ora
            </label>
            <input
              id="dataora"
              className="campo-input"
              type="datetime-local"
              value={dataOra}
              onChange={(e) => setDataOra(e.target.value)}
            />
          </div>
          <div>
            <label className="campo-etichetta" htmlFor="posti">
              Posti disponibili
            </label>
            <input
              id="posti"
              className="campo-input"
              type="number"
              min={1}
              max={30}
              value={postiTotali}
              onChange={(e) => setPostiTotali(Number(e.target.value))}
            />
          </div>
        </div>

        <div className="riga-doppia">
          <div>
            <label className="campo-etichetta" htmlFor="modalita">
              Modalita'
            </label>
            <select
              id="modalita"
              className="campo-select"
              value={modalita}
              onChange={(e) => setModalita(e.target.value as ModalitaSessione)}
            >
              <option value="PRESENZA">In presenza</option>
              <option value="ONLINE">Online</option>
            </select>
          </div>
          <div>
            <label className="campo-etichetta" htmlFor="luogo">
              {modalita === "ONLINE" ? "Link" : "Luogo"}
            </label>
            <input
              id="luogo"
              className="campo-input"
              type="text"
              value={luogo}
              onChange={(e) => setLuogo(e.target.value)}
              placeholder={
                modalita === "ONLINE" ? "Link della videochiamata" : "Es. Aula studio 2"
              }
            />
          </div>
        </div>

        {errore !== "" && <p className="campo-errore">{errore}</p>}

        <div className="riga-bottoni">
          <button className="bottone-primario" onClick={invia} disabled={inCorso}>
            {inCorso ? "Creazione…" : "Crea sessione"}
          </button>
          <button className="bottone-secondario" onClick={onAnnulla}>
            Annulla
          </button>
        </div>
      </div>
    </div>
  );
}
