package com.example.demo.Seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.example.demo.Model.Empleado;
import com.example.demo.Service.AuthService;

@ExtendWith(MockitoExtension.class)
class ProveedorAutenticacionTest {

    @Mock
    private AuthService autenticacion;

    private ProveedorAutenticacion proveedor;

    @BeforeEach
    void preparar() {
        proveedor = new ProveedorAutenticacion(autenticacion);
    }

    private Empleado empleado(String rol) {
        Empleado e = new Empleado();
        e.setUsuario("maria");
        e.setNombre("María");
        e.setRol(rol);
        return e;
    }

    @Test
    void unAdministradorRecibeElRolAdmin() {
        when(autenticacion.autenticar("maria", "clave1")).thenReturn(Optional.of(empleado("Administrador")));

        Authentication resultado = proveedor.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated("maria", "clave1"));

        assertThat(resultado.isAuthenticated()).isTrue();
        assertThat(resultado.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("ROLE_ADMIN", "ADMIN");
        assertThat(((UsuarioSesion) resultado.getPrincipal()).getRol()).isEqualTo("Administrador");
    }

    @Test
    void unEmpleadoRecibeElRolEmpleado() {
        when(autenticacion.autenticar("maria", "clave1")).thenReturn(Optional.of(empleado("Empleado")));

        Authentication resultado = proveedor.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated("maria", "clave1"));

        assertThat(resultado.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("ROLE_EMPLEADO", "EMPLEADO");
    }

    @Test
    void conCredencialesInvalidasSeRechaza() {
        when(autenticacion.autenticar("maria", "mala")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> proveedor.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated("maria", "mala")))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void elBloqueoPorIntentosNoSeOculta() {
        when(autenticacion.autenticar("maria", "mala")).thenThrow(new IntentosExcedidosException("bloqueado"));

        assertThatThrownBy(() -> proveedor.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated("maria", "mala")))
            .isInstanceOf(IntentosExcedidosException.class);
    }
}
