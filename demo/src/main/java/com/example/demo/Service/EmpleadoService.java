package com.example.demo.Service;

import java.util.List;
import com.example.demo.Model.Empleado;

/** Casos de uso de la gestión de empleados (módulo del administrador). */
public interface EmpleadoService {

    List<Empleado> listar();

    /** Crea un empleado: genera su código, lo deja Activo y guarda la contraseña cifrada. */
    Empleado crear(Empleado nuevo);

    Empleado actualizar(Long id, Empleado datos);

    Empleado suspender(Long id);

    Empleado reactivar(Long id);

    /** Genera una contraseña temporal, la guarda cifrada y la devuelve en texto plano una sola vez. */
    String restablecerContrasena(Long id);

    void eliminar(Long id);

    /** Cifra las contraseñas que todavía estén en texto plano (bases de datos antiguas). @return cuántas se cifraron */
    int cifrarContrasenasPendientes();
}
