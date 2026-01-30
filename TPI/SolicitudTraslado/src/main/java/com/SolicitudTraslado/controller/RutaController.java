package com.SolicitudTraslado.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.SolicitudTraslado.dto.RutaDTO;
import com.SolicitudTraslado.services.RutaService;
import com.SolicitudTraslado.domain.SolicitudTraslado;
import com.SolicitudTraslado.services.SolicitudTrasladoService;
import java.util.Map;
import java.util.HashMap; 
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes/rutas")
public class RutaController {
    private final RutaService rutaService;
    private final SolicitudTrasladoService solicitudTrasladoService;

    public RutaController(RutaService rutaService, SolicitudTrasladoService solicitudTrasladoService) {
        this.rutaService = rutaService;
        this.solicitudTrasladoService = solicitudTrasladoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<RutaDTO> crearRuta(@RequestBody RutaDTO ruta) {
        RutaDTO creado = rutaService.crearRutaDesdeDto(ruta);
        return ResponseEntity.ok(creado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<RutaDTO> obtenerRutaPorId(@PathVariable Long id) {
        RutaDTO ruta = rutaService.obtenerRutaDtoPorId(id);
        if (ruta != null) {
            return ResponseEntity.ok(ruta);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<RutaDTO> actualizarRuta(@PathVariable Long id, @RequestBody RutaDTO ruta) {
        ruta.setId(id);
        RutaDTO actualizado = rutaService.actualizarRutaDesdeDto(ruta);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<Long, RutaDTO>> listarRutas() {
        Map<Long, RutaDTO> rutas = rutaService.listarRutasDto();
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/para_asignar")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<Long, RutaDTO>> obtenerRutasParaAsignarASolicitud(@RequestParam Long solicitudId) {
        SolicitudTraslado solicitud;
        try {
            solicitud = solicitudTrasladoService.obtenerSolicitudPorNumero(solicitudId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
        List<RutaDTO> rutas = rutaService.obtenerRutasParaAsignarDto(solicitud);
        Map<Long, RutaDTO> rutaMap = new HashMap<>();
        for (RutaDTO ruta : rutas) {
            rutaMap.put(ruta.getId(), ruta);
        }
        return ResponseEntity.ok(rutaMap);
    }
    
}
