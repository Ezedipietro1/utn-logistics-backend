package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.Tramos;
import com.SolicitudTraslado.domain.enums.TipoTramo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface TramoRepo extends JpaRepository<Tramos, Long> {
    List<Tramos> findByCamionDominio(String dominio);
    List<Tramos> findByTipoTramo(TipoTramo tipoTramo);
}
