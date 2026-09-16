package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Integer voto;

    @Column(length = 1000)
    private String commento;

    @Column(nullable = false)
    private LocalDateTime data;

    @OneToOne
    @JoinColumn(name = "prenotazione_id", nullable = false, unique = true)
    private Prenotazione prenotazione;

    public Feedback() {
    }

    public Feedback(Integer voto, String commento, LocalDateTime data, Prenotazione prenotazione) {
        this.voto = voto;
        this.commento = commento;
        this.data = data;
        this.prenotazione = prenotazione;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVoto() { return voto; }
    public void setVoto(Integer voto) { this.voto = voto; }

    public String getCommento() { return commento; }
    public void setCommento(String commento) { this.commento = commento; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public Prenotazione getPrenotazione() { return prenotazione; }
    public void setPrenotazione(Prenotazione prenotazione) { this.prenotazione = prenotazione; }
}
