package com.example.demo.Seguridad;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import com.example.demo.Model.Empleado;

/**
 * Usuario que Spring Security guarda en la sesión tras iniciar sesión.
 * Los roles del sistema se convierten así: "Administrador" → ROLE_ADMIN y ADMIN; "Empleado" → ROLE_EMPLEADO y EMPLEADO.
 */
public class UsuarioSesion extends User {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String rol;

    public UsuarioSesion(Empleado empleado) {
        /* La contraseña no se conserva en la sesión */
        super(empleado.getUsuario(), "", autoridadesDe(empleado.getRol()));
        this.nombre = empleado.getNombre();
        this.rol = empleado.getRol();
    }

    static List<GrantedAuthority> autoridadesDe(String rol) {
        String autoridad = "Administrador".equals(rol) ? "ADMIN" : "EMPLEADO";
        /* ROLE_X sirve para hasRole("X") y X para hasAuthority("X") */
        return List.of(new SimpleGrantedAuthority("ROLE_" + autoridad), new SimpleGrantedAuthority(autoridad));
    }

    public String getNombre() {
        return nombre;
    }

    /** Rol tal como lo ve la pantalla: "Administrador" o "Empleado". */
    public String getRol() {
        return rol;
    }
}
