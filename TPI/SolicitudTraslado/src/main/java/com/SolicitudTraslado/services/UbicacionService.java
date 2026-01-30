package com.SolicitudTraslado.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SolicitudTraslado.domain.Ciudad;
import com.SolicitudTraslado.domain.Ubicacion;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.dto.UbicacionDTO;
import com.SolicitudTraslado.repo.CiudadRepo;
import com.SolicitudTraslado.repo.UbicacionRepo;

@Service
public class UbicacionService {
    private final UbicacionRepo ubicacionRepo;
    private final CiudadRepo ciudadRepo;

    public UbicacionService(UbicacionRepo ubicacionRepo, CiudadRepo ciudadRepo) {
        this.ubicacionRepo = ubicacionRepo;
        this.ciudadRepo = ciudadRepo;
    }

    @Transactional(readOnly = true)
    public Ubicacion obtenerUbicacionPorId(Long id) {
        return ubicacionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public UbicacionDTO obtenerUbicacionDtoPorId(Long id) {
        return DtoMapper.toUbicacionDto(obtenerUbicacionPorId(id));
    }

    @Transactional
    public UbicacionDTO crearUbicacion(UbicacionDTO ubicacionDto) {
        Ubicacion ubicacion = DtoMapper.toUbicacionEntity(ubicacionDto);
        Ubicacion guardada = crearUbicacion(ubicacion);
        return DtoMapper.toUbicacionDto(guardada);
    }

    @Transactional
    public Ubicacion crearUbicacion(Ubicacion ubicacion) {
        validarUbicacion(ubicacion);

        // Si viene solo el ID de la ciudad, cargar la ciudad completa desde la BD
        if (ubicacion.getCiudad() != null && ubicacion.getCiudad().getId() != null) {
            Ciudad ciudadCompleta = ciudadRepo.findById(ubicacion.getCiudad().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ciudad no encontrada con ID: " + ubicacion.getCiudad().getId()));
            ubicacion.setCiudad(ciudadCompleta);
        }

        return ubicacionRepo.save(ubicacion);
    }

    @Transactional
    public Ubicacion actualizarUbicacion(Ubicacion ubicacionActualizada) {
        validarUbicacion(ubicacionActualizada);

        // Si viene solo el ID de la ciudad, cargar la ciudad completa desde la BD
        if (ubicacionActualizada.getCiudad() != null && ubicacionActualizada.getCiudad().getId() != null) {
            Ciudad ciudadCompleta = ciudadRepo.findById(ubicacionActualizada.getCiudad().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ciudad no encontrada con ID: " + ubicacionActualizada.getCiudad().getId()));
            ubicacionActualizada.setCiudad(ciudadCompleta);
        }

        return ubicacionRepo.save(ubicacionActualizada);
    }

    @Transactional
    public UbicacionDTO actualizarUbicacion(UbicacionDTO ubicacionActualizada) {
        Ubicacion ubicacion = DtoMapper.toUbicacionEntity(ubicacionActualizada);
        Ubicacion guardada = actualizarUbicacion(ubicacion);
        return DtoMapper.toUbicacionDto(guardada);
    }

    @Transactional(readOnly = true)
    public Map<Long, Ubicacion> listarUbicaciones() {
        List<Ubicacion> ubicaciones = ubicacionRepo.findAll();
        Map<Long, Ubicacion> ubicacionMap = new HashMap<>();
        for (Ubicacion ub : ubicaciones) {
            ubicacionMap.put(ub.getId(), ub);
        }
        return ubicacionMap;
    }

    @Transactional(readOnly = true)
    public Map<Long, UbicacionDTO> listarUbicacionesDto() {
        List<Ubicacion> ubicaciones = ubicacionRepo.findAll();
        Map<Long, UbicacionDTO> ubicacionMap = new HashMap<>();
        for (Ubicacion ub : ubicaciones) {
            ubicacionMap.put(ub.getId(), DtoMapper.toUbicacionDto(ub));
        }
        return ubicacionMap;
    }

    @Transactional(readOnly = true)
    public List<Ubicacion> obtenerUbicacionesPorCiudadId(Long ciudadId) {
        return ubicacionRepo.findByCiudadId(ciudadId);
    }

    @Transactional(readOnly = true)
    public List<UbicacionDTO> obtenerUbicacionesDtoPorCiudadId(Long ciudadId) {
        return ubicacionRepo.findByCiudadId(ciudadId).stream()
                .map(DtoMapper::toUbicacionDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Ubicacion> obtenerUbicacionesPorCiudad(Ciudad ciudad) {
        return ubicacionRepo.findByCiudad(ciudad);
    }

    // Validación para Ubicacion
    private void validarUbicacion(Ubicacion u) {
        if (u == null) {
            throw new IllegalArgumentException("Ubicación no puede ser null");
        }

        if (Objects.isNull(u.getDireccion()) || u.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }

        if (u.getLatitud() == null || u.getLongitud() == null) {
            throw new IllegalArgumentException("Latitud y longitud son obligatorias");
        }
        double lat = u.getLatitud();
        double lon = u.getLongitud();
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitud fuera de rango (-90..90)");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException("Longitud fuera de rango (-180..180)");
        }

        if (u.getCiudad() == null) {
            throw new IllegalArgumentException("La ciudad de la ubicación es obligatoria");
        }

        if (u.getCiudad().getId() == null) {
            // si no tiene id, exigir nombre válido
            if (Objects.isNull(u.getCiudad().getNombre()) || u.getCiudad().getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("La ciudad debe tener id o nombre");
            }
            if (Pattern.matches(".*\\d.*", u.getCiudad().getNombre())) {
                throw new IllegalArgumentException("El nombre de la ciudad no puede contener números");
            }
        }
    }
}
