package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.Dao.BoletaDao;
import com.example.demo.Dao.ClienteDao;
import com.example.demo.Dao.ConfeccionDao;
import com.example.demo.Dao.PrendaDao;
import com.example.demo.Model.Boleta;
import com.example.demo.Model.Cliente;
import com.example.demo.Model.Prenda;
import com.example.demo.Service.impl.BoletaServiceImpl;

@ExtendWith(MockitoExtension.class)
class BoletaServiceImplTest {

    @Mock
    private BoletaDao boletas;
    @Mock
    private PrendaDao prendas;
    @Mock
    private ClienteDao clientes;
    @Mock
    private ConfeccionDao confecciones;

    private BoletaServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new BoletaServiceImpl(boletas, prendas, clientes, confecciones);
    }

    private Cliente cliente() {
        Cliente c = new Cliente();
        c.setId(1L);
        return c;
    }

    private Prenda prenda(String estado) {
        Prenda p = new Prenda();
        p.setId(5L);
        p.setCodigo("TRN-001");
        p.setEstado(estado);
        p.setPrecioVenta(420.0);
        p.setPrecioAlquiler(95.0);
        return p;
    }

    private Boleta pedido(String tipo) {
        Boleta b = new Boleta();
        b.setTipo(tipo);
        b.setCliente(cliente());
        b.setPrenda(prenda("Disponible"));
        return b;
    }

    /* Simula la base de datos: asigna el id la primera vez que se guarda */
    private void simularGuardadoDeBoletas() {
        when(boletas.guardar(any(Boleta.class))).thenAnswer(i -> {
            Boleta b = i.getArgument(0);
            if (b.getId() == null) b.setId(7L);
            return b;
        });
    }

    @Test
    void unaVentaTomaElPrecioDeVentaGeneraElNumeroYMarcaLaPrendaVendida() {
        Prenda enInventario = prenda("Disponible");
        when(prendas.buscarPorId(5L)).thenReturn(Optional.of(enInventario));
        when(clientes.buscarPorId(1L)).thenReturn(Optional.of(cliente()));
        simularGuardadoDeBoletas();

        Boleta emitida = servicio.emitir(pedido("Venta"));

        assertThat(emitida.getNumero()).isEqualTo("B001-0007");
        assertThat(emitida.getMonto()).isEqualTo(420.0);
        assertThat(emitida.getEstado()).isEqualTo("Vigente");
        assertThat(enInventario.getEstado()).isEqualTo("Vendido");
        verify(prendas).guardar(enInventario);
    }

    @Test
    void unAlquilerExigeFechaDeDevolucion() {
        when(prendas.buscarPorId(5L)).thenReturn(Optional.of(prenda("Disponible")));
        when(clientes.buscarPorId(1L)).thenReturn(Optional.of(cliente()));

        assertThatThrownBy(() -> servicio.emitir(pedido("Alquiler")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Debe indicar la fecha de devolución del alquiler");
    }

    @Test
    void noSeAlquilaAUnClienteConAlquilerBloqueado() {
        Cliente bloqueado = cliente();
        bloqueado.setAlquilerBloqueado(true);
        Boleta alquiler = pedido("Alquiler");
        alquiler.setFechaDevolucion(LocalDate.now().plusDays(3));
        when(prendas.buscarPorId(5L)).thenReturn(Optional.of(prenda("Disponible")));
        when(clientes.buscarPorId(1L)).thenReturn(Optional.of(bloqueado));

        assertThatThrownBy(() -> servicio.emitir(alquiler))
            .hasMessage("El cliente tiene el alquiler bloqueado");
    }

    @Test
    void noSeEmiteBoletaDeUnaPrendaNoDisponible() {
        when(prendas.buscarPorId(5L)).thenReturn(Optional.of(prenda("Vendido")));
        when(clientes.buscarPorId(1L)).thenReturn(Optional.of(cliente()));

        assertThatThrownBy(() -> servicio.emitir(pedido("Venta")))
            .hasMessage("La prenda no está disponible");
    }

    @Test
    void laFechaDeDevolucionNoPuedeSerPasada() {
        Boleta alquiler = pedido("Alquiler");
        alquiler.setFechaDevolucion(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> servicio.emitir(alquiler))
            .hasMessage("La fecha de devolución no puede ser pasada");
    }

    @Test
    void anularSinMotivoFalla() {
        assertThatThrownBy(() -> servicio.anular(1L, "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("El motivo de anulación es obligatorio");
    }

    @Test
    void anularUnaVentaVigenteLiberaLaPrenda() {
        Prenda vendida = prenda("Vendido");
        Boleta venta = new Boleta();
        venta.setId(7L);
        venta.setTipo("Venta");
        venta.setEstado("Vigente");
        venta.setPrenda(vendida);
        when(boletas.buscarPorId(7L)).thenReturn(Optional.of(venta));
        when(boletas.guardar(venta)).thenReturn(venta);

        Boleta anulada = servicio.anular(7L, "Error de digitación");

        assertThat(anulada.getEstado()).isEqualTo("Anulada");
        assertThat(anulada.getMotivoAnulacion()).isEqualTo("Error de digitación");
        assertThat(vendida.getEstado()).isEqualTo("Disponible");
    }

    @Test
    void noSePuedeAnularUnaBoletaYaAnulada() {
        Boleta anulada = new Boleta();
        anulada.setTipo("Venta");
        anulada.setEstado("Anulada");
        when(boletas.buscarPorId(7L)).thenReturn(Optional.of(anulada));

        assertThatThrownBy(() -> servicio.anular(7L, "otra vez"))
            .hasMessage("La boleta ya fue anulada");
    }

    @Test
    void devolverEnviaLaPrendaALavanderia() {
        Prenda alquilada = prenda("Alquilado");
        Boleta alquiler = new Boleta();
        alquiler.setTipo("Alquiler");
        alquiler.setEstado("Vigente");
        alquiler.setPrenda(alquilada);
        when(boletas.buscarPorId(3L)).thenReturn(Optional.of(alquiler));
        when(boletas.guardar(alquiler)).thenReturn(alquiler);

        Boleta devuelta = servicio.devolver(3L, "En lavandería");

        assertThat(devuelta.getEstado()).isEqualTo("Devuelta");
        assertThat(alquilada.getEstado()).isEqualTo("En lavandería");
    }

    @Test
    void devolverConEstadoDePrendaInvalidoFalla() {
        Boleta alquiler = new Boleta();
        alquiler.setTipo("Alquiler");
        alquiler.setEstado("Vigente");
        alquiler.setPrenda(prenda("Alquilado"));
        when(boletas.buscarPorId(3L)).thenReturn(Optional.of(alquiler));

        assertThatThrownBy(() -> servicio.devolver(3L, "Disponible"))
            .hasMessage("Estado de prenda no válido para una devolución");
    }
}
