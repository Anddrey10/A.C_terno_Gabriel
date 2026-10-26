package com.example.demo.Seguridad;

/** Se lanza cuando un usuario supera el máximo de intentos de inicio de sesión fallidos. */
public class IntentosExcedidosException extends RuntimeException {

    public IntentosExcedidosException(String mensaje) {
        super(mensaje);
    }
}
