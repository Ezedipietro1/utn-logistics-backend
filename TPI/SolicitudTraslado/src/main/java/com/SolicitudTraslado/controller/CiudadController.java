package com.SolicitudTraslado.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SolicitudTraslado.dto.CiudadDTO;
import com.SolicitudTraslado.services.CiudadService;

@RestController
@RequestMapping("/api/solicitudes/ciudades")
public class CiudadController {

    private final CiudadService ciudadService;

    public CiudadController(CiudadService ciudadService) {
        this.ciudadService = ciudadService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<CiudadDTO>> listarCiudades() {
        return ResponseEntity.ok(ciudadService.listarCiudades());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CiudadDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ciudadService.obtenerCiudadPorId(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CiudadDTO> buscarPorNombre(@RequestParam("nombre") String nombre) {
        return ResponseEntity.ok(ciudadService.obtenerCiudadPorNombre(nombre));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CiudadDTO> crearCiudad(@RequestBody CiudadDTO ciudad) {
        CiudadDTO creada = ciudadService.crearCiudad(ciudad);
        return ResponseEntity.ok(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CiudadDTO> actualizarCiudad(@PathVariable Long id, @RequestBody CiudadDTO ciudad) {
        ciudad.setId(id);
        CiudadDTO actualizada = ciudadService.actualizarCiudad(ciudad);
        return ResponseEntity.ok(actualizada);
    }
}
