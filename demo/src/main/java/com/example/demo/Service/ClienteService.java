package com.example.demo.Service;

import java.util.List;
import com.example.demo.Model.Cliente;

/** Casos de uso de la gestión de clientes y sus calificaciones. */
public interface ClienteService {

    List<Cliente> listar();

    Cliente crear(Cliente nuevo);

    Cliente actualizar(Long id, Cliente datos);

    /**
     * Registra una calificación (1 a 5 estrellas) y recalcula el promedio del cliente.
     *
     * @param estrellas  valor recibido de la pantalla (se valida que sea un entero entre 1 y 5)
     * @param comentario texto opcional del empleado
     */
    Cliente calificar(Long id, Object estrellas, Object comentario);

    void eliminar(Long id);
}
