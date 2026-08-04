import { useEffect, useState } from "react";
import type { ReactElement } from "react";
import { feedbackApi } from "../api/api";
import type { Feedback, Utente } from "../types";

/**
 * Finestra modale con il profilo di un tutor e la sua reputazione.
 *
 * Effettua due richieste distinte al back-end:
 *  - GET /api/feedback/tutor/{id}/media  -> la media dei voti
 *  - GET /api/feedback/tutor/{id}        -> le singole valutazioni ricevute
 *
 * E' il componente che da' senso alle valutazioni: senza, gli studenti
 * potrebbero lasciarle ma nessuno le leggerebbe mai.
 */
interface ProfiloTutorProps {
  tutor: Utente;
  onChiudi: () => void;
}

function formattaData(dataIso: string): string {
  return new Date(dataIso).toLocaleDateString("it-IT", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

export function ProfiloTutor({ tutor, onChiudi }: ProfiloTutorProps): ReactElement {
  const [media, setMedia] = useState<number | null>(null);
  const [valutazioni, setValutazioni] = useState<Feedback[]>([]);
  const [inCaricamento, setInCaricamento] = useState<boolean>(true);

  /**
   * Le due richieste partono insieme con Promise.all: si attende il
   * completamento di entrambe invece di eseguirle in sequenza.
   */
  useEffect(() => {
    let annullato = false;

    Promise.all([
      feedbackApi.mediaTutor(tutor.id),
      feedbackApi.delTutor(tutor.id),
    ])
      .then(([m, v]) => {
        if (!annullato) {
          setMedia(m);
          setValutazioni(v);
        }
      })
      .catch(() => {
        // profilo mostrato comunque, senza reputazione
      })
      .finally(() => {
        if (!annullato) setInCaricamento(false);
      });

    return () => {
      annullato = true;
    };
  }, [tutor.id]);

  return (
    <div className="sovrapposizione" onClick={onChiudi}>
      <div className="modale" onClick={(e) => e.stopPropagation()}>
        <h2 className="modale-titolo">
          {tutor.nome} {tutor.cognome}
        </h2>
        <p className="modale-sottotitolo">{tutor.email}</p>

        {tutor.bio && <p className="profilo-bio">{tutor.bio}</p>}

        <div className="profilo-reputazione">
          {inCaricamento ? (
            <span className="stato-tenue">Caricamento reputazione…</span>
          ) : valutazioni.length === 0 ? (
            <span className="stato-tenue">
              Non ha ancora ricevuto valutazioni
            </span>
          ) : (
            <>
              <span className="profilo-media">{media?.toFixed(1)}</span>
              <span className="stato-tenue">
                su 5 — {valutazioni.length}{" "}
                {valutazioni.length === 1 ? "valutazione" : "valutazioni"}
              </span>
            </>
          )}
        </div>

        {valutazioni.length > 0 && (
          <ul className="elenco elenco-valutazioni">
            {valutazioni.map((v) => (
              <li key={v.id} className="elenco-voce">
                <div className="elenco-voce-testa">
                  <span className="elenco-voce-titolo">{v.sessioneTitolo}</span>
                  <span className="voto-piccolo">{v.voto}/5</span>
                </div>
                {v.commento && <p className="citazione">{v.commento}</p>}
                <p className="elenco-voce-meta">
                  {v.studenteNome} · {formattaData(v.data)}
                </p>
              </li>
            ))}
          </ul>
        )}

        <div className="riga-bottoni">
          <button className="bottone-secondario" onClick={onChiudi}>
            Chiudi
          </button>
        </div>
      </div>
    </div>
  );
}
