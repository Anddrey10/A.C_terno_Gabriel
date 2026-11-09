package com.example.demo.Dao;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.example.demo.Model.MetodoPago;
import com.example.demo.Repository.MetodoPagoRepository;

/** Implementación JPA de {@link MetodoPagoDao}. */
@Repository
public class MetodoPagoDaoJpa extends JpaDao<MetodoPago> implements MetodoPagoDao {

    private final MetodoPagoRepository repositorio;

    public MetodoPagoDaoJpa(MetodoPagoRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }

    @Override
    public Optional<MetodoPago> buscarPorTipo(String tipo) {
        return repositorio.findByTipoIgnoreCase(tipo);
    }
}
