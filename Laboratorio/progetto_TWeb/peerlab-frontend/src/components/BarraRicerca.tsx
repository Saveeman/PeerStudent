import { useState } from "react";
import type { ReactElement } from "react";

/**
 * Campo di ricerca testuale con suggerimenti.
 *
 * Mentre l'utente digita, il componente filtra localmente l'elenco dei titoli
 * ricevuto dal genitore e mostra i primi risultati compatibili. I suggerimenti
 * non richiedono richieste al server: sono calcolati sui dati gia' disponibili.
 *
 * La ricerca vera e propria (che interroga il back-end sulla route
 * GET /api/sessioni?q=...) parte solo alla conferma.
 */
interface BarraRicercaProps {
  onCerca: (testo: string) => void;
  suggerimenti: string[];
}

export function BarraRicerca({
  onCerca,
  suggerimenti,
}: BarraRicercaProps): ReactElement {
  const [testo, setTesto] = useState<string>("");
  const [tendinaAperta, setTendinaAperta] = useState<boolean>(false);

  /* Suggerimenti compatibili con quanto digitato: al massimo cinque, per non
     coprire la pagina. Il confronto ignora maiuscole e minuscole. */
  const compatibili =
    testo.trim() === ""
      ? []
      : suggerimenti
          .filter((t) => t.toLowerCase().includes(testo.trim().toLowerCase()))
          .slice(0, 5);

  function conferma(valore?: string) {
    const daCercare = (valore ?? testo).trim();
    setTesto(daCercare);
    setTendinaAperta(false);
    onCerca(daCercare);
  }


  return (
    <div className="barra-ricerca">
      <div className="ricerca-campo">
        <input
          className="campo-input campo-ricerca"
          type="text"
          value={testo}
          placeholder="Cerca fra gli appuntamenti…"
          onChange={(e) => {
            const valore = e.target.value;
            setTesto(valore);
            setTendinaAperta(true);
            /* svuotando il campo si torna automaticamente all'elenco completo,
               senza bisogno di un pulsante dedicato */
            if (valore.trim() === "") {
              onCerca("");
            }
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") conferma();
            if (e.key === "Escape") setTendinaAperta(false);
          }}
          /* il ritardo evita che la tendina si chiuda prima che il click su un
             suggerimento venga registrato */
          onBlur={() => setTimeout(() => setTendinaAperta(false), 150)}
          onFocus={() => setTendinaAperta(true)}
        />

        {tendinaAperta && compatibili.length > 0 && (
          <ul className="tendina-suggerimenti">
            {compatibili.map((s) => (
              <li key={s}>
                <button
                  className="suggerimento"
                  onClick={() => conferma(s)}
                >
                  {s}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <button className="bottone-secondario" onClick={() => conferma()}>
        Cerca
      </button>

    </div>
  );
}
