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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SolicitudTraslado.dto.DepositoDTO;
import com.SolicitudTraslado.services.DepositoService;

@RestController
@RequestMapping("/api/solicitudes/depositos")
public class DepositoController {

    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<Long, DepositoDTO>> listarDepositos() {
        return ResponseEntity.ok(depositoService.listarDepositos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<DepositoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(depositoService.obtenerDepositoPorId(id));
    }

    @GetMapping("/por_ubicacion")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<DepositoDTO>> obtenerPorUbicacion(@RequestParam("ubicacionId") Long ubicacionId) {
        return ResponseEntity.ok(depositoService.obtenerDepositosPorUbicacionId(ubicacionId));
    }

    @GetMapping("/por_ciudad")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<DepositoDTO>> obtenerPorCiudad(@RequestParam("ciudadId") Long ciudadId) {
        return ResponseEntity.ok(depositoService.obtenerDepositosPorCiudadId(ciudadId));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<DepositoDTO> crearDeposito(@RequestBody DepositoDTO deposito) {
        DepositoDTO creado = depositoService.crearDeposito(deposito);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<DepositoDTO> actualizarDeposito(@PathVariable Long id, @RequestBody DepositoDTO deposito) {
        deposito.setId(id);
        DepositoDTO actualizado = depositoService.actualizarDeposito(deposito);
        return ResponseEntity.ok(actualizado);
    }
}
