package it.unito.peerlab.peerlabbackend.service;

import it.unito.peerlab.peerlabbackend.dto.ArgomentoDTO;
import it.unito.peerlab.peerlabbackend.dto.MateriaDTO;
import it.unito.peerlab.peerlabbackend.exception.RisorsaNonTrovataException;
import it.unito.peerlab.peerlabbackend.mapper.DTOMapper;
import it.unito.peerlab.peerlabbackend.model.Materia;
import it.unito.peerlab.peerlabbackend.repository.ArgomentoRepository;
import it.unito.peerlab.peerlabbackend.repository.MateriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic del "catalogo": le materie e i loro argomenti.
 * Sono dati di riferimento, consultati in sola lettura dal front-end per
 * popolare filtri e menu a tendina.
 */
@Service
public class CatalogoService {

    private final MateriaRepository materiaRepository;
    private final ArgomentoRepository argomentoRepository;
    private final DTOMapper mapper;

    public CatalogoService(MateriaRepository materiaRepository,
                           ArgomentoRepository argomentoRepository,
                           DTOMapper mapper) {
        this.materiaRepository = materiaRepository;
        this.argomentoRepository = argomentoRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MateriaDTO> getTutteLeMaterie() {
        return materiaRepository.findAll().stream()
                .map(mapper::toMateriaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MateriaDTO getMateriaById(Long id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new RisorsaNonTrovataException("Materia non trovata: " + id));
        return mapper.toMateriaDTO(materia);
    }

    /** Ricerca per nome (usata dal campo di ricerca del front-end). */
    @Transactional(readOnly = true)
    public List<MateriaDTO> cercaMaterie(String nome) {
        return materiaRepository.findByNomeContainsIgnoreCaseOrderByNomeAsc(nome).stream()
                .map(mapper::toMateriaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MateriaDTO> getMateriePerAnno(Integer anno) {
        return materiaRepository.findByAnnoCorsoOrderByNomeAsc(anno).stream()
                .map(mapper::toMateriaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArgomentoDTO> getArgomentiPerMateria(Long materiaId) {
        if (!materiaRepository.existsById(materiaId)) {
            throw new RisorsaNonTrovataException("Materia non trovata: " + materiaId);
        }
        return argomentoRepository.findByMateriaIdOrderByNomeAsc(materiaId).stream()
                .map(mapper::toArgomentoDTO)
                .toList();
    }
}
