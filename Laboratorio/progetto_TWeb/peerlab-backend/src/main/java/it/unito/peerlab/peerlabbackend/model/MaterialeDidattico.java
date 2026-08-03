package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "materiali_didattici")
public class MaterialeDidattico {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String titolo;

    @Column(nullable = false, length = 500)
    private String url;

    private String tipo;

    @ManyToOne
    @JoinColumn(name = "sessione_id", nullable = false)
    private Sessione sessione;

    public MaterialeDidattico() {
    }

    public MaterialeDidattico(String titolo, String url, String tipo, Sessione sessione) {
        this.titolo = titolo;
        this.url = url;
        this.tipo = tipo;
        this.sessione = sessione;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Sessione getSessione() { return sessione; }
    public void setSessione(Sessione sessione) { this.sessione = sessione; }
}
