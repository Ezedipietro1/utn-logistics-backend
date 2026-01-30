package com.Clientes;

import com.Clientes.domain.Cliente;
import com.Clientes.repo.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InitDB implements CommandLineRunner {

	private final ClienteRepository clienteRepo;

	public InitDB(ClienteRepository clienteRepo) {
		this.clienteRepo = clienteRepo;
	}

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		initClientes();
	}

	private void initClientes() {
		long count = clienteRepo.count();
		if (count == 0) {
			Cliente c1 = Cliente.builder()
					.nombre("Juan")
					.apellido("Pérez")
					.email("juan.perez@example.com")
					.telefono("+549112345678")
					.estado(true)
					.build();

			Cliente c2 = Cliente.builder()
					.nombre("María")
					.apellido("González")
					.email("maria.gonzalez@example.com")
					.telefono("+549118765432")
					.estado(true)
					.build();

			Cliente c3 = Cliente.builder()
					.nombre("Carlos")
					.apellido("Rodríguez")
					.email("carlos.rodriguez@example.com")
					.telefono("+549113333333")
					.estado(true)
					.build();

			clienteRepo.saveAll(List.of(c1, c2, c3));
			System.out.println("InitDB: clientes de prueba creados");
		} else {
			System.out.println("InitDB: ya hay " + count + " clientes");
		}
	}

}
