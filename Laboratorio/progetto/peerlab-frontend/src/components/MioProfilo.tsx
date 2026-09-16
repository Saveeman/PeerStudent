import type { ReactElement } from "react";
import { useChiusuraConEsc } from "../hooks/useChiusuraConEsc";
import type { Utente } from "../types";

/**
 * Scheda con i dati personali dell'utente collegato.
 *
 * Si apre cliccando sul proprio nome nell'intestazione. Non effettua richieste
 * al server: mostra i dati dell'utente gia' presenti nello stato di App,
 * ottenuti al momento del login (o al ricaricamento della pagina, tramite
 * GET /api/auth/me).
 */
interface MioProfiloProps {
  utente: Utente;
  onChiudi: () => void;
}

function formattaData(dataIso: string | null): string {
  if (!dataIso) return "non indicata";
  return new Date(dataIso).toLocaleDateString("it-IT", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

export function MioProfilo({ utente, onChiudi }: MioProfiloProps): ReactElement {
  // chiusura con il tasto Esc, oltre che con il click fuori
  useChiusuraConEsc(onChiudi);

  const iniziali =
    utente.nome.charAt(0).toUpperCase() + utente.cognome.charAt(0).toUpperCase();

  return (
    <div className="sovrapposizione" onClick={onChiudi}>
      <div className="modale"
        role="dialog"
        aria-modal="true"
        aria-labelledby="titolo-mio-profilo"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="profilo-intestazione">
          <div className="avatar">{iniziali}</div>
          <div>
            <h2 className="modale-titolo" id="titolo-mio-profilo">
              {utente.nome} {utente.cognome}
            </h2>
            <span className="badge-ruolo">
              {utente.ruolo === "TUTOR" ? "tutor" : "studente"}
            </span>
          </div>
        </div>

        <div className="dettaglio-righe profilo-dati">
          <div className="dettaglio-riga">
            <span className="dettaglio-chiave">Nome</span>
            <span>{utente.nome}</span>
          </div>
          <div className="dettaglio-riga">
            <span className="dettaglio-chiave">Cognome</span>
            <span>{utente.cognome}</span>
          </div>
          <div className="dettaglio-riga">
            <span className="dettaglio-chiave">Eta'</span>
            <span>
              {utente.eta !== null ? `${utente.eta} anni` : "non indicata"}
            </span>
          </div>
          <div className="dettaglio-riga">
            <span className="dettaglio-chiave">Nascita</span>
            <span>{formattaData(utente.dataNascita)}</span>
          </div>
          <div className="dettaglio-riga">
            <span className="dettaglio-chiave">Matricola</span>
            <span>{utente.matricola ?? "non assegnata"}</span>
          </div>
          <div className="dettaglio-riga">
            <span className="dettaglio-chiave">Email</span>
            <span>{utente.email}</span>
          </div>
        </div>

        <div className="riga-bottoni">
          <button className="bottone-secondario" onClick={onChiudi}>
            Chiudi
          </button>
        </div>
      </div>
    </div>
  );
}
