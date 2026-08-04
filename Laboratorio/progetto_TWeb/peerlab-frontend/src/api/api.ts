/**
 * Modulo di accesso alla API del back-end.
 *
 * Tutte le chiamate HTTP dell'applicazione passano da qui: i componenti non
 * usano mai fetch direttamente. In questo modo l'indirizzo del server, la
 * gestione degli errori e l'invio dei cookie sono definiti in un solo punto.
 */

import type {
  Argomento,
  Feedback,
  LoginRequest,
  Materia,
  MaterialeDidattico,
  NuovaPrenotazioneRequest,
  NuovaSessioneRequest,
  NuovoFeedbackRequest,
  NuovoMaterialeRequest,
  Prenotazione,
  Sessione,
  Utente,
} from "../types";

/** Origine del back-end: porta diversa da quella del front-end (5173). */
const BASE_URL = "http://localhost:8080/api";

/**
 * Errore applicativo che porta con se' lo status HTTP e il messaggio
 * restituito dal back-end, cosi' i componenti possono mostrarlo all'utente.
 */
export class ApiError extends Error {
  status: number;

  constructor(status: number, messaggio: string) {
    super(messaggio);
    this.status = status;
  }
}

/**
 * Funzione di supporto usata da tutte le altre.
 *
 * Punti importanti:
 *  - credentials: "include" -> il browser invia il cookie di sessione anche se
 *    la richiesta e' cross-origin (di default non lo farebbe);
 *  - Content-Type: application/json quando c'e' un body da serializzare;
 *  - una risposta con status di errore NON fa fallire la fetch: la Promise si
 *    risolve comunque. Va quindi controllato esplicitamente res.ok.
 */
async function richiesta<T>(
  percorso: string,
  opzioni: RequestInit = {}
): Promise<T> {
  const risposta = await fetch(BASE_URL + percorso, {
    ...opzioni,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...opzioni.headers,
    },
  });

  if (!risposta.ok) {
    // il back-end restituisce { "errore": "..." } tramite GestoreErrori
    let messaggio = "Si e' verificato un errore";
    try {
      const corpo = await risposta.json();
      if (corpo && corpo.errore) {
        messaggio = corpo.errore;
      }
    } catch {
      // la risposta non conteneva JSON: teniamo il messaggio generico
    }
    throw new ApiError(risposta.status, messaggio);
  }

  // 204 No Content oppure body vuoto (es. logout)
  if (risposta.status === 204) {
    return undefined as T;
  }
  const testo = await risposta.text();
  return testo ? (JSON.parse(testo) as T) : (undefined as T);
}

// ------------------------------------------------------------ AUTENTICAZIONE

export const authApi = {
  login: (credenziali: LoginRequest): Promise<Utente> =>
    richiesta<Utente>("/auth/login", {
      method: "POST",
      body: JSON.stringify(credenziali),
    }),

  logout: (): Promise<void> =>
    richiesta<void>("/auth/logout", { method: "POST" }),

  /** Chi e' l'utente collegato? Risponde 401 se nessuno ha fatto login. */
  utenteCorrente: (): Promise<Utente> => richiesta<Utente>("/auth/me"),
};

// ------------------------------------------------------------------ MATERIE

export const catalogoApi = {
  materie: (): Promise<Materia[]> => richiesta<Materia[]>("/materie"),

  materia: (id: number): Promise<Materia> => richiesta<Materia>(`/materie/${id}`),

  argomenti: (materiaId: number): Promise<Argomento[]> =>
    richiesta<Argomento[]>(`/materie/${materiaId}/argomenti`),
};

// ----------------------------------------------------------------- SESSIONI

