package com.example.demo.Controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Seguridad.UsuarioSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Capa Controlador (MVC): recibe el inicio de sesión y lo autentica con Spring Security. */
@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String MENSAJE_INVALIDO = "Usuario o contraseña incorrectos. Por favor, verifique sus datos.";

    private final AuthenticationManager autenticador;
    private final SecurityContextRepository repositorio;

    public AuthController(AuthenticationManager autenticador, SecurityContextRepository repositorio) {
        this.autenticador = autenticador;
        this.repositorio = repositorio;
    }

    /* Inicio de sesión contra la tabla empleado; la contraseña se compara contra su hash BCrypt.
       Si es correcto, Spring Security crea la sesión autenticada con el rol del empleado. */
    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos, HttpServletRequest peticion, HttpServletResponse respuesta) {
        Authentication autenticacion;
        try {
            autenticacion = autenticador.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(datos.get("usuario"), datos.get("contrasena")));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("mensaje", MENSAJE_INVALIDO));
        }

        /* Protección contra fijación de sesión: si ya había una sesión, se le cambia el identificador */
        if (peticion.getSession(false) != null) peticion.changeSessionId();

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
        repositorio.saveContext(contexto, peticion, respuesta);

        UsuarioSesion usuario = (UsuarioSesion) autenticacion.getPrincipal();
        return ResponseEntity.ok(Map.of("nombre", usuario.getNombre(), "rol", usuario.getRol()));
    }
}
