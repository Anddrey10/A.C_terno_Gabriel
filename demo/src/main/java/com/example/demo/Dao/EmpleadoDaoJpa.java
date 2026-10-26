package com.example.demo.Dao;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.example.demo.Model.Empleado;
import com.example.demo.Repository.EmpleadoRepository;

/** Implementación JPA de {@link EmpleadoDao}. */
@Repository
public class EmpleadoDaoJpa extends JpaDao<Empleado> implements EmpleadoDao {

    private final EmpleadoRepository repositorio;

    public EmpleadoDaoJpa(EmpleadoRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Empleado> buscarPorCodigo(String codigo) {
        return repositorio.findByCodigo(codigo);
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return repositorio.existsByCodigo(codigo);
    }

    @Override
    public Optional<Empleado> buscarPorUsuario(String usuario) {
        return repositorio.findByUsuarioIgnoreCase(usuario);
    }
}
