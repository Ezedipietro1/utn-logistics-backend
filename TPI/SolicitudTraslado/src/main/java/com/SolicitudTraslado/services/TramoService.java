package com.SolicitudTraslado.services;

import com.SolicitudTraslado.domain.Camion;
import com.SolicitudTraslado.domain.Tramos;
import com.SolicitudTraslado.domain.enums.EstadoContenedor;
import com.SolicitudTraslado.domain.enums.TipoTramo;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.dto.TramoDTO;
import com.SolicitudTraslado.repo.TramoRepo;
import com.SolicitudTraslado.repo.CamionRepo;
import com.SolicitudTraslado.repo.UbicacionRepo;
import com.SolicitudTraslado.repo.RutaRepo;
import com.SolicitudTraslado.repo.ContenedorRepo;
import java.util.Objects;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.sql.Date;
import java.util.stream.Collectors;

@Service
public class TramoService {
    private final TramoRepo tramoRepo;
    private final CamionRepo camionRepo;
    private final UbicacionRepo ubicacionRepo;
    private final RutaRepo rutaRepo;
    private final ContenedorRepo contenedorRepo;

    public TramoService(TramoRepo tramoRepo, CamionRepo camionRepo, UbicacionRepo ubicacionRepo, RutaRepo rutaRepo, ContenedorRepo contenedorRepo) {
        this.tramoRepo = tramoRepo;
        this.camionRepo = camionRepo;
        this.ubicacionRepo = ubicacionRepo;
        this.rutaRepo = rutaRepo;
        this.contenedorRepo = contenedorRepo;
    }

    @Transactional
    public TramoDTO crearTramo(TramoDTO tramoDto) {
        Tramos tramo = DtoMapper.toTramoEntity(tramoDto);
        Tramos guardado = crearTramo(tramo);
        return DtoMapper.toTramoDto(guardado);
    }

    @Transactional
    public Tramos crearTramo(Tramos tramo) {
        verificarTramo(tramo);
        return tramoRepo.save(tramo);
    }

    @Transactional
    public TramoDTO actualizarTramo(TramoDTO tramoActualizado) {
        Tramos tramo = DtoMapper.toTramoEntity(tramoActualizado);
        Tramos guardado = actualizarTramo(tramo);
        return DtoMapper.toTramoDto(guardado);
    }

    @Transactional
    public Tramos actualizarTramo(Tramos tramoActualizado) {
        verificarTramo(tramoActualizado);
        return tramoRepo.save(tramoActualizado);
    }

    @Transactional 
   public Tramos finalizarTramo(Long id) {
        Tramos tramo = tramoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado con id: " + id));

        if (tramo.getFechaFin() != null) {
            throw new IllegalArgumentException("El tramo ya ha sido finalizado");
        }

        // Si nunca se marcó fecha de inicio, la seteamos ahora para poder calcular duración (al menos 0)
        if (tramo.getFechaInicio() == null) {
            tramo.setFechaInicio(new Date(System.currentTimeMillis()));
        }
        tramo.setFechaFin(new Date(System.currentTimeMillis()));
        tramo.setEstadoTramo(com.SolicitudTraslado.domain.enums.EstadoTramo.COMPLETADO);
        tramoRepo.save(tramo);
        return tramo;
    }

    @Transactional
    public TramoDTO finalizarTramoDto(Long id) {
        return DtoMapper.toTramoDto(finalizarTramo(id));
    }

    @Transactional
    public Tramos iniciaTramos(Long id) {
        Tramos tramo = tramoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado con id: " + id));

        if (tramo.getFechaInicio() != null) {
            throw new IllegalArgumentException("El tramo ya ha sido iniciado");
        }

        tramo.setFechaInicio(new Date(System.currentTimeMillis()));
        tramo.setEstadoTramo(com.SolicitudTraslado.domain.enums.EstadoTramo.EN_PROGRESO);
        tramoRepo.save(tramo);
        actualizarEstadoContenedorEnViaje(tramo);
        return tramo;
    }

    @Transactional
    public TramoDTO iniciaTramosDto(Long id) {
        return DtoMapper.toTramoDto(iniciaTramos(id));
    }

    private void actualizarEstadoContenedorEnViaje(Tramos tramo) {
        if (tramo == null || tramo.getRuta() == null || tramo.getRuta().getId() == null) {
            return;
        }
        // Recuperar ruta con la solicitud asociada
        com.SolicitudTraslado.domain.Ruta ruta = rutaRepo.findById(tramo.getRuta().getId()).orElse(null);
        if (ruta == null || ruta.getSolicitud() == null || ruta.getSolicitud().getContenedor() == null) {
            return;
        }
        Long contenedorId = ruta.getSolicitud().getContenedor().getId();
        if (contenedorId == null) return;
        contenedorRepo.findById(contenedorId).ifPresent(c -> {
            c.setEstadoContenedor(EstadoContenedor.EN_VIAJE);
            contenedorRepo.save(c);
        });
    }

