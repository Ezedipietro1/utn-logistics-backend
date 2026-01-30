package com.SolicitudTraslado.domain;

import jakarta.persistence.*;
import lombok.*;
import com.SolicitudTraslado.domain.enums.EstadoContenedor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "contenedores")
public class Contenedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double volumen;

    @Column(nullable = false)
    private Double peso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_contenedor", nullable = false)
    private EstadoContenedor estadoContenedor;
}