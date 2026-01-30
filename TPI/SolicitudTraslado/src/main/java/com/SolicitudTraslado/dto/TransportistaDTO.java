package com.SolicitudTraslado.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportistaDTO {
    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
}
