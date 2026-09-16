package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Ruolo;
import it.unito.peerlab.peerlabbackend.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtenteRepository extends JpaRepository<Utente, Long> {

    Optional<Utente> findByUsername(String username);

    Optional<Utente> findByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);

    List<Utente> findByRuolo(Ruolo ruolo);
}
