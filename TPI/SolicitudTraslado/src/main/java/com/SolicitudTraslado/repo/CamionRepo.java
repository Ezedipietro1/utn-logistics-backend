package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.Camion;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface CamionRepo extends JpaRepository<Camion, String> {
    List<Camion> findByEstadoTrue();

    List<Camion> findByCapVolumenGreaterThanAndCapKgGreaterThan(Double volumen, Double kg);
}
