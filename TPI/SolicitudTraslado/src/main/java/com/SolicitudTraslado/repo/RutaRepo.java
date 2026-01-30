package com.SolicitudTraslado.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.SolicitudTraslado.domain.Ruta;

@Repository
public interface RutaRepo extends JpaRepository<Ruta, Long> {
    List<Ruta> findByAsignada(Boolean asignada);

    @Query("SELECT r FROM Ruta r WHERE r.solicitud.numero = :solicitudNumero")
    List<Ruta> findBySolicitudNumero(@Param("solicitudNumero") Long solicitudNumero);

    List<Ruta> findByOrigenIdAndDestinoId(Long origenId, Long destinoId);
}
