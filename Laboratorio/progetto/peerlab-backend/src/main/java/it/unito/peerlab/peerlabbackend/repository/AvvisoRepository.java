package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Avviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AvvisoRepository extends JpaRepository<Avviso, Long> {

    /** Avvisi di un singolo appuntamento, dal piu' recente. */
    List<Avviso> findBySessioneIdOrderByDataDesc(Long sessioneId);

    /**
     * Avvisi relativi a un insieme di appuntamenti: e' la query che alimenta le
     * notifiche dello studente, a partire dagli appuntamenti a cui e' ammesso.
     */
    List<Avviso> findBySessioneIdInOrderByDataDesc(Collection<Long> sessioniIds);

    /** Avvisi inviati da un tutor su qualunque suo appuntamento. */
    List<Avviso> findBySessioneTutorIdOrderByDataDesc(Long tutorId);
}
