package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.SolicitudTraslado;
import com.SolicitudTraslado.domain.enums.EstadoSolicitud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudTrasladoRepo extends JpaRepository<SolicitudTraslado, Long> {
    List<SolicitudTraslado> findByClienteId(Long clienteId);
    List<SolicitudTraslado> findByEstado(EstadoSolicitud estado);
}
