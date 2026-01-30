package com.SolicitudTraslado.services;

import com.SolicitudTraslado.domain.Ciudad;
import com.SolicitudTraslado.dto.CiudadDTO;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.repo.CiudadRepo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CiudadService {
    private final CiudadRepo ciudadRepo;

    public CiudadService(CiudadRepo ciudadRepo) {
        this.ciudadRepo = ciudadRepo;
    }

    @Transactional(readOnly = true)
    public CiudadDTO obtenerCiudadPorId(Long id) {
        Ciudad ciudad = ciudadRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada con ID: " + id));
        return DtoMapper.toCiudadDto(ciudad);
    }

    @Transactional(readOnly = true)
    public CiudadDTO obtenerCiudadPorNombre(String nombre) {
        Ciudad ciudad = ciudadRepo.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada con nombre: " + nombre));
        return DtoMapper.toCiudadDto(ciudad);
    }
    
    @Transactional
    public CiudadDTO crearCiudad(CiudadDTO ciudadDto) {
        Ciudad ciudad = DtoMapper.toCiudadEntity(ciudadDto);
        validarCiudad(ciudad);
        Ciudad guardada = ciudadRepo.save(ciudad);
        return DtoMapper.toCiudadDto(guardada);
    }

    @Transactional
    public CiudadDTO actualizarCiudad(CiudadDTO ciudadActualizada) {  
        Ciudad ciudad = DtoMapper.toCiudadEntity(ciudadActualizada);
        validarCiudad(ciudad);
        Ciudad guardada = ciudadRepo.save(ciudad);
        return DtoMapper.toCiudadDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<CiudadDTO> listarCiudades() {
        return ciudadRepo.findAll().stream()
                .map(DtoMapper::toCiudadDto)
                .collect(Collectors.toList());
    }

    // Validación para Ciudad (similar al ejemplo del cliente)
    private void validarCiudad(Ciudad c) {
        if (c == null) {
            throw new IllegalArgumentException("Ciudad no puede ser null");
        }

        // Nombre: obligatorio y no vacío
        if (Objects.isNull(c.getNombre()) || c.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la ciudad es obligatorio");
        }

        String nombre = c.getNombre().trim();
        // No debe contener dígitos
        if (Pattern.matches(".*\\d.*", nombre)) {
            throw new IllegalArgumentException("El nombre de la ciudad no puede contener números");
        }

        // Longitud máxima razonable
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre de la ciudad es demasiado largo");
        }
    }
}
