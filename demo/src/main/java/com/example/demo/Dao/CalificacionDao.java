package com.example.demo.Dao;

import java.util.List;
import com.example.demo.Model.Calificacion;

/** DAO de {@link Calificacion}. */
public interface CalificacionDao extends GenericDao<Calificacion> {

    /** @return todas las calificaciones recibidas por un cliente */
    List<Calificacion> buscarPorCliente(Long idCliente);
}
