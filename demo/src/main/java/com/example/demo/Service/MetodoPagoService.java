package com.example.demo.Service;

import java.util.List;
import java.util.Map;
import com.example.demo.Model.MetodoPago;

/** Casos de uso de los métodos de pago (Yape y transferencia) que configura el administrador. */
public interface MetodoPagoService {

    List<MetodoPago> listar();

    /** Actualiza (o crea si aún no existe) el método de pago de ese tipo con los datos recibidos. */
    MetodoPago guardar(String tipo, Map<String, Object> datos);
}
