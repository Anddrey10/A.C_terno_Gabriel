package com.example.demo.Dao;

import java.util.Optional;
import com.example.demo.Model.Empleado;

/** DAO de {@link Empleado}. */
public interface EmpleadoDao extends GenericDao<Empleado> {

    /** @return empleado con ese código (E001...) */
    Optional<Empleado> buscarPorCodigo(String codigo);

    /** @return si ya existe un empleado con ese código */
    boolean existePorCodigo(String codigo);

    /** @return empleado con ese usuario (sin distinguir mayúsculas) */
    Optional<Empleado> buscarPorUsuario(String usuario);
}
