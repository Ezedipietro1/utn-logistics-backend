package com.SolicitudTraslado.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cliente {
    private Long id;
    private String nombre;
    private String apellido;
    private String telefono;
    private boolean activo;
    private String email;
}