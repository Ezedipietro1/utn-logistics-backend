package com.SolicitudTraslado.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.SolicitudTraslado.domain.Camion;
import com.SolicitudTraslado.domain.Ciudad;
import com.SolicitudTraslado.domain.Cliente;
import com.SolicitudTraslado.domain.Contenedor;
import com.SolicitudTraslado.domain.Deposito;
import com.SolicitudTraslado.domain.Ruta;
import com.SolicitudTraslado.domain.SolicitudTraslado;
import com.SolicitudTraslado.domain.Tarifa;
import com.SolicitudTraslado.domain.Tramos;
import com.SolicitudTraslado.domain.Transportista;
import com.SolicitudTraslado.domain.Ubicacion;

public final class DtoMapper {

    private DtoMapper() {
    }

    // ================= CAMION =================

    public static CamionDTO toCamionDto(Camion camion) {
        if (camion == null) {
            return null;
        }
        return CamionDTO.builder()
                .dominio(camion.getDominio())
                .capKg(camion.getCapKg())
                .capVolumen(camion.getCapVolumen())
                .consumo(camion.getConsumo())
                .estado(camion.getEstado())
                .transportista(toTransportistaDto(camion.getTransportista()))
                .build();
    }

    public static Camion toCamionEntity(CamionDTO dto) {
        if (dto == null) {
            return null;
        }
        Camion camion = new Camion();
        camion.setDominio(dto.getDominio());
        camion.setCapKg(dto.getCapKg());
        camion.setCapVolumen(dto.getCapVolumen());
        camion.setConsumo(dto.getConsumo());
        camion.setEstado(dto.getEstado());
        camion.setTransportista(toTransportistaEntity(dto.getTransportista()));
        return camion;
    }

    // ================ TRANSPORTISTA ================

    public static TransportistaDTO toTransportistaDto(Transportista transportista) {
        if (transportista == null) {
            return null;
        }
        return TransportistaDTO.builder()
                .dni(transportista.getDni())
                .nombre(transportista.getNombre())
                .apellido(transportista.getApellido())
                .telefono(transportista.getTelefono())
                .build();
    }

    public static Transportista toTransportistaEntity(TransportistaDTO dto) {
        if (dto == null) {
            return null;
        }
        Transportista transportista = new Transportista();
        transportista.setDni(dto.getDni());
        transportista.setNombre(dto.getNombre());
        transportista.setApellido(dto.getApellido());
        transportista.setTelefono(dto.getTelefono());
        return transportista;
    }

    // ================= CIUDAD =================

    public static CiudadDTO toCiudadDto(Ciudad ciudad) {
        if (ciudad == null) {
            return null;
        }
        return CiudadDTO.builder()
                .id(ciudad.getId())
                .nombre(ciudad.getNombre())
                .build();
    }

    public static Ciudad toCiudadEntity(CiudadDTO dto) {
        if (dto == null) {
            return null;
        }
        Ciudad ciudad = new Ciudad();
        ciudad.setId(dto.getId());
        ciudad.setNombre(dto.getNombre());
        return ciudad;
    }

    // ================= UBICACION =================

