package it.unito.peerlab.peerlabbackend.service;

import it.unito.peerlab.peerlabbackend.dto.LoginRequest;
import it.unito.peerlab.peerlabbackend.dto.UtenteDTO;
import it.unito.peerlab.peerlabbackend.exception.NonAutorizzatoException;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import it.unito.peerlab.peerlabbackend.mapper.DTOMapper;
import it.unito.peerlab.peerlabbackend.model.Ruolo;
import it.unito.peerlab.peerlabbackend.model.Utente;
import it.unito.peerlab.peerlabbackend.repository.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic relativa agli utenti e all'autenticazione.
 *
 * Le dipendenze (repository e mapper) sono dichiarate come parametri del
 * costruttore: e' la dependency injection: e' Spring a creare questo bean e a
 * "iniettargli" le istanze gia' pronte degli altri bean.
 */
@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final DTOMapper mapper;

    public UtenteService(UtenteRepository utenteRepository, DTOMapper mapper) {
        this.utenteRepository = utenteRepository;
        this.mapper = mapper;
    }

    /**
     * Verifica le credenziali. In un'applicazione reale la password sarebbe
     * memorizzata come hash: qui, come consentito dalla traccia, usiamo un
     * meccanismo semplificato.
     */
    @Transactional(readOnly = true)
    public UtenteDTO login(LoginRequest richiesta) {
        Utente utente = utenteRepository
                .findByUsernameAndPassword(richiesta.username(), richiesta.password())
                .orElseThrow(() -> new NonAutorizzatoException("Username o password non validi"));
        return mapper.toUtenteDTO(utente);
    }

    @Transactional(readOnly = true)
    public UtenteDTO getById(Long id) {
        return mapper.toUtenteDTO(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<UtenteDTO> getTutor() {
        return utenteRepository.findByRuolo(Ruolo.TUTOR).stream()
                .map(mapper::toUtenteDTO)
                .toList();
    }

    /**
     * Restituisce l'entita' (non il DTO): metodo a uso interno degli altri
     * service, che hanno bisogno dell'oggetto gestito da JPA.
     */
    @Transactional(readOnly = true)
    public Utente getEntityById(Long id) {
        return utenteRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato: " + id));
    }
}
