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
import com.example.demo.Dao.PrendaDao;
import com.example.demo.Model.Prenda;
import com.example.demo.Service.impl.PrendaServiceImpl;

@ExtendWith(MockitoExtension.class)
class PrendaServiceImplTest {

    @Mock
    private PrendaDao dao;

    private PrendaServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new PrendaServiceImpl(dao);
    }

    private Prenda prenda(String codigo, String estado) {
        Prenda p = new Prenda();
        p.setCodigo(codigo);
        p.setEstado(estado);
        p.setCategoria("Adultos");
        return p;
    }

    @Test
    void siguienteCodigoContinuaElCorrelativoDelPrefijo() {
        when(dao.listar()).thenReturn(List.of(prenda("TRN-001", "Disponible"), prenda("TRN-004", "Disponible"), prenda("SMK-009", "Disponible")));

        assertThat(servicio.siguienteCodigo("trn")).isEqualTo("TRN-005");
    }

    @Test
    void siguienteCodigoExigeTresLetras() {
        assertThatThrownBy(() -> servicio.siguienteCodigo("ab1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("El prefijo debe tener 3 letras");
    }

    @Test
    void unaPrendaNuevaEntraDisponibleYNoSuspendida() {
        Prenda nueva = prenda("TRN-010", "Vendido");
        nueva.setSuspendida(true);
        when(dao.listar()).thenReturn(List.of());
        when(dao.guardar(any(Prenda.class))).thenAnswer(i -> i.getArgument(0));

        Prenda guardada = servicio.crear(nueva);

        assertThat(guardada.getEstado()).isEqualTo("Disponible");
        assertThat(guardada.isSuspendida()).isFalse();
    }

    @Test
    void crearConCodigoRepetidoFalla() {
        when(dao.listar()).thenReturn(List.of(prenda("TRN-001", "Disponible")));

        assertThatThrownBy(() -> servicio.crear(prenda("TRN-001", "Disponible")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Ya existe una prenda con ese código");
    }

    @Test
    void crearConCategoriaInvalidaFalla() {
        Prenda nueva = prenda("TRN-011", "Disponible");
        nueva.setCategoria("Mascotas");
        when(dao.listar()).thenReturn(List.of());

        assertThatThrownBy(() -> servicio.crear(nueva)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noSePuedeSuspenderUnaPrendaVendidaNiAlquilada() {
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(prenda("TRN-001", "Vendido")));
        when(dao.buscarPorId(2L)).thenReturn(Optional.of(prenda("TRN-002", "Alquilado")));

        assertThatThrownBy(() -> servicio.suspender(1L)).hasMessage("Una prenda vendida no se puede suspender");
        assertThatThrownBy(() -> servicio.suspender(2L)).hasMessage("No se puede suspender una prenda que está alquilada");
    }

    @Test
    void elEstadoSoloCambiaDeRestauracionOLavanderiaHaciaDisponible() {
        Prenda enLavanderia = prenda("TRN-001", "En lavandería");
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(enLavanderia));
        when(dao.guardar(enLavanderia)).thenReturn(enLavanderia);

        assertThat(servicio.cambiarEstado(1L, "Disponible").getEstado()).isEqualTo("Disponible");
    }

    @Test
    void unaPrendaAlquiladaNoPuedeCambiarDeEstadoAMano() {
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(prenda("TRN-001", "Alquilado")));

        assertThatThrownBy(() -> servicio.cambiarEstado(1L, "Disponible"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
