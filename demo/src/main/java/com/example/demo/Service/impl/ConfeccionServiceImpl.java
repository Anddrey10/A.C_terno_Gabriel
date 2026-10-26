package com.example.demo.Service.impl;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.ClienteDao;
import com.example.demo.Dao.ConfeccionDao;
import com.example.demo.Model.Cliente;
import com.example.demo.Model.Confeccion;
import com.example.demo.Model.Empleado;
import com.example.demo.Repository.EmpleadoRepository;
import com.example.demo.Service.ConfeccionService;
import com.google.common.base.Preconditions;

@Service
public class ConfeccionServiceImpl implements ConfeccionService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfeccionServiceImpl.class);
    /* Los selectores de colores están guardados como saco/pantalon/chaleco/camisa/corbata */
    private static final Pattern COLORES = Pattern.compile("\"(?:saco|pantalon|chaleco|camisa|corbata)\"\\s*:\\s*\"([^\"]+)\"");

    private final ConfeccionDao dao;
    private final ClienteDao clientes;
    private final EmpleadoRepository empleados;

    @org.springframework.beans.factory.annotation.Autowired
    public ConfeccionServiceImpl(ConfeccionDao dao, ClienteDao clientes, EmpleadoRepository empleados) {
        this.dao = dao;
        this.clientes = clientes;
        this.empleados = empleados;
    }

    public ConfeccionServiceImpl(ConfeccionDao dao, ClienteDao clientes) {
        this(dao, clientes, null);
    }

    @Override
    public List<Confeccion> listar() {
        return dao.listar();
    }

    /* Registrar la orden emite el TICKET (T001-0001) */
    @Override
    @Transactional
    public Confeccion registrar(Confeccion nueva) {
        Preconditions.checkArgument(nueva.getCliente() != null && nueva.getCliente().getId() != null, "Debe seleccionar un cliente");
        nueva.setId(null);
        Cliente cliente = clientes.buscarPorId(nueva.getCliente().getId())
            .orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
        // Fechas predeterminadas desde la creación del ticket si el formulario no las envía.
        LocalDate fechaCreacion = nueva.getFecha() != null ? nueva.getFecha() : LocalDate.now();
        nueva.setFecha(fechaCreacion);
        if (nueva.getPrimeraPrueba() == null) nueva.setPrimeraPrueba(fechaCreacion.plusDays(5));
        if (nueva.getEntregaFinal() == null) nueva.setEntregaFinal(fechaCreacion.plusDays(10));
        Preconditions.checkArgument(StringUtils.isNotBlank(nueva.getDetalle()) && nueva.getDetalle().contains("\"medidas\""),
            "La ficha de medidas es obligatoria");
        Preconditions.checkArgument(!nueva.getEntregaFinal().isBefore(LocalDate.now()), "La fecha de entrega no puede ser anterior a hoy");
        Preconditions.checkArgument(!nueva.getPrimeraPrueba().isBefore(LocalDate.now()),
            "La fecha de la primera prueba no puede ser anterior a hoy");
        Preconditions.checkArgument(!nueva.getEntregaFinal().isBefore(nueva.getPrimeraPrueba()),
            "La entrega final no puede ser anterior a la primera prueba");
        Preconditions.checkArgument(nueva.getAdelanto() <= nueva.getCostoTotal(), "El adelanto no puede superar el costo total");
        // Compatibilidad con la tabla existente: color es NOT NULL. Si el frontend no
        // manda el campo separado, se deriva del detalle JSON enviado por la ficha.
        if (StringUtils.isBlank(nueva.getColor()) || "No especificado".equals(nueva.getColor())) {
            String colorDerivado = extraerColor(nueva.getDetalle());
            nueva.setColor(colorDerivado.isBlank() ? "No especificado" : colorDerivado);
        }
        nueva.setCliente(cliente);
        // La BD existente exige ambas columnas: entregaFinal y fechaEntrega.
        nueva.setFechaEntrega(nueva.getEntregaFinal());
        nueva.setEstado("En confección");
        nueva.setBoleta(null);
        vincularEmpleado(nueva);
        Confeccion guardada = dao.guardar(nueva);
        guardada.setTicket(String.format("T001-%04d", guardada.getId()));
        guardada = dao.guardar(guardada);
        LOG.info("Ticket {} emitido", guardada.getTicket());
        return guardada;
    }

    private String extraerColor(String detalle) {
        if (StringUtils.isBlank(detalle)) return "";
        Matcher m = COLORES.matcher(detalle);
        Set<String> colores = new LinkedHashSet<>();
        while (m.find()) {
            String valor = m.group(1).trim();
            if (!valor.isEmpty() && !"Seleccione".equalsIgnoreCase(valor) && !"--Seleccione--".equalsIgnoreCase(valor)) colores.add(valor);
        }
        return String.join(" / ", colores);
    }

    private void vincularEmpleado(Confeccion orden) {
        if (empleados == null) return;
        if (orden.getEmpleadoResponsable() != null && orden.getEmpleadoResponsable().getId() != null) {
            empleados.findById(orden.getEmpleadoResponsable().getId()).ifPresent(orden::setEmpleadoResponsable);
            return;
        }
        String referencia = orden.getEmpleado();
        if (referencia == null || referencia.isBlank()) return;
        Empleado encontrado = empleados.findByUsuarioIgnoreCase(referencia.trim())
            .or(() -> empleados.findByCodigo(referencia.trim()))
            .orElseGet(() -> empleados.findAll().stream()
                .filter(e -> e.getNombre() != null && e.getNombre().equalsIgnoreCase(referencia.trim()))
                .findFirst().orElse(null));
        if (encontrado != null) orden.setEmpleadoResponsable(encontrado);
    }
}
