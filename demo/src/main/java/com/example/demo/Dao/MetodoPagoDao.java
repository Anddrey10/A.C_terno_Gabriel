package com.example.demo.Dao;

import java.util.Optional;
import com.example.demo.Model.MetodoPago;

/** DAO de {@link MetodoPago}. */
public interface MetodoPagoDao extends GenericDao<MetodoPago> {

    /** @return el método de pago de ese tipo (YAPE o TRANSFERENCIA) */
    Optional<MetodoPago> buscarPorTipo(String tipo);
}
