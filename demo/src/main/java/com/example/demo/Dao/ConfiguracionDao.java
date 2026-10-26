package com.example.demo.Dao;

import java.util.Optional;
import com.example.demo.Model.Configuracion;

/** DAO de {@link Configuracion}. */
public interface ConfiguracionDao extends GenericDao<Configuracion> {

    /** @return valor de configuración con esa clave (por ejemplo penalidadDemora) */
    Optional<Configuracion> buscarPorClave(String clave);
}
