package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sessioni")
public class Sessione {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String titolo;

    @Column(length = 1000)
    private String descrizione;

    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;

    private String luogo;

    /**
     * Link della videochiamata, valorizzato solo per le sessioni ONLINE.
     * E' un dato riservato: viene comunicato soltanto al tutor proprietario e
     * agli studenti la cui prenotazione e' stata accettata.
     */
    @Column(name = "link_incontro", length = 500)
    private String linkIncontro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModalitaSessione modalita;

    @Column(name = "posti_totali", nullable = false)
    private Integer postiTotali;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoSessione stato;

    @ManyToOne
    @JoinColumn(name = "tutor_id", nullable = false)
    private Utente tutor;

    @ManyToOne
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToMany
    @JoinTable(
            name = "sessioni_argomenti",
            joinColumns = @JoinColumn(name = "sessione_id"),
            inverseJoinColumns = @JoinColumn(name = "argomento_id")
    )
    private Set<Argomento> argomenti = new HashSet<>();

    @OneToMany(mappedBy = "sessione")
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    @OneToMany(mappedBy = "sessione", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialeDidattico> materiali = new ArrayList<>();

    public Sessione() {
    }

    public Sessione(String titolo, String descrizione, LocalDateTime dataOra, String luogo,
                    ModalitaSessione modalita, Integer postiTotali, StatoSessione stato,
                    Utente tutor, Materia materia) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.dataOra = dataOra;
        this.luogo = luogo;
        this.modalita = modalita;
        this.postiTotali = postiTotali;
        this.stato = stato;
        this.tutor = tutor;
        this.materia = materia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public LocalDateTime getDataOra() { return dataOra; }
    public void setDataOra(LocalDateTime dataOra) { this.dataOra = dataOra; }

    public String getLuogo() { return luogo; }
    public void setLuogo(String luogo) { this.luogo = luogo; }

    public String getLinkIncontro() { return linkIncontro; }
    public void setLinkIncontro(String linkIncontro) { this.linkIncontro = linkIncontro; }

    public ModalitaSessione getModalita() { return modalita; }
    public void setModalita(ModalitaSessione modalita) { this.modalita = modalita; }

    public Integer getPostiTotali() { return postiTotali; }
    public void setPostiTotali(Integer postiTotali) { this.postiTotali = postiTotali; }

    public StatoSessione getStato() { return stato; }
    public void setStato(StatoSessione stato) { this.stato = stato; }

    public Utente getTutor() { return tutor; }
    public void setTutor(Utente tutor) { this.tutor = tutor; }

    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }

    public Set<Argomento> getArgomenti() { return argomenti; }
    public void setArgomenti(Set<Argomento> argomenti) { this.argomenti = argomenti; }

    public List<Prenotazione> getPrenotazioni() { return prenotazioni; }
    public void setPrenotazioni(List<Prenotazione> prenotazioni) { this.prenotazioni = prenotazioni; }

    public List<MaterialeDidattico> getMateriali() { return materiali; }
    public void setMateriali(List<MaterialeDidattico> materiali) { this.materiali = materiali; }
}
