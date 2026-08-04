package it.unito.peerlab.peerlabbackend.config;

import it.unito.peerlab.peerlabbackend.model.*;
import it.unito.peerlab.peerlabbackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Popola il database con dati di esempio al primo avvio.
 *
 * Implementa CommandLineRunner: Spring esegue il metodo run() subito dopo
 * l'avvio dell'applicazione. Il controllo su utenteRepository.count() evita di
 * duplicare i dati a ogni riavvio.
 *
 * Come consentito dalla traccia, gli utenti sono predefiniti nel DB: non e'
 * richiesto implementare una registrazione lato front-end.
 */
@Component
public class CaricamentoDatiIniziali implements CommandLineRunner {

    private final UtenteRepository utenteRepository;
    private final MateriaRepository materiaRepository;
    private final ArgomentoRepository argomentoRepository;
    private final SessioneRepository sessioneRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final MaterialeDidatticoRepository materialeRepository;

    public CaricamentoDatiIniziali(UtenteRepository utenteRepository,
                                   MateriaRepository materiaRepository,
                                   ArgomentoRepository argomentoRepository,
                                   SessioneRepository sessioneRepository,
                                   PrenotazioneRepository prenotazioneRepository,
                                   MaterialeDidatticoRepository materialeRepository) {
        this.utenteRepository = utenteRepository;
        this.materiaRepository = materiaRepository;
        this.argomentoRepository = argomentoRepository;
        this.sessioneRepository = sessioneRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.materialeRepository = materialeRepository;
    }

    @Override
    public void run(String... args) {

        if (utenteRepository.count() > 0) {
            System.out.println(">>> Dati gia' presenti: caricamento iniziale saltato.");
            return;
        }

        System.out.println(">>> Caricamento dati iniziali di PeerLab...");

        // ------------------------------------------------------------ UTENTI
        Utente giulia = new Utente("Giulia", "Rossi", "giulia", "giulia123",
                "giulia.rossi@edu.unito.it", Ruolo.TUTOR,
                "Terzo anno di Informatica. Ho superato Analisi I con 30 e mi piace spiegare.");

        Utente davide = new Utente("Davide", "Poli", "davide", "davide123",
                "davide.poli@edu.unito.it", Ruolo.TUTOR,
                "Studente magistrale. Tengo ripassi di Algoritmi e Tecnologie Web.");

        Utente marco = new Utente("Marco", "Bianchi", "marco", "marco123",
                "marco.bianchi@edu.unito.it", Ruolo.STUDENTE,
                "Matricola, primo anno di Informatica.");

        Utente sara = new Utente("Sara", "Melis", "sara", "sara123",
                "sara.melis@edu.unito.it", Ruolo.STUDENTE,
                "Secondo anno, fuori sede.");

        utenteRepository.saveAll(List.of(giulia, davide, marco, sara));

        // ----------------------------------------------------------- MATERIE
        Materia analisi = new Materia("Analisi Matematica I", "MFN0577", 1);
        Materia algoritmi = new Materia("Algoritmi e Strutture Dati", "MFN0578", 2);
        Materia tweb = new Materia("Tecnologie Web", "MFN0634", 3);
        Materia statistica = new Materia("Elementi di Probabilita' e Statistica", "MFN0579", 2);

        materiaRepository.saveAll(List.of(analisi, algoritmi, tweb, statistica));

        // --------------------------------------------------------- ARGOMENTI
        Argomento integrali = new Argomento("Integrali per parti e per sostituzione", analisi);
        Argomento limiti = new Argomento("Limiti notevoli", analisi);
        Argomento serie = new Argomento("Serie numeriche", analisi);
        Argomento grafi = new Argomento("Visite di grafi: BFS e DFS", algoritmi);
        Argomento ricorrenze = new Argomento("Ricorrenze e Master Theorem", algoritmi);
        Argomento promise = new Argomento("Promise e async/await", tweb);
        Argomento springBoot = new Argomento("Spring Boot e JPA", tweb);
        Argomento distribuzioni = new Argomento("Distribuzioni notevoli", statistica);

        argomentoRepository.saveAll(List.of(integrali, limiti, serie, grafi,
                ricorrenze, promise, springBoot, distribuzioni));

        // ---------------------------------------------------------- SESSIONI
        Sessione s1 = new Sessione(
                "Integrali per parti e per sostituzione",
                "Ripasso della teoria con esercizi tratti dagli appelli degli ultimi due anni. "
                        + "Portate carta e penna, si lavora insieme alla lavagna.",
                LocalDateTime.now().plusDays(4).withHour(14).withMinute(0).withSecond(0).withNano(0),
                "Aula studio Povo 1",
                ModalitaSessione.PRESENZA, 4, StatoSessione.APERTA, giulia, analisi);
        s1.setArgomenti(Set.of(integrali));

        Sessione s2 = new Sessione(
                "Limiti notevoli: esercizi d'esame",
                "Sessione online dedicata ai limiti notevoli, con svolgimento guidato di esercizi.",
                LocalDateTime.now().plusDays(6).withHour(10).withMinute(30).withSecond(0).withNano(0),
                "Link Meet inviato agli iscritti",
                ModalitaSessione.ONLINE, 6, StatoSessione.APERTA, giulia, analisi);
        s2.setArgomenti(Set.of(limiti));

        Sessione s3 = new Sessione(
                "Promise, async/await e fetch API",
                "Come funziona davvero l'asincronia in JavaScript: dai callback alle Promise, "
                        + "fino ad async/await. Con esempi pratici di chiamate a una API REST.",
                LocalDateTime.now().plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0),
                "Laboratorio informatico 2",
                ModalitaSessione.PRESENZA, 5, StatoSessione.APERTA, davide, tweb);
        s3.setArgomenti(Set.of(promise));

