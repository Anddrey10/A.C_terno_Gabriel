package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
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
import com.example.demo.Service.impl.EmpleadoServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock
    private EmpleadoDao dao;

    private final GestorContrasenas gestor = new GestorContrasenas(new BCryptPasswordEncoder(4));
    private EmpleadoServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new EmpleadoServiceImpl(dao, gestor);
    }

    private Empleado empleado(String codigo, String usuario) {
        Empleado e = new Empleado();
        e.setCodigo(codigo);
        e.setUsuario(usuario);
        e.setContrasena("clave123");
        e.setEstado("Activo");
        e.setRol("Empleado");
        return e;
    }

    @Test
    void crearGeneraElSiguienteCodigoCifraLaClaveYDejaAlEmpleadoActivo() {
        when(dao.buscarPorUsuario("nuevo1")).thenReturn(Optional.empty());
        when(dao.listar()).thenReturn(List.of(empleado("E001", "a1"), empleado("E004", "a2")));
        when(dao.existePorCodigo("E005")).thenReturn(false);
        when(dao.guardar(any(Empleado.class))).thenAnswer(i -> i.getArgument(0));

        Empleado creado = servicio.crear(empleado(null, "nuevo1"));

        assertThat(creado.getCodigo()).isEqualTo("E005");
        assertThat(creado.getEstado()).isEqualTo("Activo");
        assertThat(gestor.estaCifrada(creado.getContrasena())).isTrue();
        assertThat(gestor.coincide("clave123", creado.getContrasena())).isTrue();
    }

    @Test
    void crearConUsuarioRepetidoFalla() {
        when(dao.buscarPorUsuario("repetido")).thenReturn(Optional.of(empleado("E001", "repetido")));

        assertThatThrownBy(() -> servicio.crear(empleado(null, "repetido")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Ya existe un empleado con ese usuario");
    }

    @Test
    void suspenderCambiaElEstado() {
        Empleado e = empleado("E001", "a1");
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(e));
        when(dao.guardar(e)).thenReturn(e);

        assertThat(servicio.suspender(1L).getEstado()).isEqualTo("Suspendido");
    }

    @Test
    void restablecerDevuelveLaTemporalYGuardaSoloElHash() {
        Empleado e = empleado("E001", "a1");
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(e));

        String temporal = servicio.restablecerContrasena(1L);

        assertThat(temporal).hasSize(8);
        assertThat(e.getContrasena()).isNotEqualTo(temporal);
        assertThat(gestor.coincide(temporal, e.getContrasena())).isTrue();
        verify(dao).guardar(e);
    }

    @Test
    void cifrarPendientesSoloTocaLasContrasenasEnTextoPlano() {
        Empleado plano = empleado("E001", "a1");
        Empleado ya = empleado("E002", "a2");
        ya.setContrasena(gestor.cifrar("otra"));
        when(dao.listar()).thenReturn(List.of(plano, ya));

        assertThat(servicio.cifrarContrasenasPendientes()).isEqualTo(1);
        assertThat(gestor.estaCifrada(plano.getContrasena())).isTrue();
    }
}
