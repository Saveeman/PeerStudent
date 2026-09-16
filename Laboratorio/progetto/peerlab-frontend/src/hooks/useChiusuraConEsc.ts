import { useEffect } from "react";

/**
 * Hook personalizzato: chiude una finestra modale alla pressione di Esc.
 *
 * E' un custom hook, cioe' una funzione costruita a partire dagli hook
 * esistenti di React (qui useEffect) e riutilizzabile da piu' componenti.
 *
 * L'ascoltatore viene registrato su window quando il componente compare e
 * rimosso dalla funzione di cleanup quando scompare: senza quella rimozione
 * ogni apertura di una modale lascerebbe dietro di se' un ascoltatore attivo,
 * che continuerebbe a rispondere ai tasti pur riferendosi a un componente non
 * piu' montato.
 */
export function useChiusuraConEsc(onChiudi: () => void): void {
  useEffect(() => {
    function gestisciTasto(evento: KeyboardEvent) {
      if (evento.key === "Escape") {
        onChiudi();
      }
    }

    window.addEventListener("keydown", gestisciTasto);

    return () => {
      window.removeEventListener("keydown", gestisciTasto);
    };
  }, [onChiudi]);
}
