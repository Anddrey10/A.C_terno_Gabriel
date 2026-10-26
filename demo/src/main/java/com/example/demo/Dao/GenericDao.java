package com.example.demo.Dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de acceso a datos (patrón DAO).
 * <p>
 * Los servicios dependen de esta abstracción y no de JPA/Spring Data (principio de
 * inversión de dependencias, la "D" de SOLID), lo que permite sustituir la
 * persistencia o simularla en las pruebas unitarias.
 *
 * @param <T> tipo de entidad que gestiona el DAO
 */
public interface GenericDao<T> {

    /** @return todos los registros de la entidad */
    List<T> listar();

    /** @return el registro con ese id, o vacío si no existe */
    Optional<T> buscarPorId(Long id);

    /** Inserta o actualiza el registro y devuelve la versión guardada. */
    T guardar(T entidad);

    /** Elimina el registro con ese id. */
    void eliminarPorId(Long id);

    /** @return cantidad de registros almacenados */
    long contar();
}
