package com.example.demo.Dao;

import java.util.Optional;
import com.example.demo.Model.Cliente;

/** DAO de {@link Cliente}. */
public interface ClienteDao extends GenericDao<Cliente> {

    /** @return cliente con ese DNI */
    Optional<Cliente> buscarPorDni(String dni);
}
