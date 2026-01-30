package com.SolicitudTraslado.domain;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.util.LinkedHashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "rutas")
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_id", nullable = false)
    private Ubicacion origen;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id", nullable = false)
    private Ubicacion destino;

    @Column(nullable = false)
    private Double distancia;

    @Column(name = "cant_tramos", nullable = false)
    private Integer cantTramos;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudTraslado solicitud;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Tramos> tramos = new LinkedHashSet<>();

    @Column(nullable = false)
    private Boolean asignada;

    @Transient
    private Double tiempoEstimado;

    @Transient
    private Double costoEstimado;
}
