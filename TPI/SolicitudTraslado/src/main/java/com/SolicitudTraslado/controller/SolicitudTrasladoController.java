package com.SolicitudTraslado.controller;

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

import com.SolicitudTraslado.domain.Ruta;
import com.SolicitudTraslado.domain.SolicitudTraslado;
import com.SolicitudTraslado.dto.SolicitudTrasladoCreateRequest;
import com.SolicitudTraslado.dto.SolicitudTrasladoDTO;
import com.SolicitudTraslado.services.RutaService;
import com.SolicitudTraslado.services.SolicitudTrasladoService;

@RestController
@RequestMapping("/api/solicitudes/solicitudTraslado")
public class SolicitudTrasladoController {

    private final SolicitudTrasladoService solicitudTrasladoService;
    private final RutaService rutaService;

    public SolicitudTrasladoController(SolicitudTrasladoService solicitudTrasladoService, RutaService rutaService) {
        this.solicitudTrasladoService = solicitudTrasladoService;
        this.rutaService = rutaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') or hasRole('OPERADOR')")
    public ResponseEntity<SolicitudTrasladoDTO> crear(
            @RequestBody SolicitudTrasladoCreateRequest request) {
        SolicitudTrasladoDTO creada = solicitudTrasladoService.crearSolicitudTrasladoDto(
                request.getClienteId(),
                request.getNombre(),
                request.getApellido(),
                request.getTelefono(),
                request.isActivo(),
                request.getEmail(),
                request.getVolumen(),
                request.getPeso(),
                request.getUbicacionOrigenId(),
                request.getUbicacionDestinoId());
        return ResponseEntity.ok(creada);
    }

    @GetMapping("/{numero}")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('OPERADOR')")
    public ResponseEntity<SolicitudTrasladoDTO> obtenerPorId(@PathVariable Long numero) {
        SolicitudTrasladoDTO solicitud = solicitudTrasladoService.obtenerSolicitudDtoPorNumero(numero);
        if (solicitud != null) {
            return ResponseEntity.ok(solicitud);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<Long, SolicitudTrasladoDTO>> listarSolicitudes() {
        Map<Long, SolicitudTrasladoDTO> solicitudes = solicitudTrasladoService.listarSolicitudesDto();
        return ResponseEntity.ok(solicitudes);
    }

    @PutMapping("/{numero}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<SolicitudTrasladoDTO> actualizar(@PathVariable Long numero,
            @RequestBody SolicitudTrasladoDTO solicitud) {
        solicitud.setNumero(numero);
        SolicitudTrasladoDTO actualizada = solicitudTrasladoService.actualizarSolicitudTraslado(solicitud);
        return ResponseEntity.ok(actualizada);
    }

    @PutMapping("/{numero}/asignar_ruta/{rutaId}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<SolicitudTrasladoDTO> asignarRuta(@PathVariable Long numero, @PathVariable Long rutaId) {
        SolicitudTraslado solicitud = solicitudTrasladoService.obtenerSolicitudPorNumero(numero);
        Ruta ruta = rutaService.obtenerRutaPorId(rutaId);
        if (solicitud == null || ruta == null) {
            return ResponseEntity.notFound().build();
        }
        solicitudTrasladoService.asignarRutaASolicitud(solicitud, ruta);
        return ResponseEntity.ok(solicitudTrasladoService.obtenerSolicitudDtoPorNumero(numero));
    }

    @PutMapping("/{numero}/asignarCamionATramo")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Map<String, String>> asignarCamionATramo(
            @PathVariable Long numero,
            @RequestParam Long tramoId,
            @RequestParam String dominioCamion) {
        try {
            solicitudTrasladoService.asignarCamionATramo(numero, tramoId, dominioCamion);
            return ResponseEntity.ok(java.util.Collections.singletonMap("mensaje", "Camión asignado al tramo correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PutMapping("/{numero}/finalizarSolicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<SolicitudTrasladoDTO> finalizarSolicitud(@PathVariable Long numero) {
        try {
            SolicitudTrasladoDTO finalizada = solicitudTrasladoService.finalizarSolicitudTrasladoDto(numero);
            return ResponseEntity.ok(finalizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
