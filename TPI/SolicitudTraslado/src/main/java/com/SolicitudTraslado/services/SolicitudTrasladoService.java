package com.SolicitudTraslado.services;

import com.SolicitudTraslado.domain.SolicitudTraslado;
import com.SolicitudTraslado.domain.enums.EstadoSolicitud;
import com.SolicitudTraslado.repo.SolicitudTrasladoRepo;

import com.SolicitudTraslado.domain.Ubicacion;
import com.SolicitudTraslado.domain.Camion;
import com.SolicitudTraslado.domain.Cliente;

import com.SolicitudTraslado.domain.Contenedor;
import com.SolicitudTraslado.domain.enums.EstadoContenedor;

import com.SolicitudTraslado.repo.RutaRepo;
import com.SolicitudTraslado.domain.Ruta;

import com.SolicitudTraslado.domain.Tarifa;
import com.SolicitudTraslado.repo.TarifaRepo;

import com.SolicitudTraslado.domain.Tramos;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.dto.SolicitudTrasladoDTO;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SolicitudTrasladoService {
    private static final Logger log = LoggerFactory.getLogger(SolicitudTrasladoService.class);
    private final SolicitudTrasladoRepo solicitudTrasladoRepo;
    private final RutaRepo rutaRepo;
    private final ContenedorService contenedorService;
    private final TarifaService tarifaService;
    private final TarifaRepo tarifaRepo;
    private final UbicacionService ubicacionService;
    private final OsrmService osrmService;
    private final TramoService tramoService;
    private final CamionService camionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SolicitudTrasladoService(SolicitudTrasladoRepo solicitudTrasladoRepo, RutaRepo rutaRepo, ContenedorService contenedorService, TarifaRepo tarifaRepo, TarifaService tarifaService, UbicacionService ubicacionService, OsrmService osrmService, TramoService tramoService, CamionService camionService) {
        this.solicitudTrasladoRepo = solicitudTrasladoRepo;
        this.rutaRepo = rutaRepo;
        this.contenedorService = contenedorService;
        this.tarifaRepo = tarifaRepo;
        this.tarifaService = tarifaService;
        this.ubicacionService = ubicacionService;
        this.osrmService = osrmService;
        this.tramoService = tramoService;
        this.camionService = camionService;
    }

    @Transactional
    public SolicitudTraslado crearSolicitudTraslado(Long clienteId, String nombre, String apellido, String telefono, boolean activo, String email, Double volumen, Double peso, Long ubicacionOrigenId, Long ubicacionDestinoId) {
        log.info("crearSolicitudTraslado params -> clienteId={}, email={}, volumen={}, peso={}, origenId={}, destinoId={}", clienteId, email, volumen, peso, ubicacionOrigenId, ubicacionDestinoId);

        Contenedor contenedorNuevo = new Contenedor();
        contenedorNuevo.setVolumen(volumen);
        contenedorNuevo.setPeso(peso);
        contenedorNuevo.setEstadoContenedor(EstadoContenedor.EN_ESPERA_RETIRO);
        contenedorNuevo = contenedorService.crearContenedor(contenedorNuevo);

        Cliente clienteNuevo = registrarClienteSiNecesario(clienteId, nombre, apellido, telefono, activo, email);

        Ubicacion origenCreada = ubicacionService.obtenerUbicacionPorId(ubicacionOrigenId);
        Ubicacion destinoCreada = ubicacionService.obtenerUbicacionPorId(ubicacionDestinoId);

        Tarifa tarifaDefault = tarifaService.obtenerTarifa();
        if (tarifaDefault == null) {
            throw new IllegalArgumentException("No se encontró una tarifa por defecto");
        }

        EstadoSolicitud estadoInicial = EstadoSolicitud.BORRADOR;

        Double distanciaMetros;
        Double duracionSegundos;
        try {
            Map<String,Object> osrmData = osrmService.getDistanceDuration(origenCreada.getLatitud(), origenCreada.getLongitud(), destinoCreada.getLatitud(), destinoCreada.getLongitud());
            distanciaMetros = extraerValorOsrm(osrmData, "distanceMeters", "distancia");
            duracionSegundos = extraerValorOsrm(osrmData, "durationSeconds", "duración");
        } catch (Exception ex) {
            log.warn("No se pudo obtener distancia/tiempo desde OSRM, se usan valores por defecto. Causa: {}", ex.getMessage());
            distanciaMetros = 0d;
            duracionSegundos = 0d;
        }

        Double costoEstimado = tarifaDefault.getCostoPorKm() * distanciaMetros / 1000
                + tarifaDefault.getCostoPorM3() * contenedorNuevo.getVolumen()
                + tarifaDefault.getCostoDeCombustible() * distanciaMetros / 1000;

        Double tiempoEstimado = duracionSegundos / 3600.0; // en horas

        SolicitudTraslado nuevaSolicitud = SolicitudTraslado.builder()
                .clienteId(clienteNuevo.getId())
                .contenedor(contenedorNuevo)
                .tarifa(tarifaDefault)
                .ubicacionOrigen(origenCreada)
                .ubicacionDestino(destinoCreada)
                .costoEstimado(costoEstimado)
                .tiempoEstimado(tiempoEstimado)
                .estado(estadoInicial)
                .build();
        return solicitudTrasladoRepo.save(nuevaSolicitud);
    }

    @Transactional
    public SolicitudTrasladoDTO crearSolicitudTrasladoDto(Long clienteId, String nombre, String apellido, String telefono, boolean activo, String email, Double volumen, Double peso, Long ubicacionOrigenId, Long ubicacionDestinoId) {
        SolicitudTraslado solicitud = crearSolicitudTraslado(clienteId, nombre, apellido, telefono, activo, email, volumen, peso, ubicacionOrigenId, ubicacionDestinoId);
        return DtoMapper.toSolicitudDto(solicitud);
    }

    @Transactional
    public void asignarRutaASolicitud(SolicitudTraslado solicitud, Ruta ruta) {
        if (solicitud == null || ruta == null) {
            throw new IllegalArgumentException("Solicitud y Ruta no pueden ser null");
        }
        solicitud.setRuta(ruta);
        solicitudTrasladoRepo.save(solicitud);

        ruta.setAsignada(true);
        rutaRepo.save(ruta);
    }

    @Transactional
    public SolicitudTraslado actualizarSolicitudTraslado(SolicitudTraslado solicitudActualizada) {
        validarSolicitud(solicitudActualizada);
        return solicitudTrasladoRepo.save(solicitudActualizada);
    }

    @Transactional
    public SolicitudTrasladoDTO actualizarSolicitudTraslado(SolicitudTrasladoDTO solicitudActualizada) {
        SolicitudTraslado solicitud = DtoMapper.toSolicitudEntity(solicitudActualizada);
        SolicitudTraslado guardada = actualizarSolicitudTraslado(solicitud);
        return DtoMapper.toSolicitudDto(guardada);
    }

    @Transactional(readOnly = true)
    public Map<Long, SolicitudTraslado> listarSolicitudes() {
        Map<Long, SolicitudTraslado> solicitudMap = new HashMap<>();
        for (SolicitudTraslado solicitud : solicitudTrasladoRepo.findAll()) {
            solicitudMap.put(solicitud.getNumero(), solicitud);
        }
        return solicitudMap;
    }

    @Transactional(readOnly = true)
    public Map<Long, SolicitudTrasladoDTO> listarSolicitudesDto() {
        Map<Long, SolicitudTrasladoDTO> solicitudMap = new HashMap<>();
        for (SolicitudTraslado solicitud : solicitudTrasladoRepo.findAll()) {
            solicitudMap.put(solicitud.getNumero(), DtoMapper.toSolicitudDto(solicitud));
        }
        return solicitudMap;
    }

    @Transactional(readOnly = true)
    public SolicitudTraslado obtenerSolicitudPorNumero(Long numero) {
        return solicitudTrasladoRepo.findById(numero)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con número: " + numero));
    }

    @Transactional(readOnly = true)
    public SolicitudTrasladoDTO obtenerSolicitudDtoPorNumero(Long numero) {
        return DtoMapper.toSolicitudDto(obtenerSolicitudPorNumero(numero));
    }

    @Transactional(readOnly = true)
    public Map<Long, SolicitudTraslado> obtenerSolicitudesPorEstado(EstadoSolicitud estado) {
        List<SolicitudTraslado> solicitudes = solicitudTrasladoRepo.findByEstado(estado);
        Map<Long, SolicitudTraslado> solicitudMap = new HashMap<>();
        for (SolicitudTraslado solicitud : solicitudes) {
            solicitudMap.put(solicitud.getNumero(), solicitud);
        }
        return solicitudMap;
    }

    @Transactional(readOnly = true)
    public Map<Long, SolicitudTrasladoDTO> obtenerSolicitudesDtoPorEstado(EstadoSolicitud estado) {
        List<SolicitudTraslado> solicitudes = solicitudTrasladoRepo.findByEstado(estado);
        Map<Long, SolicitudTrasladoDTO> solicitudMap = new HashMap<>();
        for (SolicitudTraslado solicitud : solicitudes) {
            solicitudMap.put(solicitud.getNumero(), DtoMapper.toSolicitudDto(solicitud));
        }
        return solicitudMap;
    }

    @Transactional(readOnly = true)
    public List<SolicitudTraslado> obtenerSolicitudesPorClienteId(Long clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El clienteId no puede ser null");
        }
        return solicitudTrasladoRepo.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<SolicitudTrasladoDTO> obtenerSolicitudesDtoPorClienteId(Long clienteId) {
        return solicitudTrasladoRepo.findByClienteId(clienteId).stream()
                .map(DtoMapper::toSolicitudDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void asignarCamionATramo(Long solicitudId, Long tramoId, String dominioCamion) {
        if (solicitudId == null || tramoId == null || dominioCamion == null || dominioCamion.trim().isEmpty()) {
            throw new IllegalArgumentException("Solicitud, Tramo y dominio del camión no pueden ser null o vacíos");
        }

        Tramos tramo = tramoService.obtenerTramoPorId(tramoId);
        if (tramo == null) {
            throw new IllegalArgumentException("Tramo no encontrado con ID: " + tramoId);
        }

        Camion camion = camionService.obtenerCamionPorDominio(dominioCamion);
        if (camion == null) {
            throw new IllegalArgumentException("Camión no encontrado con dominio: " + dominioCamion);
        }

        SolicitudTraslado solicitud = obtenerSolicitudPorNumero(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("Solicitud no encontrada con ID: " + solicitudId);
        }

        if (solicitud.getContenedor().getPeso() > camion.getCapKg()) {
            throw new IllegalArgumentException("El peso del contenedor excede la capacidad máxima del camión");
        }

        if (solicitud.getContenedor().getVolumen() > camion.getCapVolumen()) {
            throw new IllegalArgumentException("El volumen del contenedor excede la capacidad máxima del camión");
        }
        tramoService.asignarCamionATramo(tramo, camion);
        
    }

    @Transactional
    public SolicitudTraslado finalizarSolicitudTraslado(Long solicitudId) {
        SolicitudTraslado solicitud = obtenerSolicitudPorNumero(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("Solicitud no encontrada con ID: " + solicitudId);
        }
        // Finalizar todos los tramos asociados a la ruta (si no están finalizados)
        Ruta ruta = solicitud.getRuta();
        double tiempoRealHoras = 0.0;
        if (ruta != null && ruta.getTramos() != null) {
            java.util.List<Tramos> tramosActualizados = new java.util.ArrayList<>();

            for (Tramos tramo : ruta.getTramos()) {
                // Intentar finalizar tramo mediante el servicio (maneja validaciones)
                try {
                    Tramos actualizado = tramo;
                    if (tramo.getFechaFin() == null) {
                        actualizado = tramoService.finalizarTramo(tramo.getId());
                    } else {
                        // refrescamos para tener fechas y estado consistentes
                        actualizado = tramoService.obtenerTramoPorId(tramo.getId());
                    }
                    if (actualizado != null) {
                        tramosActualizados.add(actualizado);
                    }
                } catch (Exception ex) {
                    // no queremos abortar todo por un fallo en un tramo; registramos y seguimos
                    log.warn("No se pudo finalizar tramo id={}: {}", tramo.getId(), ex.getMessage());
                }
            }

            // Recalcular tiempo real sumando duración de tramos (cuando existan fechas)
            for (Tramos t : tramosActualizados) {
                java.sql.Date inicio = t.getFechaInicio();
                java.sql.Date fin = t.getFechaFin();
                if (inicio != null && fin != null) {
                    long millis = fin.getTime() - inicio.getTime();
                    if (millis > 0) {
                        tiempoRealHoras += millis / 1000.0 / 3600.0;
                    }
                }
            }
        }

        // Calcular costo final usando la tarifa asociada y la distancia de la ruta (si existe)
        Double costoFinal = null;
        if (solicitud.getTarifa() != null && ruta != null && ruta.getDistancia() != null && solicitud.getContenedor() != null) {
            Tarifa tarifa = solicitud.getTarifa();
            Double distanceMeters = ruta.getDistancia();
            Double volumen = solicitud.getContenedor().getVolumen();
            costoFinal = tarifa.getCostoPorKm() * distanceMeters + tarifa.getCostoPorM3() * volumen + tarifa.getCostoDeCombustible() * distanceMeters;
        }

        // Actualizar la solicitud
        if (costoFinal != null) solicitud.setCostoFinal(costoFinal);
        // siempre guardamos tiempoReal (aunque sea 0) para no dejarlo null
        solicitud.setTiempoReal(tiempoRealHoras);
        // Marcar contenedor como entregado
        if (solicitud.getContenedor() != null && solicitud.getContenedor().getId() != null) {
            try {
                contenedorService.actualizarEstadoContenedor(solicitud.getContenedor().getId(), EstadoContenedor.ENTREGADO);
                solicitud.setContenedor(contenedorService.obtenerContenedorPorId(solicitud.getContenedor().getId()));
            } catch (Exception ex) {
                log.warn("No se pudo actualizar estado del contenedor a ENTREGADO: {}", ex.getMessage());
            }
        }

        solicitud.setEstado(EstadoSolicitud.COMPLETADA);
        solicitudTrasladoRepo.save(solicitud);
        return solicitud;
    }

    @Transactional
    public SolicitudTrasladoDTO finalizarSolicitudTrasladoDto(Long solicitudId) {
        SolicitudTraslado solicitud = finalizarSolicitudTraslado(solicitudId);
        return DtoMapper.toSolicitudDto(solicitud);
    }

    // Validaciones para SolicitudTraslado
    private void validarSolicitud(SolicitudTraslado s) {
        if (s == null) {
            throw new IllegalArgumentException("Solicitud no puede ser null");
        }

        // ClienteId: obligatorio
        if (s.getClienteId() == null) {
            throw new IllegalArgumentException("El clienteId de la solicitud es obligatorio");
        }

        // Ruta: obligatorio y debe existir
        if (s.getRuta() == null || s.getRuta().getId() == null) {
            throw new IllegalArgumentException("La ruta de la solicitud es obligatoria");
        }
        rutaRepo.findById(Objects.requireNonNull(s.getRuta().getId())).orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        // Contenedor: obligatorio y debe existir (registro previo por ContenedorService)
        if (s.getContenedor() == null || s.getContenedor().getId() == null) {
            throw new IllegalArgumentException("El contenedor de la solicitud es obligatorio");
        }
        contenedorService.obtenerContenedorPorId(Objects.requireNonNull(s.getContenedor().getId()));

        // Tarifa: obligatorio y debe existir
        if (s.getTarifa() == null || s.getTarifa().getId() == null) {
            throw new IllegalArgumentException("La tarifa de la solicitud es obligatoria");
        }
        tarifaRepo.findById(Objects.requireNonNull(s.getTarifa().getId())).orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada"));

        // Costos/tiempos estimados: obligatorios y positivos
        if (s.getCostoEstimado() == null || s.getCostoEstimado() <= 0) {
            throw new IllegalArgumentException("El costo estimado debe ser un valor positivo");
        }
        if (s.getTiempoEstimado() == null || s.getTiempoEstimado() <= 0) {
            throw new IllegalArgumentException("El tiempo estimado debe ser un valor positivo");
        }

        // Si existe costoFinal o tiempoReal deben ser no negativos
        if (s.getCostoFinal() != null && s.getCostoFinal() < 0) {
            throw new IllegalArgumentException("El costo final no puede ser negativo");
        }
        if (s.getTiempoReal() != null && s.getTiempoReal() < 0) {
            throw new IllegalArgumentException("El tiempo real no puede ser negativo");
        }

        // Estado: obligatorio
        if (s.getEstado() == null) {
            throw new IllegalArgumentException("El estado de la solicitud es obligatorio");
        }
    }

    private Double extraerValorOsrm(Map<String, Object> osrmResponse, String key, String descripcion) {
        if (osrmResponse == null || osrmResponse.isEmpty()) {
            throw new IllegalStateException("No se obtuvo respuesta del servicio OSRM para " + descripcion);
        }
        if (osrmResponse.containsKey("error")) {
            throw new IllegalStateException("El servicio OSRM devolvió error al calcular " + descripcion + ": " + osrmResponse.get("error"));
        }
        Object value = osrmResponse.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalStateException("Respuesta de OSRM inválida. No se pudo leer " + descripcion);
        }
        return ((Number) value).doubleValue();
    }
    
    // --- Helpers for create flow ---
    @Value("${Cliente.service.url:http://localhost:8081}")
    private String clienteServiceUrl;

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    public Cliente buscarClientePorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del cliente no puede ser null");
        }
        try {
            String url = clienteServiceUrl + "/api/clientes/" + id;
            Cliente c = restTemplate.getForObject(url, Cliente.class);
            if (c == null || c.getId() == null) {
                return null;
            }
            return c;
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
            // no existe
            return null;
        } catch (org.springframework.web.client.ResourceAccessException rae) {
            throw new IllegalStateException("Servicio de Clientes no disponible: " + rae.getMessage(), rae);
        } catch (Exception ex) {
            throw new IllegalStateException("Error buscando cliente remoto: " + ex.getMessage(), ex);
        }
    }

    private Cliente registrarClienteSiNecesario(Long clienteId, String nombre, String apellido, String telefono, boolean activo, String email) {
        // Si se envió un id, verificamos existencia remota primero
        if (clienteId != null) {
            try {
                Cliente encontrado = obtenerClienteRemoto(clienteId);
                if (encontrado != null && encontrado.getId() != null) {
                    return encontrado;
                }
                // si devuelve body vacío o sin id, seguimos a creación
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound nf) {
                // no existe -> intentaremos crear con los datos provistos
            } catch (org.springframework.web.client.ResourceAccessException rae) {
                throw new IllegalStateException("Servicio de Clientes no disponible: " + rae.getMessage(), rae);
            } catch (Exception ex) {
                throw new IllegalStateException("Error verificando cliente remoto: " + ex.getMessage(), ex);
            }
        }

        // Construir payload para creación con campos provistos
        try {
            String url = clienteServiceUrl + "/api/clientes";
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            if (nombre != null) payload.put("nombre", nombre);
            if (apellido != null) payload.put("apellido", apellido);
            if (telefono != null) payload.put("telefono", telefono);
            payload.put("estado", activo);

            // Usar email provisto o generar uno por defecto
            String finalEmail = email;
            if (finalEmail == null || finalEmail.isBlank()) {
                String base = "user";
                if (nombre != null && apellido != null) {
                    base = nombre.toLowerCase().replaceAll("\\s+", "") + "." + apellido.toLowerCase().replaceAll("\\s+", "");
                } else if (nombre != null) {
                    base = nombre.toLowerCase().replaceAll("\\s+", "");
                }
                finalEmail = base + "@example.local";
            }
            payload.put("email", finalEmail);

            HttpHeaders headers = crearHeadersConToken();
            headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                Long newId = null;
                if (body != null && !body.isBlank()) {
                    try {
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
                        if (node.has("id") && !node.get("id").isNull()) newId = node.get("id").asLong();
                        else if (node.has("idCliente") && !node.get("idCliente").isNull()) newId = node.get("idCliente").asLong();
                        else if (node.has("clienteId") && !node.get("clienteId").isNull()) newId = node.get("clienteId").asLong();
                    } catch (Exception e) {
                        // ignore parse error, try headers next
                    }
                }

                // If body didn't contain id, try Location header
                if (newId == null) {
                    java.net.URI loc = response.getHeaders().getLocation();
                    if (loc != null) {
                        String path = loc.getPath();
                        String[] parts = path.split("/");
                        String last = parts[parts.length - 1];
                        try {
                            newId = Long.parseLong(last);
                        } catch (NumberFormatException nfe) {
                            // ignore
                        }
                    }
                }

                if (newId != null) {
                    Cliente created = new Cliente();
                    created.setId(newId);
                    created.setNombre(nombre);
                    created.setApellido(apellido);
                    created.setTelefono(telefono);
                    created.setActivo(activo);
                    created.setEmail((String) payload.get("email"));
                    return created;
                }
            }
            int status = (response != null && response.getStatusCode() != null) ? response.getStatusCode().value() : 0;
            throw new IllegalStateException("No se obtuvo id del cliente creado por el servicio remoto; status=" + status);
        } catch (org.springframework.web.client.ResourceAccessException rae) {
            throw new IllegalStateException("Servicio de Clientes no disponible al intentar crear cliente: " + rae.getMessage(), rae);
        } catch (org.springframework.web.client.HttpClientErrorException hce) {
            throw new IllegalStateException("Error de cliente al crear cliente remoto: " + hce.getStatusCode() + " - " + hce.getResponseBodyAsString(), hce);
            } catch (Exception ex) {
                throw new IllegalStateException("No se pudo registrar el cliente remoto: " + ex.getMessage(), ex);
            }
    }

    private HttpHeaders crearHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            headers.setBearerAuth(jwtAuth.getToken().getTokenValue());
        }
        return headers;
    }

    private Cliente obtenerClienteRemoto(Long clienteId) {
        String url = clienteServiceUrl + "/api/clientes/" + clienteId;
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(crearHeadersConToken()),
                String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return parseClienteDesdeJson(response.getBody());
        }
        return null;
    }

    private Cliente parseClienteDesdeJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root == null || root.isNull()) {
                return null;
            }
            if (root.isObject()) {
                // Caso mapa {"1": {...}}
                if (!root.has("id") && !root.has("idCliente") && !root.has("clienteId") && root.fields().hasNext()) {
                    JsonNode valueNode = root.fields().next().getValue();
                    if (valueNode.isObject()) {
                        return construirCliente(valueNode);
                    }
                }
                return construirCliente(root);
            }
        } catch (Exception ex) {
            log.warn("No se pudo parsear la respuesta del servicio de clientes: {}", ex.getMessage());
        }
        return null;
    }

    private Cliente construirCliente(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        Cliente cliente = new Cliente();
        if (node.hasNonNull("idCliente")) cliente.setId(node.get("idCliente").asLong());
        else if (node.hasNonNull("id")) cliente.setId(node.get("id").asLong());
        else if (node.hasNonNull("clienteId")) cliente.setId(node.get("clienteId").asLong());

        if (node.hasNonNull("nombre")) cliente.setNombre(node.get("nombre").asText());
        if (node.hasNonNull("apellido")) cliente.setApellido(node.get("apellido").asText());
        if (node.hasNonNull("telefono")) cliente.setTelefono(node.get("telefono").asText());
        if (node.hasNonNull("email")) cliente.setEmail(node.get("email").asText());
        if (node.hasNonNull("estado")) cliente.setActivo(node.get("estado").asBoolean());
        return cliente;
    }
}
