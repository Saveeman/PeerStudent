import type { ReactElement } from "react";
import type { Materia } from "../types";

/**
 * Filtro per materia.
 *
 * Componente "controllato": non possiede lo stato della selezione, lo riceve
 * come prop (materiaSelezionata) e comunica i cambiamenti al genitore tramite
 * la callback onCambiaMateria.
 *
 * E' cosi' che si realizza la comunicazione fra componenti fratelli: questo
 * componente e la lista delle sessioni non si conoscono, ma il genitore che li
 * contiene riceve l'evento da qui e aggiorna le props dell'altro.
 */
interface FiltroMaterieProps {
  materie: Materia[];
  materiaSelezionata: number | null;
  onCambiaMateria: (materiaId: number | null) => void;
}

export function FiltroMaterie({
  materie,
  materiaSelezionata,
  onCambiaMateria,
}: FiltroMaterieProps): ReactElement {
  return (
    <div className="filtro-materie">
      <span className="filtro-etichetta">Materia:</span>

      <button
        className={
          materiaSelezionata === null
            ? "chip chip-attivo"
            : "chip"
        }
        onClick={() => onCambiaMateria(null)}
      >
        Tutte
      </button>

      {materie.map((materia) => (
        <button
          key={materia.id}
          className={
            materiaSelezionata === materia.id
              ? "chip chip-attivo"
              : "chip"
          }
          onClick={() => onCambiaMateria(materia.id)}
        >
          {materia.nome}
        </button>
      ))}
    </div>
  );
}
