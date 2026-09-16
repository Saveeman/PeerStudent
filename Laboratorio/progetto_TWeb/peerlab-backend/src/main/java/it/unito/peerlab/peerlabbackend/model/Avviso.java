package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Comunicazione inviata dal tutor ai partecipanti di un suo appuntamento.
 *
 * L'avviso e' legato alla sessione, non ai singoli studenti: chi lo riceve
 * viene determinato al momento della lettura, cercando le prenotazioni
 * accettate su quella sessione. Cosi' un avviso inviato oggi raggiunge anche
 * chi viene accettato domani, senza dover duplicare il messaggio per ogni
 * destinatario.
 */
@Entity
@Table(name = "avvisi")
public class Avviso {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 800)
    private String testo;

    @Column(nullable = false)
    private LocalDateTime data;

    @ManyToOne
    @JoinColumn(name = "sessione_id", nullable = false)
    private Sessione sessione;

    public Avviso() {
    }

    public Avviso(String testo, LocalDateTime data, Sessione sessione) {
        this.testo = testo;
        this.data = data;
        this.sessione = sessione;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTesto() { return testo; }
    public void setTesto(String testo) { this.testo = testo; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public Sessione getSessione() { return sessione; }
    public void setSessione(Sessione sessione) { this.sessione = sessione; }
}
