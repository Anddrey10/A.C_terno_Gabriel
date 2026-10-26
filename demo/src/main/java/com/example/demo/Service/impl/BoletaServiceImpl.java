package com.example.demo.Service.impl;

import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.BoletaDao;
import com.example.demo.Dao.ClienteDao;
import com.example.demo.Dao.ConfeccionDao;
import com.example.demo.Dao.PrendaDao;
import com.example.demo.Model.Boleta;
import com.example.demo.Model.Cliente;
import com.example.demo.Model.Confeccion;
import com.example.demo.Model.Prenda;
import com.example.demo.Model.Empleado;
import com.example.demo.Repository.EmpleadoRepository;
import com.example.demo.Service.BoletaService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

@Service
public class BoletaServiceImpl implements BoletaService {

    private static final Logger LOG = LoggerFactory.getLogger(BoletaServiceImpl.class);
    private static final String NO_EXISTE = "La boleta no existe";

    private final BoletaDao boletas;
    private final PrendaDao prendas;
    private final ClienteDao clientes;
    private final ConfeccionDao confecciones;
    private final EmpleadoRepository empleados;

    @org.springframework.beans.factory.annotation.Autowired
    public BoletaServiceImpl(BoletaDao boletas, PrendaDao prendas, ClienteDao clientes,
                             ConfeccionDao confecciones, EmpleadoRepository empleados) {
        this.boletas = boletas;
        this.prendas = prendas;
        this.clientes = clientes;
        this.confecciones = confecciones;
        this.empleados = empleados;
    }

    /* Constructor de compatibilidad para pruebas y código legado. Spring utiliza el de cinco argumentos. */
    public BoletaServiceImpl(BoletaDao boletas, PrendaDao prendas, ClienteDao clientes, ConfeccionDao confecciones) {
        this(boletas, prendas, clientes, confecciones, null);
    }

    @Override
    public List<Boleta> listar() {
        return boletas.listar();
    }

    /* Al generar la boleta: el monto sale del precio que definió el administrador en la prenda,
       se genera el número y cambia el estado de la prenda */
    @Override
    @Transactional
    public Boleta emitir(Boleta nueva) {
        nueva.setId(null);
        nueva.setMotivoAnulacion(null);
        nueva.setEstado("Vigente");
        Preconditions.checkArgument(nueva.getFechaDevolucion() == null || !nueva.getFechaDevolucion().isBefore(LocalDate.now()),
            "La fecha de devolución no puede ser pasada");

        /* Boleta de confección: se entrega la orden y se cobra el saldo; el cliente viene del ticket */
        if ("Confección".equals(nueva.getTipo())) return emitirConfeccion(nueva);

        Preconditions.checkArgument(nueva.getPrenda() != null && nueva.getPrenda().getId() != null, "Debe seleccionar una prenda");
        Prenda prenda = prendas.buscarPorId(nueva.getPrenda().getId())
            .orElseThrow(() -> new IllegalArgumentException("La prenda no existe"));
        Cliente cliente = clientes.buscarPorId(nueva.getCliente().getId())
            .orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
        boolean alquiler = "Alquiler".equals(nueva.getTipo());
        Preconditions.checkArgument(!alquiler || nueva.getFechaDevolucion() != null, "Debe indicar la fecha de devolución del alquiler");
        if (!alquiler) nueva.setFechaDevolucion(null);
        Preconditions.checkArgument(!prenda.isSuspendida(), "La prenda está suspendida");
        Preconditions.checkArgument("Disponible".equals(prenda.getEstado()), "La prenda no está disponible");
        Preconditions.checkArgument(!alquiler || !cliente.isAlquilerBloqueado(), "El cliente tiene el alquiler bloqueado");

        nueva.setCliente(cliente);
        nueva.setPrenda(prenda);
        nueva.setMonto("Venta".equals(nueva.getTipo()) ? prenda.getPrecioVenta() : prenda.getPrecioAlquiler());

        if (alquiler) prenda.setEstado("Alquilado");
        if ("Reserva".equals(nueva.getTipo())) prenda.setEstado("Reservado");
        if ("Venta".equals(nueva.getTipo())) prenda.setEstado("Vendido");
        prendas.guardar(prenda);

        vincularEmpleado(nueva);
        Boleta guardada = boletas.guardar(nueva);
        guardada.setNumero(String.format("B001-%04d", guardada.getId()));
        guardada = boletas.guardar(guardada);
        LOG.info("Boleta {} emitida ({})", guardada.getNumero(), guardada.getTipo());
        return guardada;
    }

