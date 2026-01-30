package com.SolicitudTraslado.dto;

import java.sql.Date;

import com.SolicitudTraslado.domain.enums.EstadoTramo;
import com.SolicitudTraslado.domain.enums.TipoTramo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoDTO {
    private Long id;
    private CamionDTO camion;
    private DepositoDTO origen;
    private DepositoDTO destino;
    private Date fechaInicio;
    private Date fechaFin;
    private Long rutaId;
    private TipoTramo tipoTramo;
    private EstadoTramo estadoTramo;
}
