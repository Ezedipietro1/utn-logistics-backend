package com.SolicitudTraslado.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "tarifas")
public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double costoPorKm;

    @Column(nullable = false)
    private Double costoDeCombustible;

    @Column(nullable = false)
    private Double costoPorM3;
}