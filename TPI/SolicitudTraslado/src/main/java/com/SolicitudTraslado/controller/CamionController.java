package com.SolicitudTraslado.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.SolicitudTraslado.dto.CamionDTO;
import com.SolicitudTraslado.services.CamionService;

@RestController
@RequestMapping("/api/solicitudes/camiones")
public class CamionController {
    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping("/{dominio}")
    @PreAuthorize("hasRole('TRANSPORTISTA') or hasRole('OPERADOR')")
    public ResponseEntity<CamionDTO> obtenerCamionPorId(@PathVariable String dominio) {
        CamionDTO camion = camionService.obtenerCamionDetallePorDominio(dominio);
        return ResponseEntity.ok(camion);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<CamionDTO>> listarCamiones() {
        return ResponseEntity.ok(camionService.listarCamiones());
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CamionDTO> crearCamion(@RequestBody CamionDTO camion) {
        CamionDTO nuevoCamion = camionService.crearCamion(camion);
        return ResponseEntity.ok(nuevoCamion);
    }

    @PutMapping("/{dominio}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<CamionDTO> actualizarCamion(@PathVariable String dominio, @RequestBody CamionDTO camion) {
        CamionDTO camionActualizado = camionService.actualizarCamion(camion, dominio);
        return ResponseEntity.ok(camionActualizado);
    }
}
