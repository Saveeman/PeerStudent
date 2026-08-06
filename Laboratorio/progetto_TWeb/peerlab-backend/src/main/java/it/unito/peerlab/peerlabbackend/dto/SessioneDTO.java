package it.unito.peerlab.peerlabbackend.dto;

import it.unito.peerlab.peerlabbackend.model.ModalitaSessione;
import it.unito.peerlab.peerlabbackend.model.StatoSessione;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista "piatta" di una Sessione.
 * Nota: postiOccupati e postiDisponibili non esistono nell'entita': sono
 * calcolati dal service contando le prenotazioni accettate.
 *
 * linkIncontro e' valorizzato solo se chi ha effettuato la richiesta ha il
 * diritto di vederlo (il tutor proprietario o uno studente ammesso): per tutti
 * gli altri vale null e non compare nemmeno nel JSON inviato. E' un esempio di
 * informazione che il front-end vuole ma che il modello dati non memorizza.
 */
public record SessioneDTO(
        Long id,
        String titolo,
        String descrizione,
        LocalDateTime dataOra,
        String luogo,
        String linkIncontro,
        ModalitaSessione modalita,
        Integer postiTotali,
        long postiOccupati,
        long postiDisponibili,
        StatoSessione stato,
        UtenteDTO tutor,
        MateriaDTO materia,
        List<ArgomentoDTO> argomenti,
        List<MaterialeDidatticoDTO> materiali
) {
}
