/**
 * Tipi TypeScript che rispecchiano i DTO del back-end.
 *
 * Sono definiti come interface: descrivono la "forma" degli oggetti JSON che
 * arrivano dal server. Non esistono a run-time (spariscono con la
 * compilazione), ma permettono al compilatore e all'IDE di verificare che i
 * dati vengano usati correttamente.
 */

// ----------------------------------------------------------------- ENUM
// Corrispondono agli enum Java del back-end.

export type Ruolo = "STUDENTE" | "TUTOR";

export type ModalitaSessione = "PRESENZA" | "ONLINE";

export type StatoSessione = "APERTA" | "CHIUSA" | "COMPLETATA" | "ANNULLATA";

export type StatoPrenotazione = "IN_ATTESA" | "ACCETTATA" | "RIFIUTATA" | "RITIRATA";

// -------------------------------------------------------- DTO DI RISPOSTA

export interface Utente {
  id: number;
  nome: string;
  cognome: string;
  username: string;
  email: string;
  matricola: string | null;
  ruolo: Ruolo;
  dataNascita: string | null;
  eta: number | null;
  bio: string | null;
}

export interface Materia {
  id: number;
  nome: string;
  codiceEsame: string | null;
  annoCorso: number | null;
}

export interface Argomento {
  id: number;
  nome: string;
  materiaId: number | null;
  materiaNome: string | null;
}

export interface MaterialeDidattico {
  id: number;
  titolo: string;
  url: string;
  tipo: string | null;
}

export interface Sessione {
  id: number;
  titolo: string;
  descrizione: string | null;
  dataOra: string;          // il back-end invia una data ISO come stringa
  luogo: string | null;
  /** Link della videochiamata: valorizzato solo se l'utente ha diritto di vederlo. */
  linkIncontro: string | null;
  modalita: ModalitaSessione;
  postiTotali: number;
  postiOccupati: number;
  postiDisponibili: number;
  stato: StatoSessione;
  tutor: Utente;
  materia: Materia;
  argomenti: Argomento[];
  materiali: MaterialeDidattico[];
}

export interface Prenotazione {
  id: number;
  stato: StatoPrenotazione;
  dataRichiesta: string;
  messaggio: string | null;
  emailContatto: string | null;
  studente: Utente;
  sessioneId: number;
  sessioneTitolo: string;
  sessioneDataOra: string;
  tutorNome: string | null;
  /** Link della videochiamata, presente solo se la richiesta e' stata accettata
      e l'appuntamento si svolge online. */
  linkIncontro: string | null;
  haFeedback: boolean;
}

export interface Feedback {
  id: number;
  voto: number;
  commento: string | null;
  data: string;
  prenotazioneId: number;
  studenteNome: string | null;
  sessioneTitolo: string | null;
}

// -------------------------------------------------------- DTO DI RICHIESTA
// Corrispondono ai body delle POST accettate dal back-end.

export interface Avviso {
  id: number;
  testo: string;
  data: string;
  sessioneId: number;
  sessioneTitolo: string | null;
  tutorNome: string | null;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface NuovaSessioneRequest {
  titolo: string;
  descrizione: string;
  dataOra: string;
  luogo: string;
  linkIncontro: string;
  modalita: ModalitaSessione;
  postiTotali: number;
  materiaId: number;
  argomentiIds: number[];
}

export interface NuovaPrenotazioneRequest {
  sessioneId: number;
  messaggio: string;
  /** Richiesta solo per le sessioni in modalita' ONLINE. */
  emailContatto: string;
}

export interface NuovoFeedbackRequest {
  prenotazioneId: number;
  voto: number;
  commento: string;
}

export interface NuovoAvvisoRequest {
  sessioneId: number;
  testo: string;
}

export interface NuovoMaterialeRequest {
  titolo: string;
  url: string;
  tipo: string;
}
