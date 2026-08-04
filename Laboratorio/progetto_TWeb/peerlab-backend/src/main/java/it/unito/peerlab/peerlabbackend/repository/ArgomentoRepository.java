package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Argomento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArgomentoRepository extends JpaRepository<Argomento, Long> {

    List<Argomento> findByMateriaIdOrderByNomeAsc(Long materiaId);

    List<Argomento> findByNomeContainsIgnoreCase(String nome);
}
