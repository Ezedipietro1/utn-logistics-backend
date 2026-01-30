package com.SolicitudTraslado.repo;

import com.SolicitudTraslado.domain.Transportista;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface TransportistaRepo extends JpaRepository<Transportista, String> {
    List<Transportista> findByNombreContainingIgnoreCase(String nombre);
}
