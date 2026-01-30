package com.Clientes.services;

import com.Clientes.domain.Cliente;
import com.Clientes.repo.ClienteRepository;

import java.util.List;
import java.util.regex.Pattern;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;



@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente guardarCliente(Cliente cliente) {
        validarCliente(cliente);
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }

    public Cliente actualizarCliente(Cliente clienteActualizado) {
        validarCliente(clienteActualizado);
        return clienteRepository.save(clienteActualizado);
    }

    private void validarCliente(Cliente c) {
        if (c == null) {
            throw new IllegalArgumentException("Cliente no puede ser null");
        }

        // Nombre: no nulo y no vacío
        if (Objects.isNull(c.getNombre()) || c.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        // Nombre: no debe contener dígitos
        if (Pattern.matches(".*\\d.*", c.getNombre())) {
            throw new IllegalArgumentException("El nombre no puede contener números");
        }

        // Apellido: opcional pero si viene, debe ser string no vacío y sin dígitos
        if (c.getApellido() != null) {
            if (c.getApellido().trim().isEmpty()) {
                throw new IllegalArgumentException("Apellido inválido");
            }
            if (Pattern.matches(".*\\d.*", c.getApellido())) {
                throw new IllegalArgumentException("El apellido no puede contener números");
            }
        }

        // Telefono: si viene, debe contener solo dígitos (opcionalmente con + al inicio)
        if (c.getTelefono() != null && !c.getTelefono().trim().isEmpty()) {
            String tel = c.getTelefono().trim();
            if (!Pattern.matches("\\+?\\d+", tel)) {
                throw new IllegalArgumentException("El teléfono debe contener solo números (opcionalmente con + al inicio)");
            }
        }

        // Email: si viene, comprobar formato básico (más permissivo)
        if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) {
            String email = c.getEmail().trim();
            // patrón permissivo: no espacios, un @ y al menos un punto en el dominio
            if (!Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", email)) {
                throw new IllegalArgumentException("Email en formato inválido: " + email);
            }
        }
    }
}
