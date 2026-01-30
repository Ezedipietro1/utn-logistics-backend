package com.SolicitudTraslado.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "ubicaciones")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ubicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String direccion;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id", nullable = false)
    private Ciudad ciudad;
}
