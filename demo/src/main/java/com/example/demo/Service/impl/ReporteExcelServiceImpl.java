package com.example.demo.Service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.demo.Dao.BoletaDao;
import com.example.demo.Dao.ClienteDao;
import com.example.demo.Dao.ConfeccionDao;
import com.example.demo.Dao.PrendaDao;
import com.example.demo.Model.Boleta;
import com.example.demo.Model.Cliente;
import com.example.demo.Model.Confeccion;
import com.example.demo.Model.Prenda;
import com.example.demo.Service.ReporteService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Exporta los informes del administrador a Excel con Apache POI.
 * <p>
 * Seguridad: toda celda de texto se neutraliza contra la inyección de fórmulas (CSV/Excel injection):
 * si empieza con =, +, - o @ se antepone un apóstrofo para que Excel la trate como texto.
 */
@Service
public class ReporteExcelServiceImpl implements ReporteService {

    private static final Logger LOG = LoggerFactory.getLogger(ReporteExcelServiceImpl.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final ImmutableList<String> TIPOS =
        ImmutableList.of("Ventas", "Alquileres", "Devoluciones", "Confecciones", "Inventario", "Clientes");

    private final BoletaDao boletas;
    private final ConfeccionDao confecciones;
    private final PrendaDao prendas;
    private final ClienteDao clientes;

    public ReporteExcelServiceImpl(BoletaDao boletas, ConfeccionDao confecciones, PrendaDao prendas, ClienteDao clientes) {
        this.boletas = boletas;
        this.confecciones = confecciones;
        this.prendas = prendas;
        this.clientes = clientes;
    }

    @Override
    public byte[] generarExcel(String tipo, LocalDate desde, LocalDate hasta) {
        Preconditions.checkArgument(TIPOS.contains(tipo), "Tipo de informe no válido");
        Preconditions.checkArgument(desde == null || hasta == null || !desde.isAfter(hasta),
            "La fecha inicial no puede ser posterior a la final");

        List<String> cabecera = new ArrayList<>();
        List<List<Object>> filas = new ArrayList<>();
        armarDatos(tipo, enRango(desde, hasta), cabecera, filas);

        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet(tipo);
            CellStyle estiloCabecera = libro.createCellStyle();
            Font negrita = libro.createFont();
            negrita.setBold(true);
            estiloCabecera.setFont(negrita);

            Row filaCabecera = hoja.createRow(0);
            for (int c = 0; c < cabecera.size(); c++) {
                Cell celda = filaCabecera.createCell(c);
                celda.setCellValue(cabecera.get(c));
                celda.setCellStyle(estiloCabecera);
                hoja.setColumnWidth(c, 22 * 256);
            }
            for (int f = 0; f < filas.size(); f++) {
                Row fila = hoja.createRow(f + 1);
                for (int c = 0; c < filas.get(f).size(); c++) {
                    Object valor = filas.get(f).get(c);
                    Cell celda = fila.createCell(c);
                    if (valor instanceof Number n) celda.setCellValue(n.doubleValue());
                    else celda.setCellValue(neutralizar(valor == null ? "" : String.valueOf(valor)));
                }
            }
            libro.write(salida);
            LOG.info("Informe Excel '{}' generado con {} fila(s)", tipo, filas.size());
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", e);
        }
    }

    private void armarDatos(String tipo, Predicate<LocalDate> dentroDelRango, List<String> cabecera, List<List<Object>> filas) {
        switch (tipo) {
            case "Confecciones" -> {
                cabecera.addAll(List.of("Fecha", "Ticket", "Cliente", "Producto", "Estado", "Empleado", "Total (S/)"));
                for (Confeccion c : confecciones.listar()) {
                    if (!dentroDelRango.test(c.getFecha())) continue;
                    filas.add(List.of(texto(c.getFecha()), texto(c.getTicket()), nombre(c.getCliente()), "Confección a medida",
                        texto(c.getEstado()), texto(c.getEmpleado()), numero(c.getCostoTotal())));
                }
            }
            case "Inventario" -> {
                cabecera.addAll(List.of("Código", "Diseño", "Tipo", "Talla", "Estado", "Categoría", "Precio venta (S/)", "Precio alquiler (S/)"));
                for (Prenda p : prendas.listar()) {
                    filas.add(List.of(texto(p.getCodigo()), texto(p.getDiseno()), texto(p.getTipo()), texto(p.getTalla()),
                        texto(p.getEstado()), texto(p.getCategoria()), numero(p.getPrecioVenta()), numero(p.getPrecioAlquiler())));
                }
            }
            case "Clientes" -> {
                cabecera.addAll(List.of("DNI", "Cliente", "Promedio de estrellas", "Calificaciones", "Última calificación"));
                for (Cliente c : clientes.listar()) {
                    if (c.getCalificacion() == null) continue;
                    filas.add(List.of(texto(c.getDni()), nombre(c), numero(c.getPromedio() != null ? c.getPromedio() : c.getCalificacion()),
                        numero(c.getCantidadCalificaciones()), texto(c.getFechaCalificacion())));
                }
            }
            default -> {
                cabecera.addAll(List.of("Fecha", "Documento", "Cliente", "Producto", "Tipo", "Estado", "Empleado", "Total (S/)"));
                for (Boleta b : boletas.listar()) {
                    if (!coincideTipo(tipo, b) || !dentroDelRango.test(b.getFecha())) continue;
                    filas.add(List.of(texto(b.getFecha()), texto(b.getNumero()), nombre(b.getCliente()), producto(b),
                        texto(b.getTipo()), texto(b.getEstado()), texto(b.getEmpleado()), numero(b.getMonto())));
                }
            }
        }
    }

    private boolean coincideTipo(String informe, Boleta b) {
        return switch (informe) {
            case "Ventas" -> "Venta".equals(b.getTipo());
            case "Alquileres" -> "Alquiler".equals(b.getTipo());
            case "Devoluciones" -> "Alquiler".equals(b.getTipo()) && "Devuelta".equals(b.getEstado());
            default -> false;
        };
    }

    /* Igual que la pantalla de informes: una fecha vacía siempre entra; si no, debe estar dentro del rango */
    private Predicate<LocalDate> enRango(LocalDate desde, LocalDate hasta) {
        return fecha -> fecha == null || ((desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta)));
    }

    private String producto(Boleta b) {
        if (b.getPrenda() != null) return texto(b.getPrenda().getDiseno());
        if (b.getConfeccion() != null && b.getConfeccion().getTicket() != null) return "Confección " + b.getConfeccion().getTicket();
        return "Servicio de confección";
    }

    private String nombre(Cliente c) {
        return c == null ? "—" : StringUtils.trimToEmpty(c.getNombres() + " " + c.getApellidos());
    }

    private String texto(Object valor) {
        if (valor == null) return "—";
        if (valor instanceof LocalDate f) return f.format(FECHA);
        return String.valueOf(valor);
    }

    private Number numero(Number valor) {
        return valor == null ? 0 : valor;
    }

    /* Evita que Excel interprete como fórmula un texto escrito por un usuario */
    static String neutralizar(String texto) {
        if (texto.isEmpty()) return texto;
        char primero = texto.charAt(0);
        boolean peligroso = primero == '=' || primero == '+' || primero == '-' || primero == '@' || primero == '\t' || primero == '\r';
        return peligroso ? "'" + texto : texto;
    }
}
