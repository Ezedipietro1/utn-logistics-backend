package com.SolicitudTraslado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoDTO {
    private Long id;
    private String nombre;
    private UbicacionDTO ubicacion;
    private Double costoEstadia;
}
