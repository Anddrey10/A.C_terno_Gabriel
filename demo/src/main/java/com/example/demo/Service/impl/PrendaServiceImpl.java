package com.example.demo.Service.impl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.PrendaDao;
import com.example.demo.Model.Prenda;
import com.example.demo.Service.PrendaService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

@Service
public class PrendaServiceImpl implements PrendaService {

    private static final Logger LOG = LoggerFactory.getLogger(PrendaServiceImpl.class);
    private static final String NO_EXISTE = "La prenda no existe";
    private static final ImmutableList<String> CATEGORIAS = ImmutableList.of("Niños", "Jóvenes", "Adultos");

    private final PrendaDao dao;

    public PrendaServiceImpl(PrendaDao dao) {
        this.dao = dao;
    }

    @Override
    public List<Prenda> listar() {
        return dao.listar();
    }

    @Override
    public String siguienteCodigo(String prefijo) {
        String pre = StringUtils.upperCase(StringUtils.defaultString(prefijo));
        Preconditions.checkArgument(pre.matches("[A-Z]{3}"), "El prefijo debe tener 3 letras");
        int max = dao.listar().stream()
            .map(Prenda::getCodigo)
            .filter(c -> c != null && c.matches(pre + "-\\d+"))
            .mapToInt(c -> Integer.parseInt(c.substring(4)))
            .max().orElse(0);
        return String.format("%s-%03d", pre, max + 1);
    }

    /* Una prenda nueva siempre entra como Disponible: después solo cambia con las boletas */
    @Override
    @Transactional
    public Prenda crear(Prenda nueva) {
        Preconditions.checkArgument(dao.listar().stream().noneMatch(p -> p.getCodigo().equals(nueva.getCodigo())),
            "Ya existe una prenda con ese código");
        Preconditions.checkArgument(CATEGORIAS.contains(nueva.getCategoria()),
            "Debe elegir una categoría: Niños, Jóvenes o Adultos");
        nueva.setEstado("Disponible");
        nueva.setSuspendida(false);
        Prenda guardada = dao.guardar(nueva);
        LOG.info("Prenda {} registrada", guardada.getCodigo());
        return guardada;
    }

    /* El administrador solo puede cambiar tipo, precios e imagen; categoría, estado y código quedan fijos */
    @Override
    @Transactional
    public Prenda actualizar(Long id, Prenda datos) {
        Prenda actual = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        Preconditions.checkArgument(!"Vendido".equals(actual.getEstado()), "Una prenda vendida es de solo consulta y no se puede editar");
        actual.setTipo(datos.getTipo());
        actual.setPrecioVenta(datos.getPrecioVenta());
        actual.setPrecioAlquiler(datos.getPrecioAlquiler());
        if (StringUtils.isNotBlank(datos.getImagen())) actual.setImagen(datos.getImagen());
        return dao.guardar(actual);
    }

    /* Cambio manual de estado: solo Restauración → Lavandería/Disponible y Lavandería → Disponible */
    @Override
    @Transactional
    public Prenda cambiarEstado(Long id, String estado) {
        Prenda p = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        boolean permitido =
            ("En restauración".equals(p.getEstado()) && ImmutableList.of("En lavandería", "Disponible").contains(estado)) ||
            ("En lavandería".equals(p.getEstado()) && "Disponible".equals(estado));
        Preconditions.checkArgument(permitido, "El estado de esta prenda solo cambia con las boletas de alquiler y devolución");
        p.setEstado(estado);
        return dao.guardar(p);
    }

    @Override
    @Transactional
    public Prenda suspender(Long id) {
        Prenda p = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        Preconditions.checkArgument(!"Vendido".equals(p.getEstado()), "Una prenda vendida no se puede suspender");
        Preconditions.checkArgument(!"Alquilado".equals(p.getEstado()), "No se puede suspender una prenda que está alquilada");
        p.setSuspendida(true);
        return dao.guardar(p);
    }

    @Override
    @Transactional
    public Prenda reactivar(Long id) {
        Prenda p = dao.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException(NO_EXISTE));
        Preconditions.checkArgument(!"Vendido".equals(p.getEstado()), "Una prenda vendida no se puede reactivar");
        p.setSuspendida(false);
        return dao.guardar(p);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        dao.eliminarPorId(id);
    }
}
