package com.example.demo.Controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Service.AuthService;

/** Capa Controlador (MVC): recibe la petición de inicio de sesión y delega en {@link AuthService}. */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService servicio;

    public AuthController(AuthService servicio) {
        this.servicio = servicio;
    }

    /* Inicio de sesión contra la tabla empleado; la contraseña se compara contra su hash BCrypt */
    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        return servicio.autenticar(datos.get("usuario"), datos.get("contrasena"))
            .<ResponseEntity<?>>map(e -> ResponseEntity.ok(Map.of("nombre", e.getNombre(), "rol", e.getRol())))
            .orElse(ResponseEntity.status(401).body(Map.of("mensaje", "Usuario o contraseña incorrectos. Por favor, verifique sus datos.")));
    }
}
