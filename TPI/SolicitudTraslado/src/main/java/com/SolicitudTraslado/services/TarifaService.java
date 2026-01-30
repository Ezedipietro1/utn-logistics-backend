package com.SolicitudTraslado.services;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SolicitudTraslado.domain.Tarifa;
import com.SolicitudTraslado.dto.DtoMapper;
import com.SolicitudTraslado.dto.TarifaDTO;
import com.SolicitudTraslado.repo.TarifaRepo;

@Service
public class TarifaService {
    private final TarifaRepo tarifaRepo;
    
    public TarifaService(TarifaRepo tarifaRepo) {
        this.tarifaRepo = tarifaRepo;
    }

    @Transactional(readOnly = true)
    public Tarifa obtenerTarifa() {
        List<Tarifa> todas = tarifaRepo.findAll();
        if (todas.isEmpty()) {
            Tarifa defecto = Tarifa.builder().costoPorKm(10.0).costoDeCombustible(2.5).costoPorM3(50.0).build();
            return tarifaRepo.save(defecto);
        }
        if (todas.size() > 1) {
            tarifaRepo.deleteAll(todas.subList(1, todas.size()));
        }
        return tarifaRepo.findAll().get(0);
    }

    @Transactional(readOnly = true)
    public Tarifa obtenerTarifaPorId(Long id) {
        return tarifaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public TarifaDTO obtenerTarifaDto() {
        return DtoMapper.toTarifaDto(obtenerTarifa());
    }

    @Transactional(readOnly = true)
    public TarifaDTO obtenerTarifaDtoPorId(Long id) {
        return DtoMapper.toTarifaDto(obtenerTarifaPorId(id));
    }

    @Transactional(readOnly = true)
    public HashMap<Long, TarifaDTO> listarTarifasDto() {
        HashMap<Long, TarifaDTO> mapa = new HashMap<>();
        for (Tarifa tarifa : tarifaRepo.findAll()) {
            mapa.put(tarifa.getId(), DtoMapper.toTarifaDto(tarifa));
        }
        return mapa;
    }

    @Transactional
    public TarifaDTO crearTarifa(TarifaDTO tarifaDto) {
        Tarifa tarifa = DtoMapper.toTarifaEntity(tarifaDto);
        Tarifa guardada = crearTarifa(tarifa);
        return DtoMapper.toTarifaDto(guardada);
    }

    @Transactional
    public Tarifa crearTarifa(Tarifa tarifa) {
        validarTarifa(tarifa);
        // Si ya existe una tarifa, convertir creación en actualización de la existente
        List<Tarifa> todas = tarifaRepo.findAll();
        if (!todas.isEmpty()) {
            Tarifa existente = todas.get(0);
            existente.setCostoPorKm(tarifa.getCostoPorKm());
            existente.setCostoDeCombustible(tarifa.getCostoDeCombustible());
            existente.setCostoPorM3(tarifa.getCostoPorM3());
            if (todas.size() > 1) tarifaRepo.deleteAll(todas.subList(1, todas.size()));
            return tarifaRepo.save(existente);
        }
        return tarifaRepo.save(tarifa);
    }

    @Transactional
    public Tarifa actualizarTarifa(Tarifa tarifaActualizada) {
        validarTarifa(tarifaActualizada);
        List<Tarifa> todas = tarifaRepo.findAll();
        if (todas.isEmpty()) {
            return tarifaRepo.save(tarifaActualizada);
        }
        Tarifa existente = todas.get(0);
        existente.setCostoPorKm(tarifaActualizada.getCostoPorKm());
        existente.setCostoDeCombustible(tarifaActualizada.getCostoDeCombustible());
        existente.setCostoPorM3(tarifaActualizada.getCostoPorM3());
        if (todas.size() > 1) tarifaRepo.deleteAll(todas.subList(1, todas.size()));
        return tarifaRepo.save(existente);
    }

    @Transactional
    public TarifaDTO actualizarTarifa(TarifaDTO tarifaActualizada) {
        Tarifa tarifa = DtoMapper.toTarifaEntity(tarifaActualizada);
        Tarifa guardada = actualizarTarifa(tarifa);
        return DtoMapper.toTarifaDto(guardada);
    }

    @Transactional(readOnly = true)
    public HashMap<Long, Tarifa> listarTarifas() {
        HashMap<Long, Tarifa> tarifaMap = new HashMap<>();
        for (Tarifa tarifa : tarifaRepo.findAll()) {
            tarifaMap.put(tarifa.getId(), tarifa);
        }
        return tarifaMap;
    }

    // Validación para Tarifa
    private void validarTarifa(Tarifa t) {
        if (t == null) {
            throw new IllegalArgumentException("Tarifa no puede ser null");
        }

        if (t.getCostoPorKm() == null || t.getCostoPorKm() < 0) {
            throw new IllegalArgumentException("El costo por km debe ser un valor >= 0");
        }

        if (t.getCostoDeCombustible() == null || t.getCostoDeCombustible() < 0) {
            throw new IllegalArgumentException("El costo de combustible debe ser 0 o un valor positivo");
        }

        if (t.getCostoPorM3() == null || t.getCostoPorM3() < 0) {
            throw new IllegalArgumentException("El costo por m3 debe ser 0 o un valor positivo");
        }
    }
}
