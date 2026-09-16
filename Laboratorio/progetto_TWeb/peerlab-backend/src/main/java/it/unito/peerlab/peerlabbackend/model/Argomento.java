package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "argomenti")
public class Argomento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @ManyToMany(mappedBy = "argomenti")
    private Set<Sessione> sessioni = new HashSet<>();

    public Argomento() {
    }

    public Argomento(String nome, Materia materia) {
        this.nome = nome;
        this.materia = materia;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Materia getMateria() { return materia; }
    public void setMateria(Materia materia) { this.materia = materia; }

    public Set<Sessione> getSessioni() { return sessioni; }
    public void setSessioni(Set<Sessione> sessioni) { this.sessioni = sessioni; }
}
