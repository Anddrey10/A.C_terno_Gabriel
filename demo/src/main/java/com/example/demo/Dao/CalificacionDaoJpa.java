package com.example.demo.Dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.example.demo.Model.Calificacion;
import com.example.demo.Repository.CalificacionRepository;

/** Implementación JPA de {@link CalificacionDao}. */
@Repository
public class CalificacionDaoJpa extends JpaDao<Calificacion> implements CalificacionDao {

    private final CalificacionRepository repositorio;

    public CalificacionDaoJpa(CalificacionRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }

    @Override
    public List<Calificacion> buscarPorCliente(Long idCliente) {
        return repositorio.findByClienteId(idCliente);
    }
}
