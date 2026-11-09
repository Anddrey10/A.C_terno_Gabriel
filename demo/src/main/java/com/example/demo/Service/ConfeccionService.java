package com.example.demo.Service;

import java.time.LocalDate;
import java.util.List;
import com.example.demo.Model.Confeccion;

/** Casos de uso de las órdenes de confección a medida (tickets). */
public interface ConfeccionService {

    List<Confeccion> listar();

    /** Registra la orden y emite su ticket (T001-0001). */
    Confeccion registrar(Confeccion nueva);

    /** Registra una prueba de la orden: guarda el comentario y, si cambió, la nueva fecha de entrega final. */
    Confeccion registrarPrueba(Long id, String comentario, LocalDate entregaFinal);
}
