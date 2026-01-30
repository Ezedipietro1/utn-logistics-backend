package com.SolicitudTraslado.dto;

import com.SolicitudTraslado.domain.enums.EstadoContenedor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorDTO {
    private Long id;
    private Double volumen;
    private Double peso;
    private EstadoContenedor estadoContenedor;
}
