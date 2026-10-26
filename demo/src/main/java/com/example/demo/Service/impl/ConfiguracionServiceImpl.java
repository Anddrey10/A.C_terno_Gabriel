package com.example.demo.Service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.ConfiguracionDao;
import com.example.demo.Model.Configuracion;
import com.example.demo.Model.Empleado;
import com.example.demo.Repository.EmpleadoRepository;
import com.example.demo.Service.ConfiguracionService;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfiguracionServiceImpl.class);

    private final ConfiguracionDao dao;
    private final EmpleadoRepository empleados;

    @org.springframework.beans.factory.annotation.Autowired
    public ConfiguracionServiceImpl(ConfiguracionDao dao, EmpleadoRepository empleados) {
        this.dao = dao;
        this.empleados = empleados;
    }

    public ConfiguracionServiceImpl(ConfiguracionDao dao) {
        this(dao, null);
    }

    @Override
    public List<Configuracion> listar() {
        return dao.listar();
    }

    /* El administrador cambia un valor (penalidadDemora, penalidadMalEstado) y el empleado lo usa al registrar devoluciones */
    @Override
    @Transactional
    public Configuracion cambiar(String clave, Object valorRecibido, Object modificadoPor) {
        Configuracion c = dao.buscarPorClave(clave)
            .orElseThrow(() -> new IllegalArgumentException("No existe la configuración " + clave));
        Double valor = Doubles.tryParse(String.valueOf(valorRecibido));
        Preconditions.checkArgument(valor != null, "El monto debe ser un número");
        Preconditions.checkArgument(valor > 0, "El monto debe ser mayor a 0");
        c.setValor(valor);
        String referencia = modificadoPor == null ? "admin" : String.valueOf(modificadoPor);
        c.setModificadoPor(referencia);
        Empleado responsable = null;
        if (empleados != null) {
            responsable = empleados.findByUsuarioIgnoreCase(referencia)
                .or(() -> empleados.findByCodigo(referencia))
                .orElseGet(() -> empleados.findAll().stream()
                    .filter(e -> e.getNombre() != null && e.getNombre().equalsIgnoreCase(referencia))
                    .findFirst().orElse(null));
        }
        if (responsable != null) c.setEmpleadoResponsable(responsable);
        c.setModificado(LocalDateTime.now());
        LOG.info("Configuración {} actualizada", clave);
        return dao.guardar(c);
    }
}
