package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.example.demo.Dao.EmpleadoDao;
import com.example.demo.Model.Empleado;
import com.example.demo.Seguridad.GestorContrasenas;
import com.example.demo.Seguridad.IntentosExcedidosException;
import com.example.demo.Seguridad.LimitadorIntentos;
import com.example.demo.Service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EmpleadoDao empleados;

    private final GestorContrasenas gestor = new GestorContrasenas(new BCryptPasswordEncoder(4));
    private AuthServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new AuthServiceImpl(empleados, gestor, new LimitadorIntentos(3, 5));
    }

    private Empleado empleado(String usuario, String contrasenaGuardada, String estado) {
        Empleado e = new Empleado();
        e.setUsuario(usuario);
        e.setContrasena(contrasenaGuardada);
        e.setEstado(estado);
        e.setRol("Empleado");
        e.setNombre("Carlos Ríos");
        return e;
    }

    @Test
    void credencialesCorrectasDevuelvenAlEmpleado() {
        when(empleados.buscarPorUsuario("carlos")).thenReturn(Optional.of(empleado("carlos", gestor.cifrar("clave123"), "Activo")));

        assertThat(servicio.autenticar("carlos", "clave123")).isPresent();
    }

    @Test
    void contrasenaIncorrectaNoAutentica() {
        when(empleados.buscarPorUsuario("carlos")).thenReturn(Optional.of(empleado("carlos", gestor.cifrar("clave123"), "Activo")));

        assertThat(servicio.autenticar("carlos", "mala")).isEmpty();
    }

    @Test
    void unEmpleadoSuspendidoNoPuedeIngresar() {
        when(empleados.buscarPorUsuario("carlos")).thenReturn(Optional.of(empleado("carlos", gestor.cifrar("clave123"), "Suspendido")));

        assertThat(servicio.autenticar("carlos", "clave123")).isEmpty();
    }

    @Test
    void usuarioInexistenteNoAutentica() {
        when(empleados.buscarPorUsuario("nadie")).thenReturn(Optional.empty());

        assertThat(servicio.autenticar("nadie", "x")).isEmpty();
    }

    @Test
    void tresFallosSeguidosBloqueanElUsuario() {
        when(empleados.buscarPorUsuario("carlos")).thenReturn(Optional.of(empleado("carlos", gestor.cifrar("clave123"), "Activo")));

        servicio.autenticar("carlos", "a");
        servicio.autenticar("carlos", "b");
        servicio.autenticar("carlos", "c");

        assertThatThrownBy(() -> servicio.autenticar("carlos", "clave123"))
            .isInstanceOf(IntentosExcedidosException.class);
    }

    @Test
    void laContrasenaEnTextoPlanoSeCifraAlPrimerIngreso() {
        Empleado antiguo = empleado("admin", "admin", "Activo");
        when(empleados.buscarPorUsuario("admin")).thenReturn(Optional.of(antiguo));

        assertThat(servicio.autenticar("admin", "admin")).isPresent();

        verify(empleados).guardar(antiguo);
        assertThat(gestor.estaCifrada(antiguo.getContrasena())).isTrue();
    }
}
