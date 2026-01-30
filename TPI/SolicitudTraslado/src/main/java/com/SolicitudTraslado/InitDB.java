package com.SolicitudTraslado;

import com.SolicitudTraslado.domain.*;
import com.SolicitudTraslado.domain.enums.EstadoContenedor;
import com.SolicitudTraslado.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InitDB implements CommandLineRunner {

    private final CiudadRepo ciudadRepo;
    private final UbicacionRepo ubicacionRepo;
    private final TransportistaRepo transportistaRepo;
    private final CamionRepo camionRepo;
    private final ContenedorRepo contenedorRepo;
    private final DepositoRepo depositoRepo;
    private final TarifaRepo tarifaRepo;

    public InitDB(CiudadRepo ciudadRepo,
                  UbicacionRepo ubicacionRepo,
                  TransportistaRepo transportistaRepo,
                  CamionRepo camionRepo,
                  ContenedorRepo contenedorRepo,
                  DepositoRepo depositoRepo,
                  TarifaRepo tarifaRepo) {
        this.ciudadRepo = ciudadRepo;
        this.ubicacionRepo = ubicacionRepo;
        this.transportistaRepo = transportistaRepo;
        this.camionRepo = camionRepo;
        this.contenedorRepo = contenedorRepo;
        this.depositoRepo = depositoRepo;
        this.tarifaRepo = tarifaRepo;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initCiudadesYUbicaciones();
        initTransportistasYCamiones();
        initContenedores();
        initTarifas();
        initDepositos();

        // Nota: No se crean Rutas/Tramos ni SolicitudTraslado aquí porque
        // las entidades Ruta/Tramos tienen FK no nulos a SolicitudTraslado.
        System.out.println("InitDB: inicialización completa (Solicitudes, Rutas y Tramos quedan vacíos)");
    }

    private void initCiudadesYUbicaciones() {
        long ccount = ciudadRepo.count();
        if (ccount == 0) {
            Ciudad c1 = Ciudad.builder().nombre("Buenos Aires").build();
            Ciudad c2 = Ciudad.builder().nombre("Rosario").build();
            Ciudad c3 = Ciudad.builder().nombre("Córdoba").build();
            ciudadRepo.saveAll(List.of(c1, c2, c3));

            Ubicacion u1 = Ubicacion.builder().direccion("Depósito Central BA").latitud(-34.6).longitud(-58.4).ciudad(c1).build();
            Ubicacion u2 = Ubicacion.builder().direccion("Puerto Rosario").latitud(-32.95).longitud(-60.66).ciudad(c2).build();
            Ubicacion u3 = Ubicacion.builder().direccion("Terminal Córdoba").latitud(-31.42).longitud(-64.18).ciudad(c3).build();
            ubicacionRepo.saveAll(List.of(u1, u2, u3));

            System.out.println("InitDB: creadas ciudades y ubicaciones por defecto");
        } else {
            System.out.println("InitDB: ya hay " + ccount + " ciudades");
        }
    }

    private void initTransportistasYCamiones() {
        long tcount = transportistaRepo.count();
        if (tcount == 0) {
            Transportista t1 = Transportista.builder().dni("20123456").nombre("Transporte Alvarez").apellido("Alvarez").telefono("+54-11-4000-0001").build();
            Transportista t2 = Transportista.builder().dni("20987654").nombre("LogiExpress").apellido("Gomez").telefono("+54-11-4000-0002").build();
            transportistaRepo.saveAll(List.of(t1, t2));

            Camion cam1 = Camion.builder().dominio("ABC1234").transportista(t1).capKg(10000.0).capVolumen(60.0).consumo(30.0).estado(true).build();
            Camion cam2 = Camion.builder().dominio("XYZ9876").transportista(t2).capKg(8000.0).capVolumen(45.0).consumo(25.0).estado(true).build();
            camionRepo.saveAll(List.of(cam1, cam2));

            System.out.println("InitDB: creados transportistas y camiones por defecto");
        } else {
            System.out.println("InitDB: ya hay " + tcount + " transportistas");
        }
    }

    private void initContenedores() {
        long count = contenedorRepo.count();
        if (count == 0) {
            Contenedor cont1 = Contenedor.builder().volumen(10.0).peso(1000.0).estadoContenedor(EstadoContenedor.EN_DEPOSITO).build();
            Contenedor cont2 = Contenedor.builder().volumen(5.0).peso(500.0).estadoContenedor(EstadoContenedor.EN_ESPERA_RETIRO).build();
            contenedorRepo.saveAll(List.of(cont1, cont2));
            System.out.println("InitDB: creados contenedores por defecto");
        } else {
            System.out.println("InitDB: ya hay " + count + " contenedores");
        }
    }

    private void initTarifas() {
        long count = tarifaRepo.count();
        if (count == 0) {
            Tarifa t = Tarifa.builder().costoPorKm(10.0).costoDeCombustible(2.5).costoPorM3(50.0).build();
            tarifaRepo.save(t);
            System.out.println("InitDB: creada tarifa por defecto");
        } else {
            // Si por alguna razón hubiera más de una, consolidamos manteniendo la primera
            List<Tarifa> todas = tarifaRepo.findAll();
            if (todas.size() > 1) {
                List<Tarifa> extras = todas.subList(1, todas.size());
                tarifaRepo.deleteAll(extras);
                System.out.println("InitDB: consolidada tarifa única (eliminadas " + extras.size() + " tarifas extra)");
            } else {
                System.out.println("InitDB: ya hay " + count + " tarifa");
            }
        }
    }

    private void initDepositos() {
        long dcount = depositoRepo.count();
        if (dcount == 0) {
            List<Ubicacion> ulist = ubicacionRepo.findAll();
            if (!ulist.isEmpty()) {
                Deposito d1 = Deposito.builder().ubicacion(ulist.get(0)).nombre("Depósito Centro").costoEstadia(1500.0).build();
                Deposito d2 = Deposito.builder().ubicacion(ulist.size() > 1 ? ulist.get(1) : ulist.get(0)).nombre("Depósito Puerto").costoEstadia(1200.0).build();
                depositoRepo.saveAll(List.of(d1, d2));
                System.out.println("InitDB: creados depósitos por defecto");
            }
        } else {
            System.out.println("InitDB: ya hay " + dcount + " depósitos");
        }
    }
}
 
