package it.unito.peerlab.peerlabbackend.mapper;

import it.unito.peerlab.peerlabbackend.dto.*;
import it.unito.peerlab.peerlabbackend.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Converte le @Entity nei corrispondenti DTO da inviare al front-end.
 *
 * E' un @Component (quindi un bean) e viene iniettato nei @Service che ne hanno
 * bisogno tramite dependency injection.
 *
 * Nota importante: il mapper NON accede al database. I dati che non stanno
 * nell'entita' (es. i posti occupati di una sessione) gli vengono passati come
 * parametro da chi lo invoca.
 */
@Component
public class DTOMapper {

    public UtenteDTO toUtenteDTO(Utente u) {
        if (u == null) return null;
        return new UtenteDTO(
                u.getId(),
                u.getNome(),
                u.getCognome(),
                u.getUsername(),
                u.getEmail(),
                u.getMatricola(),
                u.getRuolo(),
                u.getDataNascita(),
                calcolaEta(u.getDataNascita()),
                u.getBio()
        );
    }

    /**
     * L'eta' non e' memorizzata nel database: sarebbe un dato che invecchia da
     * solo. Si calcola dalla data di nascita al momento della richiesta.
     */
    private Integer calcolaEta(LocalDate dataNascita) {
        if (dataNascita == null) return null;
        return Period.between(dataNascita, LocalDate.now()).getYears();
    }

    public MateriaDTO toMateriaDTO(Materia m) {
        if (m == null) return null;
        return new MateriaDTO(
                m.getId(),
                m.getNome(),
                m.getCodiceEsame(),
                m.getAnnoCorso()
        );
    }

    public ArgomentoDTO toArgomentoDTO(Argomento a) {
        if (a == null) return null;
        return new ArgomentoDTO(
                a.getId(),
                a.getNome(),
                a.getMateria() != null ? a.getMateria().getId() : null,
                a.getMateria() != null ? a.getMateria().getNome() : null
        );
    }

    public MaterialeDidatticoDTO toMaterialeDTO(MaterialeDidattico m) {
        if (m == null) return null;
        return new MaterialeDidatticoDTO(
                m.getId(),
                m.getTitolo(),
                m.getUrl(),
                m.getTipo()
        );
    }

    /**
     * @param postiOccupati numero di prenotazioni ACCETTATE, calcolato dal service
     * @param mostraLink    se false, il link della videochiamata NON viene
     *                      inserito nel DTO: chi non ha diritto di vederlo non
     *                      lo riceve nemmeno nel JSON
     */
    public SessioneDTO toSessioneDTO(Sessione s, long postiOccupati, boolean mostraLink) {
        if (s == null) return null;

        List<ArgomentoDTO> argomenti = s.getArgomenti().stream()
                .map(this::toArgomentoDTO)
                .toList();

        List<MaterialeDidatticoDTO> materiali = s.getMateriali().stream()
                .map(this::toMaterialeDTO)
                .toList();

        long disponibili = Math.max(0, s.getPostiTotali() - postiOccupati);

        return new SessioneDTO(
                s.getId(),
                s.getTitolo(),
                s.getDescrizione(),
                s.getDataOra(),
                s.getLuogo(),
                mostraLink ? s.getLinkIncontro() : null,
                s.getModalita(),
                s.getPostiTotali(),
                postiOccupati,
                disponibili,
                s.getStato(),
                toUtenteDTO(s.getTutor()),
                toMateriaDTO(s.getMateria()),
                argomenti,
                materiali
        );
    }

    public PrenotazioneDTO toPrenotazioneDTO(Prenotazione p) {
        if (p == null) return null;

        Sessione s = p.getSessione();
        String tutorNome = null;
        if (s != null && s.getTutor() != null) {
            tutorNome = s.getTutor().getNome() + " " + s.getTutor().getCognome();
        }

        /* Il link della videochiamata viene comunicato allo studente soltanto
           quando la sua richiesta e' stata accettata e la sessione si svolge
           online: e' il modo in cui "riceve" il collegamento.
           Il collegamento smette di essere mostrato quando l'appuntamento non
           e' piu' in programma, cioe' se e' stato annullato o si e' gia'
           svolto: in quei casi non servirebbe a nulla. */
        String link = null;
        boolean inProgramma = s != null
                && (s.getStato() == StatoSessione.APERTA
                    || s.getStato() == StatoSessione.CHIUSA);

        if (p.getStato() == StatoPrenotazione.ACCETTATA
                && inProgramma
                && s.getModalita() == ModalitaSessione.ONLINE) {
            link = s.getLinkIncontro();
        }

        return new PrenotazioneDTO(
                p.getId(),
                p.getStato(),
                p.getDataRichiesta(),
                p.getMessaggio(),
                p.getEmailContatto(),
                toUtenteDTO(p.getStudente()),
                s != null ? s.getId() : null,
                s != null ? s.getTitolo() : null,
                s != null ? s.getDataOra() : null,
                tutorNome,
                link,
                p.getFeedback() != null
        );
    }

    public AvvisoDTO toAvvisoDTO(Avviso a) {
        if (a == null) return null;

        Sessione s = a.getSessione();
        String tutorNome = null;
        if (s != null && s.getTutor() != null) {
            tutorNome = s.getTutor().getNome() + " " + s.getTutor().getCognome();
        }

        return new AvvisoDTO(
                a.getId(),
                a.getTesto(),
                a.getData(),
                s != null ? s.getId() : null,
                s != null ? s.getTitolo() : null,
                tutorNome
        );
    }

    public FeedbackDTO toFeedbackDTO(Feedback f) {
        if (f == null) return null;

        Prenotazione p = f.getPrenotazione();
        String studenteNome = null;
        String sessioneTitolo = null;
        if (p != null) {
            if (p.getStudente() != null) {
                studenteNome = p.getStudente().getNome() + " " + p.getStudente().getCognome();
            }
            if (p.getSessione() != null) {
                sessioneTitolo = p.getSessione().getTitolo();
            }
        }

        return new FeedbackDTO(
                f.getId(),
                f.getVoto(),
                f.getCommento(),
                f.getData(),
                p != null ? p.getId() : null,
                studenteNome,
                sessioneTitolo
        );
    }
}
