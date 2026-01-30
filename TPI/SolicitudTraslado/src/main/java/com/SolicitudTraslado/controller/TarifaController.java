package com.SolicitudTraslado.controller;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.SolicitudTraslado.dto.TarifaDTO;
import com.SolicitudTraslado.services.TarifaService;

@RestController
@RequestMapping("/api/solicitudes/tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<HashMap<Long, TarifaDTO>> listarTarifas() {
        return ResponseEntity.ok(tarifaService.listarTarifasDto());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('CLIENTE')")
    public ResponseEntity<TarifaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarifaService.obtenerTarifaDtoPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> crearTarifa(@RequestBody TarifaDTO tarifa) {
        TarifaDTO creada = tarifaService.crearTarifa(tarifa);
        return ResponseEntity.ok(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> actualizarTarifa(@PathVariable Long id, @RequestBody TarifaDTO tarifa) {
        tarifa.setId(id);
        TarifaDTO actualizada = tarifaService.actualizarTarifa(tarifa);
        return ResponseEntity.ok(actualizada);
    }

    // Endpoints para la tarifa única (actual)
    @GetMapping("/current")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> getTarifaActual() {
        TarifaDTO t = tarifaService.obtenerTarifaDto();
        return ResponseEntity.ok(t);
    }

    @PutMapping("/current")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> updateTarifaActual(@RequestBody TarifaDTO tarifa) {
        TarifaDTO actualizado = tarifaService.actualizarTarifa(tarifa);
        return ResponseEntity.ok(actualizado);
    }
}