    @Transactional(readOnly = true)
    public Tramos obtenerTramoPorId(Long id) {
        return tramoRepo.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public TramoDTO obtenerTramoDtoPorId(Long id) {
        return DtoMapper.toTramoDto(obtenerTramoPorId(id));
    }

    @Transactional(readOnly = true)
    public List<Tramos> obtenerTramosPorTipoTramo(TipoTramo tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("TipoTramo no puede ser null");
        }
        return tramoRepo.findByTipoTramo(tipo);
    }

    @Transactional(readOnly = true)
    public Map<Long, Tramos> listarTramos() {
        Map<Long, Tramos> tramoMap = new HashMap<>();
        for (Tramos tramo : tramoRepo.findAll()) {
            tramoMap.put(tramo.getId(), tramo);
        }
        return tramoMap;
    }

    @Transactional(readOnly = true)
    public Map<Long, TramoDTO> listarTramosDto() {
        Map<Long, TramoDTO> tramoMap = new HashMap<>();
        for (Tramos tramo : tramoRepo.findAll()) {
            tramoMap.put(tramo.getId(), DtoMapper.toTramoDto(tramo));
        }
        return tramoMap;
    }

    @Transactional(readOnly = true)
    public List<Tramos> obtenerTramosPorCamionDominio(String dominio) {
        if (dominio == null || dominio.trim().isEmpty()) {
            throw new IllegalArgumentException("Dominio del camión no puede ser null o vacío");
        }
        return tramoRepo.findByCamionDominio(dominio);
    }

    @Transactional(readOnly = true)
    public List<TramoDTO> obtenerTramosDtoPorCamionDominio(String dominio) {
        return obtenerTramosPorCamionDominio(dominio).stream()
                .map(DtoMapper::toTramoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void asignarCamionATramo(Tramos tramo, Camion camion) {
        if (tramo == null) {
            throw new IllegalArgumentException("Tramo no puede ser null");
        }
        if (camion == null || camion.getDominio() == null || camion.getDominio().trim().isEmpty()) {
            throw new IllegalArgumentException("Dominio del camión no puede ser null o vacío");
        }

        // Verificar que el camión existe
        camionRepo.findById(camion.getDominio())
                .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado con dominio: " + camion.getDominio()));

        // Asignar el camión al tramo
        tramo.setCamion(camionRepo.findById(camion.getDominio()).get());
        tramoRepo.save(tramo);
    }

    // Validaciones para Tramos
    private void verificarTramo(Tramos t) {
        if (t == null) {
            throw new IllegalArgumentException("Tramo no puede ser null");
        }

        // Camion: si viene informado, debe existir
        if (t.getCamion() != null) {
            if (t.getCamion().getDominio() == null || t.getCamion().getDominio().trim().isEmpty()) {
                throw new IllegalArgumentException("El camión informado para el tramo debe tener dominio");
            }
            String dominio = t.getCamion().getDominio();
            camionRepo.findById(dominio)
                    .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado con dominio: " + dominio));
        }

        // Origen y destino: obligatorios y deben existir
        if (t.getOrigen() == null || t.getOrigen().getId() == null) {
            throw new IllegalArgumentException("El origen del tramo es obligatorio");
        }
        ubicacionRepo.findById(Objects.requireNonNull(t.getOrigen().getId())).orElseThrow(() -> new IllegalArgumentException("Ubicación origen no encontrada"));

        if (t.getDestino() == null || t.getDestino().getId() == null) {
            throw new IllegalArgumentException("El destino del tramo es obligatorio");
        }
        ubicacionRepo.findById(Objects.requireNonNull(t.getDestino().getId())).orElseThrow(() -> new IllegalArgumentException("Ubicación destino no encontrada"));

        // Fechas: fechaInicio obligatorio; si fechaFin existe debe ser posterior o igual
        if (t.getFechaFin() != null && t.getFechaInicio() != null) {
            java.sql.Date inicio = t.getFechaInicio();
            java.sql.Date fin = t.getFechaFin();
            if (fin.before(inicio)) {
                throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha de inicio");
            }
        } else if (t.getFechaFin() != null) {
            throw new IllegalArgumentException("La fecha fin no puede existir sin fecha de inicio");
        }

        // Ruta: obligatoria y debe existir
        if (t.getRuta() == null || t.getRuta().getId() == null) {
            throw new IllegalArgumentException("La ruta del tramo es obligatoria");
        }
        rutaRepo.findById(Objects.requireNonNull(t.getRuta().getId())).orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        // Tipo y estado: obligatorios
        if (t.getTipoTramo() == null) {
            throw new IllegalArgumentException("El tipo de tramo es obligatorio");
        }
        if (t.getEstadoTramo() == null) {
            throw new IllegalArgumentException("El estado del tramo es obligatorio");
        }
    }

    
    
}
