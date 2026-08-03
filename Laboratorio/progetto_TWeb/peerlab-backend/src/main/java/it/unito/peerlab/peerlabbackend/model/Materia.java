package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "materie")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "codice_esame", unique = true)
    private String codiceEsame;

    @Column(name = "anno_corso")
    private Integer annoCorso;

    @OneToMany(mappedBy = "materia")
    private List<Argomento> argomenti = new ArrayList<>();

    @OneToMany(mappedBy = "materia")
    private List<Sessione> sessioni = new ArrayList<>();

    public Materia() {
    }

    public Materia(String nome, String codiceEsame, Integer annoCorso) {
        this.nome = nome;
        this.codiceEsame = codiceEsame;
        this.annoCorso = annoCorso;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCodiceEsame() { return codiceEsame; }
    public void setCodiceEsame(String codiceEsame) { this.codiceEsame = codiceEsame; }

    public Integer getAnnoCorso() { return annoCorso; }
    public void setAnnoCorso(Integer annoCorso) { this.annoCorso = annoCorso; }

    public List<Argomento> getArgomenti() { return argomenti; }
    public void setArgomenti(List<Argomento> argomenti) { this.argomenti = argomenti; }

    public List<Sessione> getSessioni() { return sessioni; }
    public void setSessioni(List<Sessione> sessioni) { this.sessioni = sessioni; }
}
