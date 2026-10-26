package com.example.demo.Seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Responsabilidad única: cifrar, comparar y generar contraseñas.
 * Ningún otro componente conoce el algoritmo de cifrado usado.
 */
@Component
public class GestorContrasenas {

    private static final String ALFABETO = "abcdefghijkmnpqrstuvwxyz23456789";
    private static final int LARGO_TEMPORAL = 8;

    private final PasswordEncoder codificador;
    private final SecureRandom azar = new SecureRandom();

    public GestorContrasenas(PasswordEncoder codificador) {
        this.codificador = codificador;
    }

    /** @return el hash BCrypt de la contraseña en texto plano */
    public String cifrar(String plana) {
        return codificador.encode(plana);
    }

    /** @return {@code true} si el valor ya es un hash BCrypt (60 caracteres que empiezan con $2a$, $2b$ o $2y$) */
    public boolean estaCifrada(String valor) {
        return valor != null && valor.length() == 60
            && (valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$"));
    }

    /**
     * Compara la contraseña escrita con la guardada. Acepta contraseñas antiguas en texto plano
     * (comparación en tiempo constante) para poder migrarlas al primer ingreso.
     */
    public boolean coincide(String plana, String guardada) {
        if (plana == null || guardada == null) return false;
        if (estaCifrada(guardada)) return codificador.matches(plana, guardada);
        return MessageDigest.isEqual(plana.getBytes(StandardCharsets.UTF_8), guardada.getBytes(StandardCharsets.UTF_8));
    }

    /** @return una contraseña temporal aleatoria de 8 caracteres (sin símbolos que se confundan) */
    public String generarTemporal() {
        StringBuilder temporal = new StringBuilder(LARGO_TEMPORAL);
        for (int i = 0; i < LARGO_TEMPORAL; i++) temporal.append(ALFABETO.charAt(azar.nextInt(ALFABETO.length())));
        return temporal.toString();
    }
}
