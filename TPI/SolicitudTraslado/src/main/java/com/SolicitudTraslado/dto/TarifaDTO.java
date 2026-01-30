package com.SolicitudTraslado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaDTO {
    private Long id;
    private Double costoPorKm;
    private Double costoDeCombustible;
    private Double costoPorM3;
}
