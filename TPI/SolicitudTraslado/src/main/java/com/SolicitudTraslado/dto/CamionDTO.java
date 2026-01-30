package com.SolicitudTraslado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionDTO {
    private String dominio;
    private TransportistaDTO transportista;
    private Double capVolumen;
    private Double capKg;
    private Double consumo;
    private Boolean estado;
}