    public static UbicacionDTO toUbicacionDto(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return null;
        }
        return UbicacionDTO.builder()
                .id(ubicacion.getId())
                .direccion(ubicacion.getDireccion())
                .latitud(ubicacion.getLatitud())
                .longitud(ubicacion.getLongitud())
                .ciudad(toCiudadDto(ubicacion.getCiudad()))
                .build();
    }

    public static Ubicacion toUbicacionEntity(UbicacionDTO dto) {
        if (dto == null) {
            return null;
        }
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(dto.getId());
        ubicacion.setDireccion(dto.getDireccion());
        ubicacion.setLatitud(dto.getLatitud());
        ubicacion.setLongitud(dto.getLongitud());
        ubicacion.setCiudad(toCiudadEntity(dto.getCiudad()));
        return ubicacion;
    }

    // ================= DEPOSITO =================

    public static DepositoDTO toDepositoDto(Deposito deposito) {
        if (deposito == null) {
            return null;
        }
        return DepositoDTO.builder()
                .id(deposito.getId())
                .nombre(deposito.getNombre())
                .costoEstadia(deposito.getCostoEstadia())
                .ubicacion(toUbicacionDto(deposito.getUbicacion()))
                .build();
    }

    public static Deposito toDepositoEntity(DepositoDTO dto) {
        if (dto == null) {
            return null;
        }
        Deposito deposito = new Deposito();
        deposito.setId(dto.getId());
        deposito.setNombre(dto.getNombre());
        deposito.setCostoEstadia(dto.getCostoEstadia());
        deposito.setUbicacion(toUbicacionEntity(dto.getUbicacion()));
        return deposito;
    }

    // ================= CONTENEDOR =================

    public static ContenedorDTO toContenedorDto(Contenedor contenedor) {
        if (contenedor == null) {
            return null;
        }
        return ContenedorDTO.builder()
                .id(contenedor.getId())
                .volumen(contenedor.getVolumen())
                .peso(contenedor.getPeso())
                .estadoContenedor(contenedor.getEstadoContenedor())
                .build();
    }

    public static Contenedor toContenedorEntity(ContenedorDTO dto) {
        if (dto == null) {
            return null;
        }
        Contenedor contenedor = new Contenedor();
        contenedor.setId(dto.getId());
        contenedor.setVolumen(dto.getVolumen());
        contenedor.setPeso(dto.getPeso());
        contenedor.setEstadoContenedor(dto.getEstadoContenedor());
        return contenedor;
    }

    // ================= TARIFA =================

    public static TarifaDTO toTarifaDto(Tarifa tarifa) {
        if (tarifa == null) {
            return null;
        }
        return TarifaDTO.builder()
                .id(tarifa.getId())
                .costoPorKm(tarifa.getCostoPorKm())
                .costoDeCombustible(tarifa.getCostoDeCombustible())
                .costoPorM3(tarifa.getCostoPorM3())
                .build();
    }

    public static Tarifa toTarifaEntity(TarifaDTO dto) {
        if (dto == null) {
            return null;
        }
        Tarifa tarifa = new Tarifa();
        tarifa.setId(dto.getId());
        tarifa.setCostoPorKm(dto.getCostoPorKm());
        tarifa.setCostoDeCombustible(dto.getCostoDeCombustible());
        tarifa.setCostoPorM3(dto.getCostoPorM3());
        return tarifa;
    }

    // ================= TRAMOS =================

    public static TramoDTO toTramoDto(Tramos tramo) {
        if (tramo == null) {
            return null;
        }
        return TramoDTO.builder()
                .id(tramo.getId())
                .camion(toCamionDto(tramo.getCamion()))
                .origen(toDepositoDto(tramo.getOrigen()))
                .destino(toDepositoDto(tramo.getDestino()))
                .fechaInicio(tramo.getFechaInicio())
                .fechaFin(tramo.getFechaFin())
                .rutaId(tramo.getRuta() != null ? tramo.getRuta().getId() : null)
                .tipoTramo(tramo.getTipoTramo())
                .estadoTramo(tramo.getEstadoTramo())
                .build();
    }

    public static Tramos toTramoEntity(TramoDTO dto) {
        if (dto == null) {
            return null;
        }
        Tramos tramo = new Tramos();
        tramo.setId(dto.getId());
        tramo.setCamion(toCamionEntity(dto.getCamion()));
        tramo.setOrigen(toDepositoEntity(dto.getOrigen()));
        tramo.setDestino(toDepositoEntity(dto.getDestino()));
        tramo.setFechaInicio(dto.getFechaInicio());
        tramo.setFechaFin(dto.getFechaFin());
        tramo.setTipoTramo(dto.getTipoTramo());
        tramo.setEstadoTramo(dto.getEstadoTramo());
        if (dto.getRutaId() != null) {
            Ruta ruta = new Ruta();
            ruta.setId(dto.getRutaId());
            tramo.setRuta(ruta);
        }
        return tramo;
    }

    // ================= RUTA =================

    public static RutaDTO toRutaDto(Ruta ruta) {
        if (ruta == null) {
            return null;
        }
        List<TramoDTO> tramos = ruta.getTramos() == null
                ? Collections.emptyList()
                : ruta.getTramos().stream()
                        .map(DtoMapper::toTramoDto)
                        .collect(Collectors.toList());

        return RutaDTO.builder()
                .id(ruta.getId())
                .origen(toUbicacionDto(ruta.getOrigen()))
                .destino(toUbicacionDto(ruta.getDestino()))
                .distancia(ruta.getDistancia())
                .cantTramos(ruta.getCantTramos())
                .solicitudNumero(ruta.getSolicitud() != null ? ruta.getSolicitud().getNumero() : null)
                .asignada(ruta.getAsignada())
                .tiempoEstimado(ruta.getTiempoEstimado())
                .costoEstimado(ruta.getCostoEstimado())
                .tramos(tramos)
                .build();
    }

    public static Ruta toRutaEntity(RutaDTO dto) {
        if (dto == null) {
            return null;
        }
        Ruta ruta = new Ruta();
        ruta.setId(dto.getId());
        ruta.setOrigen(toUbicacionEntity(dto.getOrigen()));
        ruta.setDestino(toUbicacionEntity(dto.getDestino()));
        ruta.setDistancia(dto.getDistancia());
        ruta.setCantTramos(dto.getCantTramos());
        ruta.setAsignada(dto.getAsignada());
        ruta.setTiempoEstimado(dto.getTiempoEstimado());
        ruta.setCostoEstimado(dto.getCostoEstimado());
        if (dto.getSolicitudNumero() != null) {
            SolicitudTraslado solicitud = new SolicitudTraslado();
            solicitud.setNumero(dto.getSolicitudNumero());
            ruta.setSolicitud(solicitud);
        }
        if (dto.getTramos() != null && !dto.getTramos().isEmpty()) {
            ruta.setTramos(dto.getTramos().stream()
                    .map(DtoMapper::toTramoEntity)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
        }
        return ruta;
    }

    // ================= SOLICITUD TRASLADO =================

    public static SolicitudTrasladoDTO toSolicitudDto(SolicitudTraslado solicitud) {
        if (solicitud == null) {
            return null;
        }
        return SolicitudTrasladoDTO.builder()
                .numero(solicitud.getNumero())
                .clienteId(solicitud.getClienteId())
                .ubicacionOrigen(toUbicacionDto(solicitud.getUbicacionOrigen()))
                .ubicacionDestino(toUbicacionDto(solicitud.getUbicacionDestino()))
                .ruta(toRutaDto(solicitud.getRuta()))
                .contenedor(toContenedorDto(solicitud.getContenedor()))
                .tarifa(toTarifaDto(solicitud.getTarifa()))
                .costoEstimado(solicitud.getCostoEstimado())
                .tiempoEstimado(solicitud.getTiempoEstimado())
                .costoFinal(solicitud.getCostoFinal())
                .tiempoReal(solicitud.getTiempoReal())
                .estado(solicitud.getEstado())
                .build();
    }

    public static SolicitudTraslado toSolicitudEntity(SolicitudTrasladoDTO dto) {
        if (dto == null) {
            return null;
        }
        SolicitudTraslado solicitud = new SolicitudTraslado();
        solicitud.setNumero(dto.getNumero());
        solicitud.setClienteId(dto.getClienteId());
        solicitud.setUbicacionOrigen(toUbicacionEntity(dto.getUbicacionOrigen()));
        solicitud.setUbicacionDestino(toUbicacionEntity(dto.getUbicacionDestino()));
        solicitud.setRuta(toRutaEntity(dto.getRuta()));
        solicitud.setContenedor(toContenedorEntity(dto.getContenedor()));
        solicitud.setTarifa(toTarifaEntity(dto.getTarifa()));
        solicitud.setCostoEstimado(dto.getCostoEstimado());
        solicitud.setTiempoEstimado(dto.getTiempoEstimado());
        solicitud.setCostoFinal(dto.getCostoFinal());
        solicitud.setTiempoReal(dto.getTiempoReal());
        solicitud.setEstado(dto.getEstado());
        return solicitud;
    }

    // ================= CLIENTE =================

    public static ClienteDTO toClienteDto(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .telefono(cliente.getTelefono())
                .activo(cliente.isActivo())
                .email(cliente.getEmail())
                .build();
    }

    public static Cliente toClienteEntity(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setActivo(dto.getActivo() != null ? dto.getActivo() : false);
        cliente.setEmail(dto.getEmail());
        return cliente;
    }

    // ================= HELPERS =================

    public static List<CamionDTO> toCamionDtoList(List<Camion> camiones) {
        if (camiones == null) {
            return Collections.emptyList();
        }
        return camiones.stream().map(DtoMapper::toCamionDto).collect(Collectors.toList());
    }

    public static List<CiudadDTO> toCiudadDtoList(List<Ciudad> ciudades) {
        if (ciudades == null) {
            return Collections.emptyList();
        }
        return ciudades.stream().map(DtoMapper::toCiudadDto).collect(Collectors.toList());
    }

    public static List<DepositoDTO> toDepositoDtoList(List<Deposito> depositos) {
        if (depositos == null) {
            return Collections.emptyList();
        }
        return depositos.stream().map(DtoMapper::toDepositoDto).collect(Collectors.toList());
    }

    public static List<ContenedorDTO> toContenedorDtoList(List<Contenedor> contenedores) {
        if (contenedores == null) {
            return Collections.emptyList();
        }
        return contenedores.stream().map(DtoMapper::toContenedorDto).collect(Collectors.toList());
    }

    public static List<UbicacionDTO> toUbicacionDtoList(List<Ubicacion> ubicaciones) {
        if (ubicaciones == null) {
            return Collections.emptyList();
        }
        return ubicaciones.stream().map(DtoMapper::toUbicacionDto).collect(Collectors.toList());
    }

    public static List<RutaDTO> toRutaDtoList(List<Ruta> rutas) {
        if (rutas == null) {
            return Collections.emptyList();
        }
        return rutas.stream().map(DtoMapper::toRutaDto).collect(Collectors.toList());
    }

    public static List<SolicitudTrasladoDTO> toSolicitudDtoList(List<SolicitudTraslado> solicitudes) {
        if (solicitudes == null) {
            return Collections.emptyList();
        }
        return solicitudes.stream().map(DtoMapper::toSolicitudDto).collect(Collectors.toList());
    }
}
