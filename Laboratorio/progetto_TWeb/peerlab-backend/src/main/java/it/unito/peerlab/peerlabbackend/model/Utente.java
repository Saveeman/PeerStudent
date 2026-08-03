package it.unito.peerlab.peerlabbackend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utenti")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ruolo ruolo;

    @Column(length = 500)
    private String bio;

    @OneToMany(mappedBy = "tutor")
    private List<Sessione> sessioniCreate = new ArrayList<>();

    @OneToMany(mappedBy = "studente")
    private List<Prenotazione> prenotazioni = new ArrayList<>();

    public Utente() {
    }

    public Utente(String nome, String cognome, String username, String password,
                  String email, Ruolo ruolo, String bio) {
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.password = password;
        this.email = email;
        this.ruolo = ruolo;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Ruolo getRuolo() { return ruolo; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<Sessione> getSessioniCreate() { return sessioniCreate; }
    public void setSessioniCreate(List<Sessione> sessioniCreate) { this.sessioniCreate = sessioniCreate; }

    public List<Prenotazione> getPrenotazioni() { return prenotazioni; }
    public void setPrenotazioni(List<Prenotazione> prenotazioni) { this.prenotazioni = prenotazioni; }
}
