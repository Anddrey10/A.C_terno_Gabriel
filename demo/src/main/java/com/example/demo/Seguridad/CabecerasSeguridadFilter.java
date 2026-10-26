package com.example.demo.Seguridad;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Agrega cabeceras HTTP de seguridad a todas las respuestas y evita guardar en caché las respuestas de la API. */
@Component
public class CabecerasSeguridadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        respuesta.setHeader("X-Content-Type-Options", "nosniff");
        respuesta.setHeader("X-Frame-Options", "SAMEORIGIN");
        respuesta.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        if (peticion.getRequestURI().startsWith("/api/")) {
            respuesta.setHeader("Cache-Control", "no-store");
        }
        cadena.doFilter(peticion, respuesta);
    }
}
