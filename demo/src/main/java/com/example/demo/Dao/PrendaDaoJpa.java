package com.example.demo.Dao;


import org.springframework.stereotype.Repository;
import com.example.demo.Model.Prenda;
import com.example.demo.Repository.PrendaRepository;

/** Implementación JPA de {@link PrendaDao}. */
@Repository
public class PrendaDaoJpa extends JpaDao<Prenda> implements PrendaDao {

    private final PrendaRepository repositorio;

    public PrendaDaoJpa(PrendaRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }
}
