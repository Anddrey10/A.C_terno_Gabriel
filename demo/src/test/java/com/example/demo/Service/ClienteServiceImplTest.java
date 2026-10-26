package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.Dao.CalificacionDao;
import com.example.demo.Dao.ClienteDao;
import com.example.demo.Model.Calificacion;
import com.example.demo.Model.Cliente;
import com.example.demo.Service.impl.ClienteServiceImpl;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteDao dao;
    @Mock
    private CalificacionDao calificaciones;

    private ClienteServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new ClienteServiceImpl(dao, calificaciones);
    }

    private Cliente cliente(Long id, String dni) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setDni(dni);
        return c;
    }

    private Calificacion estrellas(int n) {
        Calificacion c = new Calificacion();
        c.setEstrellas(n);
        return c;
    }

    @Test
    void crearConDniRepetidoFalla() {
        when(dao.buscarPorDni("46281573")).thenReturn(Optional.of(cliente(1L, "46281573")));

        assertThatThrownBy(() -> servicio.crear(cliente(null, "46281573")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Ya existe un cliente registrado con ese DNI");
    }

    @Test
    void calificarGuardaElPromedioDeTodasLasCalificaciones() {
        Cliente c = cliente(1L, "46281573");
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(c));
        when(calificaciones.buscarPorCliente(1L)).thenReturn(List.of(estrellas(5), estrellas(4), estrellas(4)));
        when(dao.guardar(any(Cliente.class))).thenAnswer(i -> i.getArgument(0));

        Cliente resultado = servicio.calificar(1L, "4", "  Buen cliente  ");

        assertThat(resultado.getPromedio()).isEqualTo(4.33);
        assertThat(resultado.getCantidadCalificaciones()).isEqualTo(3);
        assertThat(resultado.getCalificacion()).isEqualTo(4);
        assertThat(resultado.getComentario()).isEqualTo("Buen cliente");
    }

    @Test
    void calificarRechazaValoresFueraDeRango() {
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(cliente(1L, "46281573")));

        assertThatThrownBy(() -> servicio.calificar(1L, 7, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("La calificación debe ser un número del 1 al 5");
    }

    @Test
    void calificarRechazaTextoQueNoEsNumero() {
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(cliente(1L, "46281573")));

        assertThatThrownBy(() -> servicio.calificar(1L, "abc", ""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calificarClienteInexistenteFalla() {
        when(dao.buscarPorId(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.calificar(9L, 5, ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("El cliente no existe");
    }
}
