package com.SolicitudTraslado.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.dto.TransportistaDTO;
import com.SolicitudTraslado.services.TransportistaService;

@RestController
@RequestMapping("/api/solicitudes/transportistas")
public class TransportistaController {

    private final TransportistaService transportistaService;

    public TransportistaController(TransportistaService transportistaService) {
        this.transportistaService = transportistaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<TransportistaDTO>> listarTransportistas() {
        List<TransportistaDTO> transportistas = transportistaService.listarTransportistas().stream()
                .map(DtoMapper::toTransportistaDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transportistas);
    }

    @GetMapping("/{dni}")
    @PreAuthorize("hasRole('OPERADOR') or hasRole('TRANSPORTISTA')")
    public ResponseEntity<TransportistaDTO> obtenerTransportistaPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(DtoMapper.toTransportistaDto(transportistaService.obtenerTransportistaPorDni(dni)));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TransportistaDTO> crearTransportista(@RequestBody TransportistaDTO transportista) {
        return ResponseEntity.ok(DtoMapper
                .toTransportistaDto(transportistaService.crearTransportista(DtoMapper.toTransportistaEntity(transportista))));
    }

    @PutMapping("/{dni}")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TransportistaDTO> actualizarTransportista(@PathVariable String dni,
            @RequestBody TransportistaDTO transportista) {
        return ResponseEntity.ok(DtoMapper.toTransportistaDto(
                transportistaService.actualizarTransportista(DtoMapper.toTransportistaEntity(transportista), dni)));
    }
}
