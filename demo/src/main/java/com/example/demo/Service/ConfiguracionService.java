package com.example.demo.Service;

import java.util.List;
import com.example.demo.Model.Configuracion;

/** Casos de uso de los valores configurables (montos de penalidad). */
public interface ConfiguracionService {

    List<Configuracion> listar();

    /** Cambia el valor de una clave; el monto debe ser un número mayor a 0. */
    Configuracion cambiar(String clave, Object valor, Object modificadoPor);
}
