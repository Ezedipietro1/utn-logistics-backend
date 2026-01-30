package com.SolicitudTraslado.services;

import com.SolicitudTraslado.domain.Deposito;
import com.SolicitudTraslado.repo.DepositoRepo;
import com.SolicitudTraslado.domain.Ubicacion;
import com.SolicitudTraslado.repo.UbicacionRepo;
import com.SolicitudTraslado.dto.DepositoDTO;
import com.SolicitudTraslado.dto.DtoMapper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositoService {
    private final DepositoRepo depositoRepo;
    private final UbicacionRepo ubicacionRepo;

    public DepositoService(DepositoRepo depositoRepo, UbicacionRepo ubicacionRepo) {
        this.depositoRepo = depositoRepo;
        this.ubicacionRepo = ubicacionRepo;
    }

    @Transactional
    public DepositoDTO crearDeposito(DepositoDTO depositoDto) {
        Deposito deposito = DtoMapper.toDepositoEntity(depositoDto);
        validarDeposito(deposito);
        Deposito guardado = depositoRepo.save(deposito);
        return DtoMapper.toDepositoDto(guardado);
    }

    @Transactional
    public DepositoDTO actualizarDeposito(DepositoDTO depositoActualizado) {
        Deposito deposito = DtoMapper.toDepositoEntity(depositoActualizado);
        validarDeposito(deposito);
        Deposito guardado = depositoRepo.save(deposito);
        return DtoMapper.toDepositoDto(guardado);
    }

    @Transactional(readOnly = true)
    public Map<Long, DepositoDTO> listarDepositos() {
        List<Deposito> depositos = depositoRepo.findAll();
        Map<Long, DepositoDTO> depositoMap = new HashMap<>();
        for (Deposito dep : depositos) {
            depositoMap.put(dep.getId(), DtoMapper.toDepositoDto(dep));
        }
        return depositoMap;
    }

    @Transactional(readOnly = true)
    public List<DepositoDTO> obtenerDepositosPorUbicacionId(Long ubicacionId) {
        return depositoRepo.findByUbicacionId(ubicacionId).stream()
                .map(DtoMapper::toDepositoDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DepositoDTO> obtenerDepositosPorCiudadId(Long ciudadId) {
        return depositoRepo.findByCiudadId(ciudadId).stream()
                .map(DtoMapper::toDepositoDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepositoDTO obtenerDepositoPorId(Long id) {
        Deposito deposito = depositoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado con ID: " + id));
        return DtoMapper.toDepositoDto(deposito);
    }
    
    // Validaciones para Deposito y Ubicacion
    private void validarDeposito(Deposito d) {
        if (d == null) {
            throw new IllegalArgumentException("Deposito no puede ser null");
        }

        // Nombre: obligatorio, no vacío y sin dígitos
        if (Objects.isNull(d.getNombre()) || d.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del depósito es obligatorio");
        }
        if (Pattern.matches(".*\\d.*", d.getNombre())) {
            throw new IllegalArgumentException("El nombre del depósito no puede contener números");
        }

        // CostoEstadia: obligatorio y positivo
        if (d.getCostoEstadia() == null || d.getCostoEstadia() <= 0) {
            throw new IllegalArgumentException("El costo de estadía debe ser un valor positivo");
        }

        // Ubicacion: si viene, validar sus campos
        Ubicacion u = d.getUbicacion();
        if (u != null) {
            // Verificar que la Ubicacion exista en la base de datos
            Ubicacion ubicacionExistente = ubicacionRepo.findById(u.getId())
                    .orElseThrow(() -> new IllegalArgumentException("La ubicación especificada no existe"));

            // Dirección: obligatorio y no vacío
            if (Objects.isNull(ubicacionExistente.getDireccion()) || ubicacionExistente.getDireccion().trim().isEmpty()) {
                throw new IllegalArgumentException("La dirección de la ubicación es obligatoria");
            }

            // Ciudad: obligatorio
            if (ubicacionExistente.getCiudad() == null) {
                throw new IllegalArgumentException("La ciudad de la ubicación es obligatoria");
            }
        } else {
            throw new IllegalArgumentException("La ubicación del depósito es obligatoria");
        }
    }
}
