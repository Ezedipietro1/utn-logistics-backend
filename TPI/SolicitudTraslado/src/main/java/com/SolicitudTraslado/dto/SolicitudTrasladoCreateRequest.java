package com.SolicitudTraslado.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudTrasladoCreateRequest {
    private Long clienteId;
    private String nombre;
    private String apellido;
    private String telefono;
    private boolean activo;
    private String email;
    private Double volumen;
    private Double peso;
    private Long ubicacionOrigenId;
    private Long ubicacionDestinoId;
}
