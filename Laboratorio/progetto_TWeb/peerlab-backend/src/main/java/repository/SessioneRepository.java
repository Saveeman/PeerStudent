package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Sessione;
import it.unito.peerlab.peerlabbackend.model.StatoSessione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessioneRepository extends JpaRepository<Sessione, Long> {

    List<Sessione> findByStatoOrderByDataOraAsc(StatoSessione stato);

    List<Sessione> findByMateriaIdAndStatoOrderByDataOraAsc(Long materiaId, StatoSessione stato);

    List<Sessione> findByMateriaIdOrderByDataOraAsc(Long materiaId);

    List<Sessione> findByTutorIdOrderByDataOraDesc(Long tutorId);

    List<Sessione> findByTutorIdAndStatoOrderByDataOraDesc(Long tutorId, StatoSessione stato);

    List<Sessione> findByTitoloContainsIgnoreCaseOrderByDataOraAsc(String titolo);

    List<Sessione> findByDataOraAfterAndStatoOrderByDataOraAsc(LocalDateTime dataOra, StatoSessione stato);

    long countByTutorIdAndStato(Long tutorId, StatoSessione stato);
}
