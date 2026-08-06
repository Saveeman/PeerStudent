package it.unito.peerlab.peerlabbackend.dto;

import it.unito.peerlab.peerlabbackend.model.ModalitaSessione;

import java.time.LocalDateTime;
import java.util.List;

/** Body della POST /api/sessioni (creazione di una sessione da parte di un tutor) */
public record NuovaSessioneRequest(
        String titolo,
        String descrizione,
        LocalDateTime dataOra,
        String luogo,
        String linkIncontro,
        ModalitaSessione modalita,
        Integer postiTotali,
        Long materiaId,
        List<Long> argomentiIds
) {
}
