package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.MaterialeDidattico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialeDidatticoRepository extends JpaRepository<MaterialeDidattico, Long> {

    List<MaterialeDidattico> findBySessioneIdOrderByTitoloAsc(Long sessioneId);

    void deleteBySessioneId(Long sessioneId);
}
