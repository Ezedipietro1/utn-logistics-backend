package com.SolicitudTraslado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {
    private Long id;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private CiudadDTO ciudad;
}
