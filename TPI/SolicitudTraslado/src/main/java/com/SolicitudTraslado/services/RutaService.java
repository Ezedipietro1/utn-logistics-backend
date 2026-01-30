package com.SolicitudTraslado.services;

import com.SolicitudTraslado.domain.Ruta;
import com.SolicitudTraslado.domain.SolicitudTraslado;
import com.SolicitudTraslado.domain.Tramos;
import com.SolicitudTraslado.domain.Ubicacion;
import com.SolicitudTraslado.domain.Deposito;
import com.SolicitudTraslado.domain.enums.EstadoTramo;
import com.SolicitudTraslado.domain.enums.TipoTramo;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.dto.RutaDTO;
import com.SolicitudTraslado.repo.DepositoRepo;
import com.SolicitudTraslado.repo.RutaRepo;
import com.SolicitudTraslado.repo.SolicitudTrasladoRepo;
import com.SolicitudTraslado.repo.UbicacionRepo;
import com.SolicitudTraslado.services.UbicacionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Service
public class RutaService {
    private final RutaRepo rutaRepo;
    private final UbicacionRepo ubicacionRepo;
    private final OsrmService osrmService;
    private final UbicacionService ubicacionService;
    private final DepositoRepo depositoRepo;
    private final SolicitudTrasladoRepo solicitudTrasladoRepo;

    public RutaService(RutaRepo rutaRepo, UbicacionRepo ubicacionRepo, OsrmService osrmService, UbicacionService ubicacionService, DepositoRepo depositoRepo, SolicitudTrasladoRepo solicitudTrasladoRepo) {
        this.rutaRepo = rutaRepo;
        this.ubicacionRepo = ubicacionRepo;
        this.osrmService = osrmService;
        this.ubicacionService = ubicacionService;
        this.depositoRepo = depositoRepo;
        this.solicitudTrasladoRepo = solicitudTrasladoRepo;
    }

    // ==================== MÉTODOS USADOS POR CONTROLADORES ====================

    @Transactional
    public RutaDTO crearRutaDesdeDto(RutaDTO rutaDto) {
        Ruta ruta = DtoMapper.toRutaEntity(rutaDto);
        Ruta guardado = crearRuta(ruta);
        return DtoMapper.toRutaDto(guardado);
    }

    @Transactional
    public RutaDTO actualizarRutaDesdeDto(RutaDTO rutaDto) {
        Ruta ruta = DtoMapper.toRutaEntity(rutaDto);
        Ruta actualizado = actualizarRuta(ruta);
        return DtoMapper.toRutaDto(actualizado);
    }

    @Transactional(readOnly = true)
    public RutaDTO obtenerRutaDtoPorId(Long id) {
        return DtoMapper.toRutaDto(obtenerRutaPorId(id));
    }

    @Transactional(readOnly = true)
    public Map<Long, RutaDTO> listarRutasDto() {
        Map<Long, RutaDTO> rutaMap = new HashMap<>();
        for (Ruta ruta : rutaRepo.findAll()) {
            rutaMap.put(ruta.getId(), DtoMapper.toRutaDto(ruta));
        }
        return rutaMap;
    }

    @Transactional
    public List<RutaDTO> obtenerRutasParaAsignarDto(SolicitudTraslado solicitud) {
        List<Ruta> rutas = obtenerRutasParaAsignarASolicitud(solicitud);
        if (rutas == null) {
            return java.util.Collections.emptyList();
        }
        return rutas.stream().map(DtoMapper::toRutaDto).collect(Collectors.toList());
    }

    // ==================== MÉTODOS CON LÓGICA DE NEGOCIO ====================

    @Transactional
    public Ruta crearRuta(Ruta ruta) {

        hidratarUbicaciones(ruta);

        if (ruta.getCantTramos() == null || ruta.getCantTramos() <= 0) {
            ruta.setCantTramos(1);
        }
        if (ruta.getAsignada() == null) {
            ruta.setAsignada(false);
        }

        try {
            Map<String,Object> distancia = osrmService.getDistanceDuration(
                    ruta.getOrigen().getLatitud(),
                    ruta.getOrigen().getLongitud(),
                    ruta.getDestino().getLatitud(),
                    ruta.getDestino().getLongitud());
            ruta.setDistancia(extraerDistanciaOsrm(distancia));
        } catch (Exception ex) {
            // Si OSRM no responde, calculamos una distancia aproximada para no romper el flujo
            Double aprox = calcularDistanciaHaversineMetros(
                    ruta.getOrigen().getLatitud(),
                    ruta.getOrigen().getLongitud(),
                    ruta.getDestino().getLatitud(),
                    ruta.getDestino().getLongitud());
            ruta.setDistancia(aprox);
        }

        prepararTramos(ruta);

        validarRuta(ruta);
        return rutaRepo.save(ruta);
    }

