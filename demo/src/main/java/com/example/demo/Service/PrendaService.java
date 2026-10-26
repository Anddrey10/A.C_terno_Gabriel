package com.example.demo.Service;

import java.util.List;
import com.example.demo.Model.Prenda;

/** Casos de uso del inventario de prendas. */
public interface PrendaService {

    List<Prenda> listar();

    /** @return el siguiente código correlativo para un prefijo de 3 letras, por ejemplo TRN-005 */
    String siguienteCodigo(String prefijo);

    Prenda crear(Prenda nueva);

    Prenda actualizar(Long id, Prenda datos);

    Prenda cambiarEstado(Long id, String estado);

    Prenda suspender(Long id);

    Prenda reactivar(Long id);

    void eliminar(Long id);
}
