package com.example.demo.Seguridad;

import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Obliga a que el token CSRF se cargue en cada petición, así la cookie XSRF-TOKEN siempre está disponible
 * para que el JavaScript de las pantallas la envíe en la cabecera X-XSRF-TOKEN.
 * No se anota como @Component: solo se registra dentro de la cadena de Spring Security.
 */
public class CookieCsrfFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) peticion.getAttribute(CsrfToken.class.getName());
        if (token != null) token.getToken();
        cadena.doFilter(peticion, respuesta);
    }
}