        Sessione s4 = new Sessione(
                "Ricorrenze e Master Theorem",
                "Metodo di sostituzione, albero di ricorsione e Master Theorem: quando usare cosa.",
                LocalDateTime.now().plusDays(8).withHour(15).withMinute(0).withSecond(0).withNano(0),
                "Aula studio Povo 2",
                ModalitaSessione.PRESENZA, 3, StatoSessione.APERTA, davide, algoritmi);
        s4.setArgomenti(Set.of(ricorrenze));

        Sessione s5 = new Sessione(
                "Spring Boot: entita', repository e controller",
                "Panoramica pratica sullo sviluppo di un back-end con Spring Boot e JPA.",
                LocalDateTime.now().minusDays(5).withHour(11).withMinute(0).withSecond(0).withNano(0),
                "Laboratorio informatico 1",
                ModalitaSessione.PRESENZA, 4, StatoSessione.COMPLETATA, davide, tweb);
        s5.setArgomenti(Set.of(springBoot));

        sessioneRepository.saveAll(List.of(s1, s2, s3, s4, s5));

        // ------------------------------------------------------- PRENOTAZIONI
        // Marco ha una richiesta accettata su s1 e una ancora in attesa su s3
        Prenotazione p1 = new Prenotazione(StatoPrenotazione.ACCETTATA,
                LocalDateTime.now().minusDays(1), "Sono fermo sugli integrali per parti.",
                marco, s1);

        Prenotazione p2 = new Prenotazione(StatoPrenotazione.IN_ATTESA,
                LocalDateTime.now().minusHours(3), "Vorrei chiarimenti sulle Promise.",
                marco, s3);

        // Sara ha una richiesta in attesa su s1 e una accettata sulla sessione conclusa
        Prenotazione p3 = new Prenotazione(StatoPrenotazione.IN_ATTESA,
                LocalDateTime.now().minusHours(6), "Posso partecipare anche io?",
                sara, s1);

        Prenotazione p4 = new Prenotazione(StatoPrenotazione.ACCETTATA,
                LocalDateTime.now().minusDays(7), "Interessata alla parte su JPA.",
                sara, s5);

        prenotazioneRepository.saveAll(List.of(p1, p2, p3, p4));

        // --------------------------------------------------------- MATERIALI
        materialeRepository.saveAll(List.of(
                new MaterialeDidattico("Esercizi svolti sugli integrali",
                        "https://esempio.unito.it/esercizi-integrali.pdf", "PDF", s1),
                new MaterialeDidattico("Slide su Promise e async/await",
                        "https://esempio.unito.it/promise-slide.pdf", "PDF", s3)
        ));

        System.out.println(">>> Dati iniziali caricati:");
        System.out.println("    4 utenti  | tutor: giulia/giulia123, davide/davide123");
        System.out.println("              | studenti: marco/marco123, sara/sara123");
        System.out.println("    4 materie | 8 argomenti | 5 sessioni | 4 prenotazioni");
    }
}
