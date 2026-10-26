package com.example.demo.Dao;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.example.demo.Model.Cliente;
import com.example.demo.Repository.ClienteRepository;

/** Implementación JPA de {@link ClienteDao}. */
@Repository
public class ClienteDaoJpa extends JpaDao<Cliente> implements ClienteDao {

    private final ClienteRepository repositorio;

    public ClienteDaoJpa(ClienteRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Cliente> buscarPorDni(String dni) {
        return repositorio.findByDni(dni);
    }
}