export const sessioniApi = {
  /** Elenco delle sessioni aperte, con filtro opzionale per materia o testo. */
  elenco: (materiaId?: number, ricerca?: string): Promise<Sessione[]> => {
    const parametri = new URLSearchParams();
    if (materiaId) parametri.set("materiaId", String(materiaId));
    if (ricerca) parametri.set("q", ricerca);
    const queryString = parametri.toString();
    return richiesta<Sessione[]>(`/sessioni${queryString ? "?" + queryString : ""}`);
  },

  dettaglio: (id: number): Promise<Sessione> => richiesta<Sessione>(`/sessioni/${id}`),

  delTutor: (tutorId: number): Promise<Sessione[]> =>
    richiesta<Sessione[]>(`/sessioni/tutor/${tutorId}`),

  crea: (dati: NuovaSessioneRequest): Promise<Sessione> =>
    richiesta<Sessione>("/sessioni", {
      method: "POST",
      body: JSON.stringify(dati),
    }),

  chiudi: (id: number): Promise<Sessione> =>
    richiesta<Sessione>(`/sessioni/${id}/chiudi`, { method: "POST" }),

  completa: (id: number): Promise<Sessione> =>
    richiesta<Sessione>(`/sessioni/${id}/completa`, { method: "POST" }),

  annulla: (id: number): Promise<Sessione> =>
    richiesta<Sessione>(`/sessioni/${id}/annulla`, { method: "POST" }),

  /** Materiali didattici allegati a una sessione. */
  materiali: (id: number): Promise<MaterialeDidattico[]> =>
    richiesta<MaterialeDidattico[]>(`/sessioni/${id}/materiali`),

  /** Allega un materiale a una sessione (solo il tutor proprietario). */
  aggiungiMateriale: (
    id: number,
    dati: NuovoMaterialeRequest
  ): Promise<MaterialeDidattico> =>
    richiesta<MaterialeDidattico>(`/sessioni/${id}/materiali`, {
      method: "POST",
      body: JSON.stringify(dati),
    }),
};

// ------------------------------------------------------------- PRENOTAZIONI

export const prenotazioniApi = {
  crea: (dati: NuovaPrenotazioneRequest): Promise<Prenotazione> =>
    richiesta<Prenotazione>("/prenotazioni", {
      method: "POST",
      body: JSON.stringify(dati),
    }),

  /** Le prenotazioni dell'utente collegato (vista studente). */
  mie: (): Promise<Prenotazione[]> => richiesta<Prenotazione[]>("/prenotazioni/mie"),

  /** Le richieste in attesa rivolte al tutor collegato (vista tutor). */
  richiesteInAttesa: (): Promise<Prenotazione[]> =>
    richiesta<Prenotazione[]>("/prenotazioni/richieste"),

  diSessione: (sessioneId: number): Promise<Prenotazione[]> =>
    richiesta<Prenotazione[]>(`/prenotazioni/sessione/${sessioneId}`),

  accetta: (id: number): Promise<Prenotazione> =>
    richiesta<Prenotazione>(`/prenotazioni/${id}/accetta`, { method: "POST" }),

  rifiuta: (id: number): Promise<Prenotazione> =>
    richiesta<Prenotazione>(`/prenotazioni/${id}/rifiuta`, { method: "POST" }),

  ritira: (id: number): Promise<Prenotazione> =>
    richiesta<Prenotazione>(`/prenotazioni/${id}/ritira`, { method: "POST" }),
};

// ----------------------------------------------------------------- FEEDBACK

export const feedbackApi = {
  crea: (dati: NuovoFeedbackRequest): Promise<Feedback> =>
    richiesta<Feedback>("/feedback", {
      method: "POST",
      body: JSON.stringify(dati),
    }),

  delTutor: (tutorId: number): Promise<Feedback[]> =>
    richiesta<Feedback[]>(`/feedback/tutor/${tutorId}`),

  mediaTutor: (tutorId: number): Promise<number> =>
    richiesta<number>(`/feedback/tutor/${tutorId}/media`),

  diSessione: (sessioneId: number): Promise<Feedback[]> =>
    richiesta<Feedback[]>(`/feedback/sessione/${sessioneId}`),
};
