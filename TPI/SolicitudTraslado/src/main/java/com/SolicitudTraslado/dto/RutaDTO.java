package com.SolicitudTraslado.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {
    private Long id;
    private UbicacionDTO origen;
    private UbicacionDTO destino;
    private Double distancia;
    private Integer cantTramos;
    private Long solicitudNumero;
    private Boolean asignada;
    private Double tiempoEstimado;
    private Double costoEstimado;
    private List<TramoDTO> tramos;
}
