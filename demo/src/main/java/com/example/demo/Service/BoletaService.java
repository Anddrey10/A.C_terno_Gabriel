package com.example.demo.Service;

import java.util.List;
import com.example.demo.Model.Boleta;

/** Casos de uso de ventas, alquileres, reservas y entrega de confecciones (boletas). */
public interface BoletaService {

    List<Boleta> listar();

    /** Emite la boleta: calcula el monto, genera el número y actualiza el estado de la prenda u orden. */
    Boleta emitir(Boleta nueva);

    /** Cierra un alquiler vigente y envía la prenda a lavandería o restauración. */
    Boleta devolver(Long id, String estadoPrenda);

    /** Anula una boleta vigente (motivo obligatorio) y libera la prenda. */
    Boleta anular(Long id, String motivo);

    Boleta actualizar(Long id, Boleta datos);

    void eliminar(Long id);
}
