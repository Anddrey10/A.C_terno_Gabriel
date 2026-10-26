package com.example.demo.Dao;


import org.springframework.stereotype.Repository;
import com.example.demo.Model.Boleta;
import com.example.demo.Repository.BoletaRepository;

/** Implementación JPA de {@link BoletaDao}. */
@Repository
public class BoletaDaoJpa extends JpaDao<Boleta> implements BoletaDao {

    private final BoletaRepository repositorio;

    public BoletaDaoJpa(BoletaRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }
}
