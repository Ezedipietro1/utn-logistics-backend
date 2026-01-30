package com.SolicitudTraslado.domain;

import jakarta.persistence.*;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "transportistas")
public class Transportista {
    @Id
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false, length = 50)
    private String nombre;  

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 10)
    private String telefono;
}
