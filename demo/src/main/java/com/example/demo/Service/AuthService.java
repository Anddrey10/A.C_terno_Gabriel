package com.example.demo.Service;

import java.util.Optional;
import com.example.demo.Model.Empleado;

/** Caso de uso: iniciar sesión. */
public interface AuthService {

    /**
     * Valida usuario y contraseña de un empleado activo.
     *
     * @return el empleado autenticado, o vacío si las credenciales no son válidas
     * @throws com.example.demo.Seguridad.IntentosExcedidosException si el usuario superó el máximo de intentos fallidos
     */
    Optional<Empleado> autenticar(String usuario, String contrasena);
}
