package com.SolicitudTraslado.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SolicitudTraslado.dto.ContenedorDTO;
import com.SolicitudTraslado.services.ContenedorService;

@RestController
@RequestMapping("/api/solicitudes/contenedores")
public class ContenedorController {

    private final ContenedorService contenedorService;

    public ContenedorController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<Long, ContenedorDTO>> listarContenedores() {
        return ResponseEntity.ok(contenedorService.listarContenedoresDto());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<ContenedorDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contenedorService.obtenerContenedorDtoPorId(id));
    }

    @GetMapping("/en_deposito")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<List<ContenedorDTO>> obtenerContenedoresEnDeposito() {
        return ResponseEntity.ok(contenedorService.obtenerContenedoresEnDepositoDto());
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<ContenedorDTO> crearContenedor(@RequestBody ContenedorDTO contenedor) {
        ContenedorDTO creado = contenedorService.crearContenedor(contenedor);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<ContenedorDTO> actualizarContenedor(@PathVariable Long id, @RequestBody ContenedorDTO contenedor) {
        ContenedorDTO actualizado = contenedorService.actualizarContenedor(contenedor, id);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/no_entregados")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<List<ContenedorDTO>> obtenerContenedoresNoEntregados() {
        return ResponseEntity.ok(contenedorService.obtenerContenedoresNoEntregadosDto());
    }
}
