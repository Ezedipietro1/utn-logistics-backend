package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.Tarifa;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TarifaRepo extends JpaRepository<Tarifa, Long> {
}
