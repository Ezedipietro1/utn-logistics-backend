package com.SolicitudTraslado.services;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SolicitudTraslado.domain.Transportista;
import com.SolicitudTraslado.repo.TransportistaRepo;

@Service
public class TransportistaService {

    private final TransportistaRepo transportistaRepo;

    public TransportistaService(TransportistaRepo transportistaRepo) {
        this.transportistaRepo = transportistaRepo;
    }

    // servicio para crear un transportista
    @Transactional
    public Transportista crearTransportista(Transportista transportista) {
        // validamos los datos del transportista
        validarTransportista(transportista);

        // validamos si no existe otro transportista con el mismo DNI
        if (transportistaRepo.existsById(transportista.getDni())) {
            throw new IllegalArgumentException("Ya existe un transportista con el DNI: " + transportista.getDni());
        }

        // como paso todas las validaciones, creo el transportista

        return transportistaRepo.save(transportista);
    }

    // servicio para actualizar un transportista
    @Transactional
    public Transportista actualizarTransportista(Transportista transportista, String dni) {
        Transportista transportistaExistente = transportistaRepo.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con DNI: " + dni));

        // actualizo los campos que pueden ser modificados
        validarTransportista(transportista);

        return transportistaRepo.save(transportistaExistente);
    }

    @Transactional(readOnly = true)
    public List<Transportista> listarTransportistas() {
        return transportistaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Transportista> obtenerTransportistaPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        List<Transportista> transportistas = transportistaRepo.findByNombreContainingIgnoreCase(nombre);

        if (transportistas.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron transportistas con el nombre: " + nombre);
        }

        return transportistas;
    }

    @Transactional(readOnly = true)
    public Transportista obtenerTransportistaPorDni(String dni) {
        return transportistaRepo.findById(dni)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado con DNI: " + dni));
    }

    // ==================== MÉTODOS PRIVADOS DE VALIDACIÓN ====================

    private void validarTransportista(Transportista transportista) {
        if (transportista == null) {
            throw new IllegalArgumentException("El transportista no puede ser null");
        }

        // DNI: no nulo, no vacío y debe tener 8 dígitos
        if (transportista.getDni() == null || transportista.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del transportista es obligatorio");
        }

        if (!transportista.getDni().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos");
        }

        // Nombre: no nulo, no vacío y no debe contener números
        if (transportista.getNombre() == null || transportista.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del transportista es obligatorio");
        }

        if (Pattern.matches(".*\\d.*", transportista.getNombre())) {
            throw new IllegalArgumentException("El nombre no puede contener números");
        }

        // Apellido: no nulo, no vacío y no debe contener números
        if (transportista.getApellido() == null || transportista.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del transportista es obligatorio");
        }

        if (Pattern.matches(".*\\d.*", transportista.getApellido())) {
            throw new IllegalArgumentException("El apellido no puede contener números");
        }

        // Teléfono: no nulo, no vacío y debe contener exactamente 10 dígitos
        if (transportista.getTelefono() == null || transportista.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono del transportista es obligatorio");
        }

        if (!transportista.getTelefono().matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe contener exactamente 10 dígitos");
        }
    }

}
