package com.SolicitudTraslado.domain;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "camiones")
public class Camion {
    @Id
    @Column(nullable = false, unique = true, length = 7)
    private String dominio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_dni", nullable = false)
    private Transportista transportista;

    @Column(name = "cap_volumen", nullable = false)
    private Double capVolumen;

    @Column(name = "cap_kg", nullable = false)
    private Double capKg;

    @Column(name = "consumo") // litros/100km por ej.
    private Double consumo;

    @Column(nullable = false)
    private Boolean estado;
}
