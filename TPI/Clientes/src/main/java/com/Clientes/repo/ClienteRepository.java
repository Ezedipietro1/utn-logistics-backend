package com.Clientes.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Clientes.domain.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}