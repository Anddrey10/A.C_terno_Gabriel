package com.example.demo.Dao;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.example.demo.Model.Configuracion;
import com.example.demo.Repository.ConfiguracionRepository;

/** Implementación JPA de {@link ConfiguracionDao}. */
@Repository
public class ConfiguracionDaoJpa extends JpaDao<Configuracion> implements ConfiguracionDao {

    private final ConfiguracionRepository repositorio;

    public ConfiguracionDaoJpa(ConfiguracionRepository repositorio) {
        super(repositorio);
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Configuracion> buscarPorClave(String clave) {
        return repositorio.findByClave(clave);
    }
}
