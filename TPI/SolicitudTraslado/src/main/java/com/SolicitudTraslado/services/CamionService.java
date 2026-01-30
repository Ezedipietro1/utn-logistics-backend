package com.SolicitudTraslado.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SolicitudTraslado.domain.Camion;
import com.SolicitudTraslado.domain.Transportista;
import com.SolicitudTraslado.dto.CamionDTO;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.repo.CamionRepo;
import com.SolicitudTraslado.repo.TransportistaRepo;

@Service
public class CamionService {

    private final CamionRepo camionRepo;
    private final TransportistaRepo transportistaRepo;

    public CamionService(CamionRepo camionRepo, TransportistaRepo transportistaRepo) {
        this.camionRepo = camionRepo;
        this.transportistaRepo = transportistaRepo;
    }

    // ==================== MÉTODOS USADOS POR CONTROLADORES ====================

    @Transactional
    public CamionDTO crearCamion(CamionDTO camionDto) {
        Camion camion = DtoMapper.toCamionEntity(camionDto);
        // validamos los datos del camion
        validarCamion(camion);

        // validamos si no existe otro camion con el mismo dominio
        if (camionRepo.existsById(camion.getDominio())) {
            throw new IllegalArgumentException("Ya existe un camión con el mismo dominio: " + camion.getDominio());
        }

        // obtenemos y validamos que el transportista exista --> tambien obtenemos su
        // dni para pasarlo directamente por postman
        String dniTransportista = null;
        if (camion.getTransportista() != null && camion.getTransportista().getDni() != null) {
            dniTransportista = camion.getTransportista().getDni();
        }

        if (dniTransportista == null || dniTransportista.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del transportista es obligatorio");
        }

        // Validamos que el transportista exista y lo asignamos al camión
        Transportista transportista = transportistaRepo.findById(dniTransportista)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un transportista con el DNI: "));
        camion.setTransportista(transportista);

        // Seteamos el estado por defecto si no viene
        if (camion.getEstado() == null) {
            camion.setEstado(true);
        }

        Camion guardado = camionRepo.save(camion);
        return DtoMapper.toCamionDto(guardado);
    }

    @Transactional
    public CamionDTO actualizarCamion(CamionDTO camionDto, String dominio) {
        Camion camionExistente = camionRepo.findById(dominio)
                .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado con dominio: " + dominio));

        // actualizo los campos que pueden ser modificados
        Camion data = DtoMapper.toCamionEntity(camionDto);
        validarCamion(data);

        camionExistente.setCapKg(data.getCapKg());
        camionExistente.setCapVolumen(data.getCapVolumen());
        camionExistente.setConsumo(data.getConsumo());
        camionExistente.setEstado(data.getEstado());

        if (data.getTransportista() != null && data.getTransportista().getDni() != null) {
            Transportista transportista = transportistaRepo.findById(data.getTransportista().getDni())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró un transportista con el DNI: " + data.getTransportista().getDni()));
            camionExistente.setTransportista(transportista);
        }

        Camion actualizado = camionRepo.save(camionExistente);
        return DtoMapper.toCamionDto(actualizado);

    }

    @Transactional(readOnly = true)
    public List<CamionDTO> listarCamiones() {
        return camionRepo.findAll().stream()
                .map(DtoMapper::toCamionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CamionDTO obtenerCamionDetallePorDominio(String dominio) {
        Camion camion = obtenerCamionPorDominio(dominio);
        return DtoMapper.toCamionDto(camion);
    }

    // ==================== MÉTODOS USADOS POR OTROS SERVICIOS ====================

    // analizar si es mejor devolver un map o el objeto camion directamente !!!!
    @Transactional(readOnly = true)
    public Camion obtenerCamionPorDominio(String dominio) {
        return camionRepo.findById(dominio)
                .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado con dominio: " + dominio));
    }

    // ==================== MÉTODOS PRIVADOS DE VALIDACIÓN ====================

    private void validarCamion(Camion camion) {
        if (camion == null) {
            throw new IllegalArgumentException("El camión no puede ser null");
        }

        // Dominio: no nulo y no vacío
        if (camion.getDominio() == null || camion.getDominio().trim().isEmpty()) {
            throw new IllegalArgumentException("El dominio del camión es obligatorio");
        }

        // Dominio: formato válido (letras y números, típicamente 6-7 caracteres)
        String dominio = camion.getDominio().trim();
        if (!dominio.matches("^[A-Z]{2,3}\\d{3}[A-Z]{0,2}$") && !dominio.matches("^[A-Z]{3}\\d{3}$")) {
            throw new IllegalArgumentException("El dominio debe tener un formato válido (ej: ABC123 o AB123CD)");
        }

        // Capacidad en kg: debe ser positiva
        if (camion.getCapKg() == null || camion.getCapKg() <= 0) {
            throw new IllegalArgumentException("La capacidad de carga en kg debe ser un valor positivo");
        }

        // Capacidad en volumen: debe ser positiva
        if (camion.getCapVolumen() == null || camion.getCapVolumen() <= 0) {
            throw new IllegalArgumentException("La capacidad de volumen debe ser un valor positivo");
        }

        // Consumo: si viene, debe ser no negativo
        if (camion.getConsumo() != null && camion.getConsumo() < 0) {
            throw new IllegalArgumentException("El consumo no puede ser negativo");
        }

        // Transportista: debe existir
        if (camion.getTransportista() == null || camion.getTransportista().getDni() == null) {
            throw new IllegalArgumentException("El transportista es obligatorio");
        }
    }

}
