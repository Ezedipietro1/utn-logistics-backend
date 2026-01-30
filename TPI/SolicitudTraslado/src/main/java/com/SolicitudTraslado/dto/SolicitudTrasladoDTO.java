package com.SolicitudTraslado.dto;

import com.SolicitudTraslado.domain.enums.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudTrasladoDTO {
    private Long numero;
    private Long clienteId;
    private UbicacionDTO ubicacionOrigen;
    private UbicacionDTO ubicacionDestino;
    private RutaDTO ruta;
    private ContenedorDTO contenedor;
    private TarifaDTO tarifa;
    private Double costoEstimado;
    private Double tiempoEstimado;
    private Double costoFinal;
    private Double tiempoReal;
    private EstadoSolicitud estado;
}
