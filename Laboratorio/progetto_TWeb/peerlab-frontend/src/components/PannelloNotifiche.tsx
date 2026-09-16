import type { ReactElement } from "react";
import type { Avviso, Prenotazione } from "../types";

/**
 * Pannello delle notifiche dello studente.
 *
 * Raccoglie tutto cio' che lo studente "riceve" dopo essere stato ammesso a un
 * appuntamento:
 *  - il link della videochiamata, per gli appuntamenti online;
 *  - gli avvisi inviati dal tutor ai partecipanti.
 *
 * E' distinto dal pannello delle richieste, che invece contiene cio' che lo
 * studente deve gestire (ritirare una richiesta, lasciare una valutazione).
 */
interface PannelloNotificheProps {
  prenotazioni: Prenotazione[];
  avvisi: Avviso[];
}

function formattaQuando(dataIso: string): string {
  const data = new Date(dataIso);
  const minutiFa = Math.floor((Date.now() - data.getTime()) / 60000);

  if (minutiFa < 60) {
    return minutiFa <= 1 ? "poco fa" : `${minutiFa} minuti fa`;
  }
  const oreFa = Math.floor(minutiFa / 60);
  if (oreFa < 24) {
    return oreFa === 1 ? "un'ora fa" : `${oreFa} ore fa`;
  }
  return data.toLocaleDateString("it-IT", { day: "numeric", month: "long" });
}

export function PannelloNotifiche({
  prenotazioni,
  avvisi,
}: PannelloNotificheProps): ReactElement {
  /* I link arrivano dalle prenotazioni accettate su appuntamenti online: il
     back-end valorizza linkIncontro solo in quel caso. */
  const conLink = prenotazioni.filter((p) => p.linkIncontro);

  const totale = conLink.length + avvisi.length;

  return (
    <aside className="pannello">
      <h2 className="pannello-titolo">
        Notifiche
        {totale > 0 && <span className="contatore">{totale}</span>}
      </h2>

      {totale === 0 ? (
        <p className="messaggio-vuoto-piccolo">
          Non hai notifiche. Qui riceverai i link delle videochiamate e gli
          avvisi dei tutor.
        </p>
      ) : (
        <ul className="elenco">
          {conLink.map((p) => (
            <li key={"link-" + p.id} className="elenco-voce">
              <div className="elenco-voce-testa">
                <span className="tipo-notifica tipo-notifica-link">
                  Videochiamata
                </span>
              </div>
              <p className="elenco-voce-meta">{p.sessioneTitolo}</p>
              <div className="riquadro-link">
                <span className="riquadro-link-titolo">
                  Link della videochiamata
                </span>
                <a
                  href={p.linkIncontro ?? "#"}
                  target="_blank"
                  rel="noreferrer"
                  className="riquadro-link-indirizzo"
                >
                  {p.linkIncontro}
                </a>
              </div>
            </li>
          ))}

          {avvisi.map((a) => (
            <li key={"avviso-" + a.id} className="elenco-voce">
              <div className="elenco-voce-testa">
                <span className="tipo-notifica tipo-notifica-avviso">
                  Avviso
                </span>
                <span className="stato-tenue">{formattaQuando(a.data)}</span>
              </div>
              <p className="elenco-voce-meta">
                {a.sessioneTitolo} · {a.tutorNome}
              </p>
              <p className="citazione">{a.testo}</p>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
}
