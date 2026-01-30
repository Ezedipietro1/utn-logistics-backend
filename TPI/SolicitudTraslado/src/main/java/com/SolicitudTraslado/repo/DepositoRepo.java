package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.Deposito;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DepositoRepo extends JpaRepository<Deposito, Long> {
    List<Deposito> findByUbicacionId(Long ubicacionId);
    
    @Query("SELECT d FROM Deposito d WHERE d.ubicacion.ciudad.id = :ciudadId")
    List<Deposito> findByCiudadId(@Param("ciudadId") Long ciudadId);
}
