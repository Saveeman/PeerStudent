package it.unito.peerlab.peerlabbackend.repository;

import it.unito.peerlab.peerlabbackend.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByPrenotazioneId(Long prenotazioneId);

    boolean existsByPrenotazioneId(Long prenotazioneId);

    List<Feedback> findByPrenotazioneSessioneTutorIdOrderByDataDesc(Long tutorId);

    List<Feedback> findByPrenotazioneSessioneIdOrderByDataDesc(Long sessioneId);
}
