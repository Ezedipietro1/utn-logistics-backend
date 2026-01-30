package com.SolicitudTraslado.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SolicitudTraslado.domain.Contenedor;
import com.SolicitudTraslado.domain.enums.EstadoContenedor;
import com.SolicitudTraslado.dto.ContenedorDTO;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.repo.ContenedorRepo;

@Service
public class ContenedorService {

    private final ContenedorRepo contenedorRepo;

    public ContenedorService(ContenedorRepo contenedorRepo) {
        this.contenedorRepo = contenedorRepo;
    }

    // ==================== MÉTODOS USADOS POR CONTROLADORES ====================

    @Transactional
    public ContenedorDTO crearContenedor(ContenedorDTO contenedorDto) {
        Contenedor contenedor = crearContenedor(DtoMapper.toContenedorEntity(contenedorDto));
        return DtoMapper.toContenedorDto(contenedor);
    }

    @Transactional
    public ContenedorDTO actualizarContenedor(ContenedorDTO contenedorDto, Long id) {
        Contenedor actualizado = actualizarContenedor(DtoMapper.toContenedorEntity(contenedorDto), id);
        return DtoMapper.toContenedorDto(actualizado);
    }

    @Transactional(readOnly = true)
    public Map<Long, ContenedorDTO> listarContenedoresDto() {
        List<Contenedor> contenedores = contenedorRepo.findAll();
        Map<Long, ContenedorDTO> contenedorMap = new HashMap<>();
        for (Contenedor contenedor : contenedores) {
            contenedorMap.put(contenedor.getId(), DtoMapper.toContenedorDto(contenedor));
        }
        return contenedorMap;
    }

    @Transactional(readOnly = true)
    public ContenedorDTO obtenerContenedorDtoPorId(Long id) {
        return DtoMapper.toContenedorDto(obtenerContenedorPorId(id));
    }

    @Transactional(readOnly = true)
    public List<ContenedorDTO> obtenerContenedoresEnDepositoDto() {
        return contenedorRepo.findByEstadoContenedor(EstadoContenedor.EN_DEPOSITO).stream()
                .map(DtoMapper::toContenedorDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContenedorDTO> obtenerContenedoresNoEntregadosDto() {
        return contenedorRepo.findByEstadoContenedorNot(EstadoContenedor.ENTREGADO).stream()
                .map(DtoMapper::toContenedorDto)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS USADOS POR OTROS SERVICIOS ====================

    @Transactional
    public Contenedor crearContenedor(Contenedor contenedor) {
        // validamos que no exista otro contenedor con el mismo id
        if (contenedor.getId() != null && contenedorRepo.existsById(contenedor.getId())) {
            throw new IllegalArgumentException("Ya existe un contenedor con el mismo id: " + contenedor.getId());
        }

        // validamos los datos del contenedor
        // Si no viene estado, asignamos un estado por defecto para evitar constraint NOT NULL
        if (contenedor.getEstadoContenedor() == null) {
            contenedor.setEstadoContenedor(EstadoContenedor.EN_DEPOSITO);
        }
        validarContenedor(contenedor);

        return contenedorRepo.save(contenedor);
    }

    // servicio para actualizar un contenedor
    @Transactional
    public Contenedor actualizarContenedor(Contenedor contenedor, Long id) {
        Contenedor contenedorExistente = contenedorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado con id: " + id));

        // Copiamos los campos que pueden actualizarse
        contenedorExistente.setVolumen(contenedor.getVolumen());
        contenedorExistente.setPeso(contenedor.getPeso());
        contenedorExistente.setEstadoContenedor(contenedor.getEstadoContenedor());

        // validamos los datos del contenedor actualizado
        validarContenedor(contenedorExistente);

        return contenedorRepo.save(contenedorExistente);
    }

    @Transactional
    public Contenedor actualizarEstadoContenedor(Long id, EstadoContenedor estado) {
        Contenedor contenedor = contenedorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado con id: " + id));
        contenedor.setEstadoContenedor(estado);
        validarEstadoContenedor(estado);
        return contenedorRepo.save(contenedor);
    }

    // servicio para listar todos los contenedores en un mapa con su id como clave
    @Transactional(readOnly = true)
    public Map<Long, Contenedor> listarContenedores() {
        List<Contenedor> contenedores = contenedorRepo.findAll();
        Map<Long, Contenedor> contenedorMap = new HashMap<>();
        for (Contenedor contenedor : contenedores) {
            contenedorMap.put(contenedor.getId(), contenedor);
        }
        return contenedorMap;
    }

    @Transactional(readOnly = true)
    public Contenedor obtenerContenedorPorId(Long id) {
        return contenedorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado con id: " + id));
    }

    // servicio para obtener una lista de contenedores que esten en el deposito
    @Transactional(readOnly = true)
    public List<Contenedor> obtenerContenedoresEnDeposito() {
        return contenedorRepo.findByEstadoContenedor(EstadoContenedor.EN_DEPOSITO);
    }

    @Transactional(readOnly = true)
    public List<Contenedor> obtenerContenedoresNoEntregados() {
        return contenedorRepo.findByEstadoContenedorNot(EstadoContenedor.ENTREGADO);
    }

    // ==================== MÉTODOS PRIVADOS DE VALIDACIÓN ====================

    private void validarContenedor(Contenedor contenedor) {
        if (contenedor == null) {
            throw new IllegalArgumentException("El contenedor no puede ser null");
        }

        // Volumen: debe ser positivo
        if (contenedor.getVolumen() == null || contenedor.getVolumen() <= 0) {
            throw new IllegalArgumentException("El volumen debe ser un valor positivo");
        }

        // Peso: debe ser positivo
        if (contenedor.getPeso() == null || contenedor.getPeso() <= 0) {
            throw new IllegalArgumentException("El peso debe ser un valor positivo");
        }

        // Estado: si viene, debe ser un valor válido del enum
        if (contenedor.getEstadoContenedor() != null) {
            validarEstadoContenedor(contenedor.getEstadoContenedor());
        }
    }

    private void validarEstadoContenedor(EstadoContenedor estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del contenedor no puede ser null");
        }

        // Verificar que el estado sea uno de los valores válidos del enum
        boolean esValido = false;
        for (EstadoContenedor estadoValido : EstadoContenedor.values()) {
            if (estadoValido == estado) {
                esValido = true;
                break;
            }
        }

        if (!esValido) {
            throw new IllegalArgumentException(
                    "Estado de contenedor inválido. Estados permitidos: EN_ESPERA_RETIRO, RETIRADO, EN_VIAJE, EN_DEPOSITO, ENTREGADO");
        }
    }

}
