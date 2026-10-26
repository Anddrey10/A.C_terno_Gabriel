package com.example.demo.Seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de seguridad de la aplicación.
 * <p>
 * Solo se usa el módulo de cifrado de Spring Security (BCrypt): las contraseñas de los
 * empleados ya no se guardan en texto plano. No se activa el filtro de login de Spring
 * Security para no alterar el flujo de pantallas existente.
 */
@Configuration
public class SeguridadConfig {

    /** BCrypt con factor de costo 10: genera una sal distinta por cada contraseña. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
