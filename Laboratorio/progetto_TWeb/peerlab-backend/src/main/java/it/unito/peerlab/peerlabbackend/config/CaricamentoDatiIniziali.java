package it.unito.peerlab.peerlabbackend.config;

import it.unito.peerlab.peerlabbackend.model.*;
import it.unito.peerlab.peerlabbackend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final AvvisoRepository avvisoRepository;

    public CaricamentoDatiIniziali(UtenteRepository utenteRepository,
                                   MateriaRepository materiaRepository,
                                   ArgomentoRepository argomentoRepository,
                                   SessioneRepository sessioneRepository,
                                   PrenotazioneRepository prenotazioneRepository,
                                   MaterialeDidatticoRepository materialeRepository,
                                   AvvisoRepository avvisoRepository) {
        this.utenteRepository = utenteRepository;
        this.materiaRepository = materiaRepository;
        this.argomentoRepository = argomentoRepository;
        this.sessioneRepository = sessioneRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.materialeRepository = materialeRepository;
        this.avvisoRepository = avvisoRepository;
    }

    @Override
    public void run(String... args) {

        if (utenteRepository.count() > 0) {
            System.out.println(">>> Dati gia' presenti: caricamento iniziale saltato.");
            return;
        }

        System.out.println(">>> Caricamento dati iniziali di PeerStudent...");

        // ------------------------------------------------------------ TUTOR
        Utente giulia = new Utente("Giulia", "Rossi", "giulia", "giulia123",
                "giulia.rossi@edu.unito.it", "912345", Ruolo.TUTOR, LocalDate.of(2003, 4, 12),
                "Terzo anno di Informatica. Ho superato Analisi Matematica con 30 e mi piace spiegare.");

        Utente davide = new Utente("Davide", "Poli", "davide", "davide123",
                "davide.poli@edu.unito.it", "884120", Ruolo.TUTOR, LocalDate.of(2001, 9, 3),
                "Studente magistrale. Tengo ripassi di Algoritmi e Sviluppo Applicazioni Software.");

        Utente elena = new Utente("Elena", "Ferraro", "elena", "elena123",
                "elena.ferraro@edu.unito.it", "901877", Ruolo.TUTOR, LocalDate.of(2002, 11, 27),
                "Al secondo anno. Analisi e' stato il mio primo 30 e lode, ci tengo a passarlo avanti.");

        Utente luca = new Utente("Luca", "Bertino", "luca", "luca123",
                "luca.bertino@edu.unito.it", "873094", Ruolo.TUTOR, LocalDate.of(2000, 2, 18),
                "Magistrale in Informatica. Mi occupo di strutture dati e complessita'.");

        Utente chiara = new Utente("Chiara", "Amato", "chiara", "chiara123",
                "chiara.amato@edu.unito.it", "889231", Ruolo.TUTOR, LocalDate.of(2001, 6, 30),
                "Ho seguito SAS lo scorso anno: UML e testing sono il mio pane.");

        Utente matteo = new Utente("Matteo", "Riva", "matteo", "matteo123",
                "matteo.riva@edu.unito.it", "905612", Ruolo.TUTOR, LocalDate.of(2002, 1, 9),
                "Terzo anno. Mi trovo bene a spiegare progettazione software e design pattern.");

        // --------------------------------------------------------- STUDENTI
        Utente marco = new Utente("Marco", "Bianchi", "marco", "marco123",
                "marco.bianchi@edu.unito.it", "998204", Ruolo.STUDENTE, LocalDate.of(2005, 3, 21),
                "Primo anno di Informatica, sto ancora prendendo le misure.");

        Utente sara = new Utente("Sara", "Melis", "sara", "sara123",
                "sara.melis@edu.unito.it", "974558", Ruolo.STUDENTE, LocalDate.of(2004, 7, 14),
                "Secondo anno, fuori sede.");

        utenteRepository.saveAll(
                List.of(giulia, davide, elena, luca, chiara, matteo, marco, sara));

        // ----------------------------------------------------------- MATERIE
        Materia analisi = new Materia("Analisi Matematica", "MFN0577", 1);
        Materia algoritmi = new Materia("Algoritmi e Strutture Dati", "MFN0578", 2);
        Materia sas = new Materia("Sviluppo Applicazioni Software", "MFN0596", 2);

        materiaRepository.saveAll(List.of(analisi, algoritmi, sas));

        // --------------------------------------------------------- ARGOMENTI
        Argomento integrali = new Argomento("Integrali per parti e per sostituzione", analisi);
        Argomento limiti = new Argomento("Limiti notevoli", analisi);
        Argomento serie = new Argomento("Serie numeriche", analisi);
        Argomento studioFunzione = new Argomento("Studio di funzione", analisi);

        Argomento grafi = new Argomento("Visite di grafi: BFS e DFS", algoritmi);
        Argomento ricorrenze = new Argomento("Ricorrenze e Master Theorem", algoritmi);
        Argomento ordinamento = new Argomento("Algoritmi di ordinamento", algoritmi);
        Argomento alberi = new Argomento("Alberi binari di ricerca", algoritmi);

        Argomento uml = new Argomento("Diagrammi UML e casi d'uso", sas);
        Argomento pattern = new Argomento("Design pattern", sas);
        Argomento testing = new Argomento("Testing e JUnit", sas);
        Argomento versionamento = new Argomento("Git e lavoro in team", sas);

        argomentoRepository.saveAll(List.of(integrali, limiti, serie, studioFunzione,
                grafi, ricorrenze, ordinamento, alberi,
                uml, pattern, testing, versionamento));

        // ---------------------------------------------------------- SESSIONI
        // Ogni materia ha piu' appuntamenti, ciascuno con un tutor diverso,
        // distribuiti fra online e sedi in presenza.

        // --- Analisi Matematica
        Sessione a1 = new Sessione(
                "Integrali per parti e per sostituzione",
                "Ripasso della teoria con esercizi tratti dagli appelli degli ultimi due anni. "
                        + "Portate carta e penna, si lavora insieme alla lavagna.",
                fraGiorni(4, 14, 0), "Sala studio Edisu",
                ModalitaSessione.PRESENZA, 4, StatoSessione.APERTA, giulia, analisi);
        a1.setArgomenti(Set.of(integrali));

        Sessione a2 = new Sessione(
                "Limiti notevoli: esercizi d'esame",
                "Svolgimento guidato dei limiti piu' ricorrenti nei compiti, con i trucchi "
                        + "per riconoscere subito la forma indeterminata.",
                fraGiorni(6, 10, 30), "Online",
                ModalitaSessione.ONLINE, 6, StatoSessione.APERTA, elena, analisi);
        a2.setLinkIncontro("https://meet.google.com/peerstudent-analisi-limiti");
        a2.setArgomenti(Set.of(limiti));

        Sessione a3 = new Sessione(
                "Studio di funzione dalla A alla Z",
                "Un esercizio completo svolto passo passo: dominio, asintoti, derivate, grafico.",
                fraGiorni(9, 16, 0), "Aula A",
                ModalitaSessione.PRESENZA, 5, StatoSessione.APERTA, giulia, analisi);
        a3.setArgomenti(Set.of(studioFunzione));

        // --- Algoritmi e Strutture Dati
        Sessione b1 = new Sessione(
                "Ricorrenze e Master Theorem",
                "Metodo di sostituzione, albero di ricorsione e Master Theorem: quando usare cosa.",
                fraGiorni(5, 15, 0), "Aula B",
                ModalitaSessione.PRESENZA, 3, StatoSessione.APERTA, davide, algoritmi);
        b1.setArgomenti(Set.of(ricorrenze));

        Sessione b2 = new Sessione(
                "Visite di grafi: BFS e DFS",
                "Come si scrivono, come si ricordano, e come si riconosce quale serve "
                        + "in un esercizio d'esame.",
                fraGiorni(7, 11, 0), "Online",
                ModalitaSessione.ONLINE, 8, StatoSessione.APERTA, luca, algoritmi);
        b2.setLinkIncontro("https://meet.google.com/peerstudent-algoritmi-grafi");
        b2.setArgomenti(Set.of(grafi));

        Sessione b3 = new Sessione(
                "Alberi binari di ricerca e ordinamento",
                "Inserimento, cancellazione, bilanciamento. Nella seconda parte confronto "
                        + "fra gli algoritmi di ordinamento e le rispettive complessita'.",
                fraGiorni(11, 14, 30), "Sala studio Edisu",
                ModalitaSessione.PRESENZA, 4, StatoSessione.APERTA, luca, algoritmi);
        b3.setArgomenti(Set.of(alberi, ordinamento));

        // --- Sviluppo Applicazioni Software
        Sessione c1 = new Sessione(
                "Diagrammi UML e casi d'uso",
                "Dalla richiesta del committente al diagramma: come si individuano attori, "
                        + "scenari e casi d'uso senza perdersi.",
                fraGiorni(3, 9, 30), "Aula A",
                ModalitaSessione.PRESENZA, 6, StatoSessione.APERTA, chiara, sas);
        c1.setArgomenti(Set.of(uml));

        Sessione c2 = new Sessione(
                "Design pattern piu' richiesti all'esame",
                "Singleton, Observer, Strategy e Factory spiegati con esempi di codice "
                        + "e con gli errori tipici da evitare.",
                fraGiorni(8, 17, 0), "Online",
                ModalitaSessione.ONLINE, 10, StatoSessione.APERTA, matteo, sas);
        c2.setLinkIncontro("https://meet.google.com/peerstudent-sas-pattern");
        c2.setArgomenti(Set.of(pattern));

        Sessione c3 = new Sessione(
                "Testing con JUnit e uso di Git in team",
                "Scrivere test che servono davvero, e gestire branch e conflitti senza panico.",
                fraGiorni(12, 15, 30), "Aula B",
                ModalitaSessione.PRESENZA, 5, StatoSessione.APERTA, chiara, sas);
        c3.setArgomenti(Set.of(testing, versionamento));

        // --- una sessione gia' svolta, per poter mostrare le valutazioni
        Sessione conclusa = new Sessione(
                "Serie numeriche: criteri di convergenza",
                "Confronto, rapporto, radice: quale criterio applicare e perche'.",
                LocalDateTime.now().minusDays(6).withHour(11).withMinute(0)
                        .withSecond(0).withNano(0),
                "Aula A",
                ModalitaSessione.PRESENZA, 4, StatoSessione.COMPLETATA, elena, analisi);
        conclusa.setArgomenti(Set.of(serie));

        sessioneRepository.saveAll(List.of(a1, a2, a3, b1, b2, b3, c1, c2, c3, conclusa));

        // ------------------------------------------------------- PRENOTAZIONI
        Prenotazione p1 = new Prenotazione(StatoPrenotazione.ACCETTATA,
                LocalDateTime.now().minusDays(1),
                "Sono fermo sugli integrali per parti, non capisco come scegliere le due parti.",
                null, marco, a1);

        Prenotazione p2 = new Prenotazione(StatoPrenotazione.IN_ATTESA,
                LocalDateTime.now().minusHours(3),
                "Vorrei ripassare i limiti notevoli prima dell'appello di settembre.",
                "marco.bianchi@edu.unito.it", marco, a2);

        Prenotazione p3 = new Prenotazione(StatoPrenotazione.IN_ATTESA,
                LocalDateTime.now().minusHours(6),
                "Ho problemi con il Master Theorem, in particolare con il terzo caso.",
                null, sara, b1);

        Prenotazione p4 = new Prenotazione(StatoPrenotazione.ACCETTATA,
                LocalDateTime.now().minusDays(8),
                "Non riesco a distinguere quando usare il criterio del rapporto.",
                null, sara, conclusa);

        prenotazioneRepository.saveAll(List.of(p1, p2, p3, p4));

        // --------------------------------------------------------- MATERIALI
        materialeRepository.saveAll(List.of(
                new MaterialeDidattico("Esercizi svolti sugli integrali",
                        "http://localhost:5173/materiali/esercizi-integrali.pdf", "PDF", a1),
                new MaterialeDidattico("Schema dei design pattern",
                        "http://localhost:5173/materiali/schema-design-pattern.pdf", "PDF", c2)
        ));

        // ----------------------------------------------------------- AVVISI
        avvisoRepository.saveAll(List.of(
                new Avviso("Ricordate di portare il libro di testo: partiamo dagli "
                        + "esercizi del capitolo 6.",
                        LocalDateTime.now().minusHours(20), a1),
                new Avviso("Ho aggiunto fra i materiali lo schema dei pattern che "
                        + "useremo durante l'incontro.",
                        LocalDateTime.now().minusHours(5), c2)
        ));

        System.out.println(">>> Dati iniziali caricati:");
        System.out.println("    tutor:    giulia, davide, elena, luca, chiara, matteo  (password: nome+123)");
        System.out.println("    studenti: marco/marco123, sara/sara123");
        System.out.println("    3 materie | 12 argomenti | 10 sessioni | 4 prenotazioni | 2 avvisi");
    }

    /** Costruisce una data futura a partire da oggi, con ora e minuti indicati. */
    private LocalDateTime fraGiorni(int giorni, int ora, int minuti) {
        return LocalDateTime.now()
                .plusDays(giorni)
                .withHour(ora)
                .withMinute(minuti)
                .withSecond(0)
                .withNano(0);
    }
}
