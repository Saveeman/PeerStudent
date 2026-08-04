import { useState } from "react";
import type { ReactElement } from "react";

/**
 * Campo di ricerca testuale per titolo di sessione.
 *
 * Componente controllato: possiede lo stato del testo digitato, ma comunica al
 * genitore solo quando l'utente conferma la ricerca. Il genitore usa il valore
 * ricevuto per interrogare il back-end sulla route GET /api/sessioni?q=...
 */
interface BarraRicercaProps {
  onCerca: (testo: string) => void;
  ricercaAttiva: string;
}

export function BarraRicerca({
  onCerca,
  ricercaAttiva,
}: BarraRicercaProps): ReactElement {
  const [testo, setTesto] = useState<string>("");

  function conferma() {
    onCerca(testo.trim());
  }

  function azzera() {
    setTesto("");
    onCerca("");
  }

  return (
    <div className="barra-ricerca">
      <input
        className="campo-input campo-ricerca"
        type="text"
        value={testo}
        placeholder="Cerca fra le sessioni per titolo…"
        onChange={(e) => setTesto(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === "Enter") conferma();
        }}
      />
      <button className="bottone-secondario" onClick={conferma}>
        Cerca
      </button>

      {ricercaAttiva !== "" && (
        <span className="ricerca-attiva">
          Risultati per “{ricercaAttiva}”
          <button className="bottone-testo" onClick={azzera}>
            azzera
          </button>
        </span>
      )}
    </div>
  );
}
