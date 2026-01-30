package com.SolicitudTraslado.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SolicitudTraslado.domain.Contenedor;
import com.SolicitudTraslado.domain.enums.EstadoContenedor;

@Repository
public interface ContenedorRepo extends JpaRepository<Contenedor, Long> {
    List<Contenedor> findByEstadoContenedor(EstadoContenedor estadoContenedor);
    List<Contenedor> findByEstadoContenedorNot(EstadoContenedor estadoContenedor);
}
