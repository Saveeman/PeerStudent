package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Long> {

    Optional<Materia> findByCodiceEsame(String codiceEsame);

    List<Materia> findByNomeContainsIgnoreCaseOrderByNomeAsc(String nome);

    List<Materia> findByAnnoCorsoOrderByNomeAsc(Integer annoCorso);
}
