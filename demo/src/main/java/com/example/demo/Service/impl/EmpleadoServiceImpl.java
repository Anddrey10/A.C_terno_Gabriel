package com.example.demo.Service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.EmpleadoDao;
import com.example.demo.Model.Empleado;
import com.example.demo.Seguridad.GestorContrasenas;
import com.example.demo.Service.EmpleadoService;
import com.google.common.base.Preconditions;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private static final Logger LOG = LoggerFactory.getLogger(EmpleadoServiceImpl.class);
    private static final String NO_EXISTE = "El empleado no existe";

    private final EmpleadoDao dao;
    private final GestorContrasenas contrasenas;

    public EmpleadoServiceImpl(EmpleadoDao dao, GestorContrasenas contrasenas) {
        this.dao = dao;
        this.contrasenas = contrasenas;
    }

    @Override
    public List<Empleado> listar() {
        return dao.listar();
    }

    @Override
    @Transactional
    public Empleado crear(Empleado nuevo) {
        Preconditions.checkArgument(dao.buscarPorUsuario(nuevo.getUsuario()).isEmpty(), "Ya existe un empleado con ese usuario");
        nuevo.setCodigo(siguienteCodigo());
        nuevo.setEstado("Activo");
        nuevo.setContrasena(contrasenas.cifrar(nuevo.getContrasena()));
        Empleado guardado = dao.guardar(nuevo);
        LOG.info("Empleado {} creado con rol {}", guardado.getCodigo(), guardado.getRol());
        return guardado;
    }

    @Override
    @Transactional
    public Empleado actualizar(Long id, Empleado datos) {
        Empleado actual = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        datos.setId(id);
        datos.setCodigo(actual.getCodigo());
        datos.setContrasena(contrasenas.cifrar(datos.getContrasena()));
        return dao.guardar(datos);
    }

    @Override
    @Transactional
    public Empleado suspender(Long id) {
        Empleado e = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        e.setEstado("Suspendido");
        LOG.info("Empleado {} suspendido", e.getCodigo());
        return dao.guardar(e);
    }

    @Override
    @Transactional
    public Empleado reactivar(Long id) {
        Empleado e = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        e.setEstado("Activo");
        LOG.info("Empleado {} reactivado", e.getCodigo());
        return dao.guardar(e);
    }

    @Override
    @Transactional
    public String restablecerContrasena(Long id) {
        Empleado e = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        String temporal = contrasenas.generarTemporal();
        e.setContrasena(contrasenas.cifrar(temporal));
        dao.guardar(e);
        LOG.info("Contraseña restablecida para el empleado {}", e.getCodigo());
        return temporal;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        dao.eliminarPorId(id);
        LOG.info("Empleado con id {} eliminado", id);
    }

    @Override
    @Transactional
    public int cifrarContrasenasPendientes() {
        int cifradas = 0;
        for (Empleado e : dao.listar()) {
            if (e.getContrasena() != null && !contrasenas.estaCifrada(e.getContrasena())) {
                e.setContrasena(contrasenas.cifrar(e.getContrasena()));
                dao.guardar(e);
                cifradas++;
            }
        }
        if (cifradas > 0) LOG.info("Se cifraron {} contraseña(s) que estaban en texto plano", cifradas);
        return cifradas;
    }

    /* El código (E001, E002...) lo genera el sistema, no se escribe a mano */
    private String siguienteCodigo() {
        int max = dao.listar().stream()
            .map(Empleado::getCodigo)
            .filter(c -> c != null && c.matches("E\\d{3}"))
            .mapToInt(c -> Integer.parseInt(c.substring(1)))
            .max().orElse(0);
        String codigo;
        do { codigo = String.format("E%03d", ++max); } while (dao.existePorCodigo(codigo));
        return codigo;
    }
}
