package com.example.demo.Service;

import java.util.List;
import com.example.demo.Model.Penalidad;

/** Casos de uso de las penalizaciones a clientes. */
public interface PenalidadService {

    List<Penalidad> listar();

    Penalidad crear(Penalidad nueva);

    Penalidad actualizar(Long id, Penalidad datos);

    void eliminar(Long id);
}