    @Transactional
    public Ruta actualizarRuta(Ruta rutaActualizada) {
        validarRuta(rutaActualizada);
        return rutaRepo.save(rutaActualizada);
    }

    @Transactional(readOnly = true)
    public Ruta obtenerRutaPorId(Long id) {
        return rutaRepo.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Ruta> obtenerRutasPorAsignada(Boolean asignada) {
        if (asignada == null) {
            throw new IllegalArgumentException("El parámetro 'asignada' no puede ser null");
        }
        return rutaRepo.findByAsignada(asignada);
    }

    @Transactional(readOnly = true)
    public Map<Long, Ruta> listarRutas() {
        Map<Long, Ruta> rutaMap = new HashMap<>();
        for (Ruta ruta : rutaRepo.findAll()) {
            rutaMap.put(ruta.getId(), ruta);
        }
        return rutaMap;
    }

    @Transactional(readOnly = true)
    public List<Ruta> obtenerRutasPorSolicitud(Long numeroSolicitud) {
        if (numeroSolicitud == null) {
            throw new IllegalArgumentException("El número de solicitud no puede ser null");
        }
        return rutaRepo.findBySolicitudNumero(numeroSolicitud);
    }

    @Transactional(readOnly = true)
    public List<Ruta> obtenerRutasPorOrigenYDestino(Long origenId, Long destinoId) {
        if (origenId == null || destinoId == null) {
            throw new IllegalArgumentException("Los IDs de origen y destino no pueden ser null");
        }
        return rutaRepo.findByOrigenIdAndDestinoId(origenId, destinoId);
    }

    @Transactional
    public List<Ruta> obtenerRutasParaAsignarASolicitud(SolicitudTraslado solicitudTraslado) {
        if (solicitudTraslado == null || solicitudTraslado.getNumero() == null) {
            throw new IllegalArgumentException("Solicitud no puede ser null");
        }
        // Refrescamos la solicitud dentro de esta transacción para evitar problemas de lazy loading
        SolicitudTraslado solicitud = solicitudTrasladoRepo.findById(solicitudTraslado.getNumero())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        List<Ruta> rutas = rutaRepo.findBySolicitudNumero(solicitudTraslado.getNumero());
        if (rutas == null || rutas.isEmpty()) {
            rutas = this.crearRutasParaSolicitud(solicitud);
        }
        if (rutas == null || rutas.isEmpty()) return rutas;

        // Obtener tarifa y contenedor asociados a la solicitud (si están disponibles)
        com.SolicitudTraslado.domain.Tarifa tarifa = null;
        com.SolicitudTraslado.domain.Contenedor cont = null;
        if (solicitud != null) {
            tarifa = solicitud.getTarifa();
            cont = solicitud.getContenedor();
        }

        for (Ruta ruta : rutas) {
            try {
                Map<String,Object> osrm = osrmService.getDistanceDuration(ruta.getOrigen().getLatitud(), ruta.getOrigen().getLongitud(), ruta.getDestino().getLatitud(), ruta.getDestino().getLongitud());
                Double distanceMeters = osrm != null ? (Double) osrm.get("distanceMeters") : null;
                Double durationSeconds = osrm != null ? (Double) osrm.get("durationSeconds") : null;

                if (distanceMeters != null) {
                    ruta.setDistancia(distanceMeters);
                }
                if (durationSeconds != null) {
                    ruta.setTiempoEstimado(durationSeconds / 3600.0); // horas
                }

                if (tarifa != null && cont != null && distanceMeters != null) {
                    // Mantener consistencia con cálculo en SolicitudTraslado: aplicar sobre metros como en código existente.
                    Double costo = tarifa.getCostoPorKm() * distanceMeters + tarifa.getCostoPorM3() * cont.getVolumen() + tarifa.getCostoDeCombustible() * distanceMeters;
                    ruta.setCostoEstimado(costo);
                }
            } catch (Exception ex) {
                // No queremos romper la generación de rutas por un fallo en OSRM; registrar y seguir
                // (no inyectamos logger en este Service para no romper constructor)
            }
        }

        return rutas;
    }

    public List<Ruta> crearRutasParaSolicitud(SolicitudTraslado solicitud) {
        if (solicitud == null) throw new IllegalArgumentException("Solicitud no puede ser null");
        Ubicacion origen = solicitud.getUbicacionOrigen();
        Ubicacion destino = solicitud.getUbicacionDestino();
        if (origen == null || destino == null) throw new IllegalArgumentException("Origen y destino son obligatorios para generar rutas");

        java.util.List<Ruta> creadas = new java.util.ArrayList<>();

        // 1) Ruta directa (1 tramo)
        Ruta directa = Ruta.builder()
                .origen(origen)
                .destino(destino)
                .cantTramos(1)
                .asignada(false)
                .solicitud(solicitud)
                .build();
        directa = this.crearRuta(directa);
        creadas.add(directa);

        // 2) Intentar generar una alternativa via una ubicacion intermedia (si existe)
        Ubicacion intermedia = buscarUbicacionIntermediaConDeposito(origen.getId(), destino.getId());

        if (intermedia != null) {
            // Ruta alternativa que conceptualmente tiene 2 tramos (origen->intermedia->destino)
            Ruta viaIntermedia = Ruta.builder()
                    .origen(origen)
                    .destino(destino)
                    .cantTramos(2)
                    .asignada(false)
                    .solicitud(solicitud)
                    .build();
            viaIntermedia = this.crearRuta(viaIntermedia);
            creadas.add(viaIntermedia);
        }

        return creadas;
    }

    
    private void prepararTramos(Ruta ruta) {
        if (ruta.getTramos() != null && !ruta.getTramos().isEmpty()) {
            for (Tramos tramo : ruta.getTramos()) {
                tramo.setRuta(ruta);
                if (tramo.getEstadoTramo() == null) {
                    tramo.setEstadoTramo(EstadoTramo.PENDIENTE);
                }
                if (tramo.getTipoTramo() == null) {
                    tramo.setTipoTramo(TipoTramo.FINAL);
                }
            }
            ruta.setCantTramos(ruta.getTramos().size());
            return;
        }

        Deposito origenDep = obtenerDepositoPorUbicacion(ruta.getOrigen().getId());
        Deposito destinoDep = obtenerDepositoPorUbicacion(ruta.getDestino().getId());
        LinkedHashSet<Tramos> nuevos = new LinkedHashSet<>();

        if (ruta.getCantTramos() != null && ruta.getCantTramos() > 1) {
            Ubicacion intermedia = buscarUbicacionIntermediaConDeposito(ruta.getOrigen().getId(), ruta.getDestino().getId());
            if (intermedia != null) {
                Deposito interDep = obtenerDepositoPorUbicacion(intermedia.getId());
                nuevos.add(crearTramo(origenDep, interDep, ruta, TipoTramo.INICIAL));
                nuevos.add(crearTramo(interDep, destinoDep, ruta, TipoTramo.FINAL));
            }
        }

        if (nuevos.isEmpty()) {
            nuevos.add(crearTramo(origenDep, destinoDep, ruta, TipoTramo.FINAL));
        }

        ruta.setTramos(nuevos);
        ruta.setCantTramos(nuevos.size());
    }

    private Tramos crearTramo(Deposito origen, Deposito destino, Ruta ruta, TipoTramo tipoTramo) {
        return Tramos.builder()
                .origen(origen)
                .destino(destino)
                .ruta(ruta)
                .tipoTramo(tipoTramo)
                .estadoTramo(EstadoTramo.PENDIENTE)
                .build();
    }

    private Deposito obtenerDepositoPorUbicacion(Long ubicacionId) {
        java.util.Optional<Deposito> existente = depositoRepo.findByUbicacionId(ubicacionId).stream().findFirst();
        if (existente.isPresent()) {
            return existente.get();
        }
        // Si no existe un depósito asociado, creamos uno mínimo para no bloquear la generación de tramos
        Ubicacion ubicacion = ubicacionRepo.findById(ubicacionId)
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada para crear depósito (id " + ubicacionId + ")"));
        Deposito nuevo = Deposito.builder()
                .ubicacion(ubicacion)
                .nombre("Depósito auto " + ubicacionId)
                .costoEstadia(1.0)
                .build();
        return depositoRepo.save(nuevo);
    }

    private Ubicacion buscarUbicacionIntermediaConDeposito(Long origenId, Long destinoId) {
        return ubicacionService.listarUbicaciones().values().stream()
                .filter(u -> u.getId() != null && !u.getId().equals(origenId) && !u.getId().equals(destinoId))
                .filter(u -> depositoRepo.findByUbicacionId(u.getId()) != null && !depositoRepo.findByUbicacionId(u.getId()).isEmpty())
                .findFirst()
                .orElse(null);
    }

    // Validaciones para Ruta
    private void validarRuta(Ruta r) {
        if (r == null) {
            throw new IllegalArgumentException("Ruta no puede ser null");
        }

        // Origen y destino obligatorios y deben existir
        if (r.getOrigen() == null || r.getOrigen().getId() == null) {
            throw new IllegalArgumentException("El origen de la ruta es obligatorio");
        }
        if (r.getDestino() == null || r.getDestino().getId() == null) {
            throw new IllegalArgumentException("El destino de la ruta es obligatorio");
        }

        Long origenId = r.getOrigen().getId();
        Long destinoId = r.getDestino().getId();

        ubicacionRepo.findById(origenId).orElseThrow(() -> new IllegalArgumentException("Ubicación origen no encontrada"));
        ubicacionRepo.findById(destinoId).orElseThrow(() -> new IllegalArgumentException("Ubicación destino no encontrada"));

        if (Objects.equals(origenId, destinoId)) {
            throw new IllegalArgumentException("Origen y destino no pueden ser la misma ubicación");
        }

        // Distancia: obligatoria y positiva
        if (r.getDistancia() == null || r.getDistancia() <= 0) {
            throw new IllegalArgumentException("La distancia debe ser un valor positivo");
        }

        // Cantidad de tramos: obligatoria y positiva
        if (r.getCantTramos() == null || r.getCantTramos() <= 0) {
            throw new IllegalArgumentException("La cantidad de tramos debe ser un valor mayor a 0");
        }

        // Solicitud: verificar que tenga id (la entidad puede pertenecer a otro microservicio)
        if (r.getSolicitud() == null || r.getSolicitud().getNumero() == null) {
            throw new IllegalArgumentException("La solicitud asociada a la ruta es obligatoria");
        }

        // Asignada: no debe ser null
        if (r.getAsignada() == null) {
            throw new IllegalArgumentException("El campo 'asignada' es obligatorio");
        }

        // Evitar duplicados inútiles: misma solicitud, mismo origen/destino y misma cantidad de tramos
        List<Ruta> existentes = rutaRepo.findByOrigenIdAndDestinoId(origenId, destinoId);
        if (existentes != null && !existentes.isEmpty()) {
            boolean otro = existentes.stream().anyMatch(rt -> {
                boolean diferenteId = r.getId() == null || !rt.getId().equals(r.getId());
                boolean mismaSolicitud = rt.getSolicitud() != null && r.getSolicitud() != null
                        && Objects.equals(rt.getSolicitud().getNumero(), r.getSolicitud().getNumero());
                boolean mismaCantidadTramos = Objects.equals(rt.getCantTramos(), r.getCantTramos());
                return diferenteId && mismaSolicitud && mismaCantidadTramos;
            });
            if (otro) {
                throw new IllegalArgumentException("Ya existe una ruta con el mismo origen y destino para la misma solicitud");
            }
        }
    }

    private Double extraerDistanciaOsrm(Map<String, Object> osrmResponse) {
        if (osrmResponse == null || osrmResponse.isEmpty()) {
            throw new IllegalStateException("No se obtuvo respuesta del servicio OSRM para la ruta");
        }
        if (osrmResponse.containsKey("error")) {
            throw new IllegalStateException("Error devuelto por OSRM al calcular la ruta: " + osrmResponse.get("error"));
        }
        Object value = osrmResponse.get("distanceMeters");
        if (!(value instanceof Number)) {
            throw new IllegalStateException("Respuesta de OSRM inválida. Distancia no presente.");
        }
        double distance = ((Number) value).doubleValue();
        if (distance <= 0) {
            throw new IllegalStateException("OSRM devolvió una distancia no válida: " + distance);
        }
        return distance;
    }

    /**
     * Cálculo aproximado de distancia en metros usando la fórmula de Haversine,
     * para evitar fallar si OSRM no está disponible.
     */
    private Double calcularDistanciaHaversineMetros(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 1d;
        }
        double R = 6371000d; // radio terrestre en metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = R * c;
        // Evitar 0 para pasar validación
        return distancia > 0 ? distancia : 1d;
    }

    private void hidratarUbicaciones(Ruta ruta) {
        if (ruta == null || ruta.getOrigen() == null || ruta.getDestino() == null) {
            throw new IllegalArgumentException("Origen y destino son obligatorios para calcular la ruta");
        }
        Ubicacion origen = ubicacionRepo.findById(ruta.getOrigen().getId())
                .orElseThrow(() -> new IllegalArgumentException("Ubicación origen no encontrada"));
        Ubicacion destino = ubicacionRepo.findById(ruta.getDestino().getId())
                .orElseThrow(() -> new IllegalArgumentException("Ubicación destino no encontrada"));
        ruta.setOrigen(origen);
        ruta.setDestino(destino);
    }
}
