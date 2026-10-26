package com.example.demo.Service.impl;

import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.CalificacionDao;
import com.example.demo.Dao.ClienteDao;
import com.example.demo.Model.Calificacion;
import com.example.demo.Model.Cliente;
import com.example.demo.Service.ClienteService;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

@Service
public class ClienteServiceImpl implements ClienteService {

    private static final Logger LOG = LoggerFactory.getLogger(ClienteServiceImpl.class);
    private static final String ERROR_ESTRELLAS = "La calificación debe ser un número del 1 al 5";
    private static final int MAX_COMENTARIO = 255;

    private final ClienteDao dao;
    private final CalificacionDao calificaciones;

    public ClienteServiceImpl(ClienteDao dao, CalificacionDao calificaciones) {
        this.dao = dao;
        this.calificaciones = calificaciones;
    }

    @Override
    public List<Cliente> listar() {
        return dao.listar();
    }

    @Override
    @Transactional
    public Cliente crear(Cliente nuevo) {
        Preconditions.checkArgument(dao.buscarPorDni(nuevo.getDni()).isEmpty(), "Ya existe un cliente registrado con ese DNI");
        return dao.guardar(nuevo);
    }

    @Override
    @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        Cliente actual = dao.buscarPorId(id).orElseThrow();
        datos.setId(id);
        /* La calificación no se edita a mano: se conserva lo calculado */
        datos.setCalificacion(actual.getCalificacion());
        datos.setComentario(actual.getComentario());
        datos.setFechaCalificacion(actual.getFechaCalificacion());
        datos.setPromedio(actual.getPromedio());
        datos.setCantidadCalificaciones(actual.getCantidadCalificaciones());
        return dao.guardar(datos);
    }

    @Override
    @Transactional
    public Cliente calificar(Long id, Object estrellasRecibidas, Object comentarioRecibido) {
        Cliente cliente = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
        Integer estrellas = Ints.tryParse(String.valueOf(estrellasRecibidas));
        Preconditions.checkArgument(estrellas != null && estrellas >= 1 && estrellas <= 5, ERROR_ESTRELLAS);
        String comentario = comentarioRecibido == null ? ""
            : StringUtils.abbreviate(StringUtils.trimToEmpty(String.valueOf(comentarioRecibido)), MAX_COMENTARIO);

        /* Se guarda cada calificación y el cliente queda con el promedio aritmético de todas */
        Calificacion nueva = new Calificacion();
        nueva.setCliente(cliente);
        nueva.setEstrellas(estrellas);
        nueva.setComentario(comentario);
        nueva.setFecha(LocalDate.now());
        calificaciones.guardar(nueva);

        List<Calificacion> todas = calificaciones.buscarPorCliente(cliente.getId());
        double promedio = todas.stream().mapToInt(Calificacion::getEstrellas).average().orElse(estrellas);
        cliente.setPromedio(Math.round(promedio * 100.0) / 100.0);
        cliente.setCantidadCalificaciones(todas.size());
        /* Redondeo al entero más cercano para dibujar las estrellas (3.67 → 4); siempre entre 1 y 5 */
        cliente.setCalificacion((int) Math.min(5, Math.max(1, Math.round(promedio))));
        cliente.setComentario(comentario);
        cliente.setFechaCalificacion(LocalDate.now());
        LOG.info("Cliente {} calificado con {} estrella(s)", cliente.getId(), estrellas);
        return dao.guardar(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        dao.eliminarPorId(id);
    }
}
