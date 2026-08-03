package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazioni")
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPrenotazione stato;

    @Column(name = "data_richiesta", nullable = false)
    private LocalDateTime dataRichiesta;

    @Column(length = 500)
    private String messaggio;

    @ManyToOne
    @JoinColumn(name = "studente_id", nullable = false)
    private Utente studente;

    @ManyToOne
    @JoinColumn(name = "sessione_id", nullable = false)
    private Sessione sessione;

    @OneToOne(mappedBy = "prenotazione", cascade = CascadeType.ALL, orphanRemoval = true)
    private Feedback feedback;

    public Prenotazione() {
    }

    public Prenotazione(StatoPrenotazione stato, LocalDateTime dataRichiesta, String messaggio,
                        Utente studente, Sessione sessione) {
        this.stato = stato;
        this.dataRichiesta = dataRichiesta;
        this.messaggio = messaggio;
        this.studente = studente;
        this.sessione = sessione;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StatoPrenotazione getStato() { return stato; }
    public void setStato(StatoPrenotazione stato) { this.stato = stato; }

    public LocalDateTime getDataRichiesta() { return dataRichiesta; }
    public void setDataRichiesta(LocalDateTime dataRichiesta) { this.dataRichiesta = dataRichiesta; }

    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }

    public Utente getStudente() { return studente; }
    public void setStudente(Utente studente) { this.studente = studente; }

    public Sessione getSessione() { return sessione; }
    public void setSessione(Sessione sessione) { this.sessione = sessione; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }
}
