package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Prenotazione;
import it.unito.peerlab.peerlabbackend.model.StatoPrenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {

    List<Prenotazione> findByStudenteIdOrderByDataRichiestaDesc(Long studenteId);

    List<Prenotazione> findByStudenteIdAndStatoOrderByDataRichiestaDesc(Long studenteId, StatoPrenotazione stato);

    List<Prenotazione> findBySessioneIdOrderByDataRichiestaAsc(Long sessioneId);

    List<Prenotazione> findBySessioneIdAndStatoOrderByDataRichiestaAsc(Long sessioneId, StatoPrenotazione stato);

    Optional<Prenotazione> findByStudenteIdAndSessioneId(Long studenteId, Long sessioneId);

    boolean existsByStudenteIdAndSessioneId(Long studenteId, Long sessioneId);

    long countBySessioneIdAndStato(Long sessioneId, StatoPrenotazione stato);

    List<Prenotazione> findBySessioneTutorIdAndStatoOrderByDataRichiestaAsc(Long tutorId, StatoPrenotazione stato);
}
