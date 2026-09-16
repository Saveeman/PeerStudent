package it.unito.peerlab.peerlabbackend.controller;

import it.unito.peerlab.peerlabbackend.dto.ArgomentoDTO;
import it.unito.peerlab.peerlabbackend.dto.MateriaDTO;
import it.unito.peerlab.peerlabbackend.service.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route di consultazione del catalogo: materie e argomenti.
 * Sono dati di riferimento, in sola lettura, usati dal front-end per popolare
 * filtri e menu a tendina.
 */
@RestController
@RequestMapping("/api/materie")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * GET /api/materie
     * GET /api/materie?anno=2
     * GET /api/materie?q=analisi
     */
    @GetMapping
    public ResponseEntity<List<MateriaDTO>> elencaMaterie(
            @RequestParam(required = false) Integer anno,
            @RequestParam(required = false) String q) {

        if (anno != null) {
            return ResponseEntity.ok(catalogoService.getMateriePerAnno(anno));
        }
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(catalogoService.cercaMaterie(q));
        }
        return ResponseEntity.ok(catalogoService.getTutteLeMaterie());
    }

    /** GET /api/materie/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<MateriaDTO> dettaglioMateria(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.getMateriaById(id));
    }

    /** GET /api/materie/{id}/argomenti */
    @GetMapping("/{id}/argomenti")
    public ResponseEntity<List<ArgomentoDTO>> argomentiDiMateria(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.getArgomentiPerMateria(id));
    }
}