    private Boleta emitirConfeccion(Boleta nueva) {
        Preconditions.checkArgument(nueva.getConfeccion() != null && nueva.getConfeccion().getId() != null,
            "Debe indicar la orden de confección");
        Confeccion orden = confecciones.buscarPorId(nueva.getConfeccion().getId())
            .orElseThrow(() -> new IllegalArgumentException("La orden de confección no existe"));
        Preconditions.checkArgument("En confección".equals(orden.getEstado()), "La orden ya fue entregada");
        nueva.setCliente(orden.getCliente());
        nueva.setConfeccion(orden);
        nueva.setPrenda(null);
        /* Si el ticket ya se pagó completo no hay nada que cobrar: la boleta conserva la forma de pago elegida al
           generar el ticket. Si no llega forma de pago, se usa la del ticket (o Efectivo si es un ticket antiguo). */
        boolean pagadoCompleto = orden.getAdelanto() != null && orden.getAdelanto() + 0.005 >= orden.getCostoTotal();
        if (pagadoCompleto || StringUtils.isBlank(nueva.getMetodoPago()))
            nueva.setMetodoPago(StringUtils.defaultIfBlank(orden.getMetodoPago(), "Efectivo"));
        /* La boleta registra el costo total de la confección (adelanto + saldo), así los informes
           suman el ingreso real una sola vez; el ticket no cuenta como ingreso */
        nueva.setMonto(orden.getCostoTotal());
        vincularEmpleado(nueva);
        Boleta emitida = boletas.guardar(nueva);
        emitida.setNumero(String.format("B001-%04d", emitida.getId()));
        emitida = boletas.guardar(emitida);
        orden.setEstado("Entregada");
        orden.setBoleta(emitida.getNumero());
        confecciones.guardar(orden);
        LOG.info("Boleta {} emitida por entrega de confección {}", emitida.getNumero(), orden.getTicket());
        return emitida;
    }

    /* Devolución: la boleta de alquiler se cierra y la prenda vuelve al inventario */
    @Override
    @Transactional
    public Boleta devolver(Long id, String estadoPrenda) {
        Boleta boleta = boletas.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        Preconditions.checkArgument("Alquiler".equals(boleta.getTipo()) && "Vigente".equals(boleta.getEstado()),
            "Esta boleta no tiene una devolución pendiente");
        /* Devolución normal → Lavandería; devolución con daños → Restauración */
        Preconditions.checkArgument(ImmutableList.of("En lavandería", "En restauración").contains(estadoPrenda),
            "Estado de prenda no válido para una devolución");
        boleta.setEstado("Devuelta");
        boleta.getPrenda().setEstado(estadoPrenda);
        prendas.guardar(boleta.getPrenda());
        LOG.info("Devolución registrada para la boleta {}", boleta.getNumero());
        return boletas.guardar(boleta);
    }

    @Override
    @Transactional
    public Boleta anular(Long id, String motivo) {
        Preconditions.checkArgument(StringUtils.isNotBlank(motivo), "El motivo de anulación es obligatorio");
        Boleta boleta = boletas.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        Preconditions.checkArgument(!"Anulada".equals(boleta.getEstado()), "La boleta ya fue anulada");
        Preconditions.checkArgument(ImmutableList.of("Alquiler", "Venta").contains(boleta.getTipo()) && "Vigente".equals(boleta.getEstado()),
            "Solo se pueden anular boletas vigentes de alquiler o compra/venta. Las reservas, confecciones y operaciones cerradas no se pueden anular desde esta pantalla.");
        /* La prenda solo se libera si esta boleta todavía la tiene (una boleta ya devuelta no debe liberar
           una prenda que ahora está alquilada a otro cliente) */
        boolean tieneLaPrenda = "Vigente".equals(boleta.getEstado());
        boleta.setEstado("Anulada");
        boleta.setMotivoAnulacion(StringUtils.abbreviate(motivo.trim(), 255));
        if (boleta.getPrenda() != null && tieneLaPrenda) {
            boleta.getPrenda().setEstado("Disponible");
            prendas.guardar(boleta.getPrenda());
        }
        /* Si era la entrega de una confección, la orden vuelve a estar en confección */
        if (boleta.getConfeccion() != null) {
            boleta.getConfeccion().setEstado("En confección");
            boleta.getConfeccion().setBoleta(null);
            confecciones.guardar(boleta.getConfeccion());
        }
        LOG.info("Boleta {} anulada", boleta.getNumero());
        return boletas.guardar(boleta);
    }

    @Override
    @Transactional
    public Boleta actualizar(Long id, Boleta datos) {
        boletas.buscarPorId(id).orElseThrow();
        datos.setId(id);
        return boletas.guardar(datos);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        boletas.eliminarPorId(id);
    }

    private void vincularEmpleado(Boleta boleta) {
        if (empleados == null) return;
        if (boleta.getEmpleadoResponsable() != null && boleta.getEmpleadoResponsable().getId() != null) {
            empleados.findById(boleta.getEmpleadoResponsable().getId()).ifPresent(boleta::setEmpleadoResponsable);
            return;
        }
        String referencia = boleta.getEmpleado();
        if (referencia == null || referencia.isBlank()) return;
        Empleado encontrado = empleados.findByUsuarioIgnoreCase(referencia.trim())
            .or(() -> empleados.findByCodigo(referencia.trim()))
            .orElseGet(() -> empleados.findAll().stream()
                .filter(e -> e.getNombre() != null && e.getNombre().equalsIgnoreCase(referencia.trim()))
                .findFirst().orElse(null));
        if (encontrado != null) boleta.setEmpleadoResponsable(encontrado);
    }
}
