package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.Ciudad;
import com.SolicitudTraslado.domain.Ubicacion;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UbicacionRepo extends JpaRepository<Ubicacion, Long> {
    List<Ubicacion> findByCiudadId(Long ciudadId);
    List<Ubicacion> findByCiudad(Ciudad ciudad);
}
