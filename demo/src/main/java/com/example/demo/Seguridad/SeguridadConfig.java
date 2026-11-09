package com.example.demo.Seguridad;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuración de Spring Security.
 * <ul>
 *   <li>{@link PasswordEncoder}: las contraseñas se guardan cifradas con BCrypt, nunca en texto plano.</li>
 *   <li>{@link SecurityFilterChain}: reglas de acceso por rol. El administrador entra a /admin/**,
 *       el empleado a /empleado/**, y el login y los archivos estáticos son públicos.</li>
 *   <li>El inicio de sesión sigue siendo la misma pantalla (POST /api/login); ahora crea una sesión
 *       autenticada en el servidor y el cierre de sesión es POST /logout.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
/* Permite @PreAuthorize("hasAuthority('ADMIN')") en controladores y servicios */
@EnableMethodSecurity
public class SeguridadConfig {

    /** BCrypt con factor de costo 10: genera una sal distinta por cada contraseña. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /** Autentica contra la tabla empleado mediante {@link ProveedorAutenticacion}. */
    @Bean
    public AuthenticationManager authenticationManager(ProveedorAutenticacion proveedor) {
        return new ProviderManager(proveedor);
    }

    /** Guarda el contexto de seguridad en la sesión HTTP (lo usan el login y la cadena de filtros). */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repositorio) throws Exception {
        CsrfTokenRequestAttributeHandler manejadorCsrf = new CsrfTokenRequestAttributeHandler();

        http
            .authorizeHttpRequests(auth -> auth
                /* Público: pantalla de login, página de acceso denegado, estáticos y el inicio de sesión */
                .requestMatchers("/", "/login", "/acceso-denegado", "/error", "/favicon.ico",
                    "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                /* Rutas de ejemplo de la sesión 14: /public/** abierto; /privado/home exige sesión y /privado/admin la autoridad ADMIN */
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/privado/admin").hasAuthority("ADMIN")
                .requestMatchers("/privado/**").authenticated()

                /* Pantallas por rol */
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/empleado/**").hasRole("EMPLEADO")

                /* API: solo el administrador gestiona empleados, reportes, configuración, métodos de pago,
                   y crea, edita o elimina prendas, clientes, penalidades y boletas ya emitidas */
                .requestMatchers("/api/empleados/**", "/api/reportes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/configuracion/**", "/api/metodos-pago/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/prendas/nuevo").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/prendas/actualizar/**", "/api/prendas/suspender/**",
                    "/api/prendas/reactivar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/clientes/actualizar/**", "/api/penalidades/actualizar/**",
                    "/api/boletas/actualizar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/prendas/**", "/api/clientes/**", "/api/penalidades/**",
                    "/api/boletas/**").hasRole("ADMIN")

                /* El resto de la API (ventas, alquileres, devoluciones, confecciones...) exige haber iniciado sesión */
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated())

            /* CSRF: el token viaja en la cookie XSRF-TOKEN y el JavaScript lo reenvía en X-XSRF-TOKEN */
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(manejadorCsrf))
            .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class)

            .securityContext(contexto -> contexto.securityContextRepository(repositorio))

            /* Cierre de sesión: invalida la sesión del servidor y vuelve al login */
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"))

            /* Mismas cabeceras que ya enviaba CabecerasSeguridadFilter */
            .headers(cabeceras -> cabeceras.frameOptions(marcos -> marcos.sameOrigin()))

            .exceptionHandling(errores -> errores
                .authenticationEntryPoint(sinSesion())
                .accessDeniedHandler(sinPermiso()));

        return http.build();
    }

    /** Sin sesión: la API responde 401 (el JavaScript vuelve al login); una pantalla redirige a /login. */
    private AuthenticationEntryPoint sinSesion() {
        return (peticion, respuesta, error) -> {
            if (esApi(peticion)) responderJson(respuesta, 401, "Tu sesión venció. Inicia sesión de nuevo.");
            else respuesta.sendRedirect(peticion.getContextPath() + "/login");
        };
    }

    /** Sin permiso: la API responde 403; una pantalla redirige a /acceso-denegado. */
    private AccessDeniedHandler sinPermiso() {
        return (peticion, respuesta, error) -> {
            if (esApi(peticion)) {
                String mensaje = error instanceof CsrfException
                    ? "La sesión de seguridad venció. Recarga la página e inicia sesión de nuevo."
                    : "No tienes permiso para realizar esta acción.";
                responderJson(respuesta, 403, mensaje);
            } else {
                respuesta.sendRedirect(peticion.getContextPath() + "/acceso-denegado");
            }
        };
    }

    private static boolean esApi(HttpServletRequest peticion) {
        return peticion.getRequestURI().startsWith(peticion.getContextPath() + "/api/");
    }

    private static void responderJson(HttpServletResponse respuesta, int estado, String mensaje) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
