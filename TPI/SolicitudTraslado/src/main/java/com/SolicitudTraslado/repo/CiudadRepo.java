package com.SolicitudTraslado.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SolicitudTraslado.domain.Ciudad;

@Repository
public interface CiudadRepo extends JpaRepository<Ciudad, Long> {
    Optional<Ciudad> findByNombreIgnoreCase(String nombre);
}
