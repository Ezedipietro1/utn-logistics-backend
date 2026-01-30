package com.SolicitudTraslado.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.SolicitudTraslado.dto.UbicacionDTO;
import com.SolicitudTraslado.services.UbicacionService;

@RestController
@RequestMapping("/api/solicitudes/ubicaciones")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<Long, UbicacionDTO>> listarUbicaciones() {
        return ResponseEntity.ok(ubicacionService.listarUbicacionesDto());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<UbicacionDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ubicacionService.obtenerUbicacionDtoPorId(id));
    }

    @GetMapping("/por-ciudad")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<UbicacionDTO>> obtenerPorCiudad(@RequestParam("ciudadId") Long ciudadId) {
        return ResponseEntity.ok(ubicacionService.obtenerUbicacionesDtoPorCiudadId(ciudadId));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<UbicacionDTO> crearUbicacion(@RequestBody UbicacionDTO ubicacion) {
        UbicacionDTO creada = ubicacionService.crearUbicacion(ubicacion);
        return ResponseEntity.ok(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<UbicacionDTO> actualizarUbicacion(@PathVariable Long id, @RequestBody UbicacionDTO ubicacion) {
        ubicacion.setId(id);
        UbicacionDTO actualizada = ubicacionService.actualizarUbicacion(ubicacion);
        return ResponseEntity.ok(actualizada);
    }
}
