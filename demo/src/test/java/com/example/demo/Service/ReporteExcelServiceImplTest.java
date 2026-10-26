package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.example.demo.Service.impl.ReporteExcelServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReporteExcelServiceImplTest {

    @Mock
    private BoletaDao boletas;
    @Mock
    private ConfeccionDao confecciones;
    @Mock
    private PrendaDao prendas;
    @Mock
    private ClienteDao clientes;

    private ReporteExcelServiceImpl servicio;

    @BeforeEach
    void preparar() {
        servicio = new ReporteExcelServiceImpl(boletas, confecciones, prendas, clientes);
    }

    private Boleta boleta(String tipo, String estado, LocalDate fecha, String nombres, double monto) {
        Cliente c = new Cliente();
        c.setNombres(nombres);
        c.setApellidos("Quispe");
        Prenda p = new Prenda();
        p.setDiseno("Terno Clásico");
        Boleta b = new Boleta();
        b.setNumero("B001-0001");
        b.setTipo(tipo);
        b.setEstado(estado);
        b.setFecha(fecha);
        b.setCliente(c);
        b.setPrenda(p);
        b.setMonto(monto);
        return b;
    }

    private Sheet leer(byte[] archivo, String hoja) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(archivo)).getSheet(hoja);
    }

    @Test
    void elInformeDeVentasSoloIncluyeVentasDentroDelRango() throws IOException {
        when(boletas.listar()).thenReturn(List.of(
            boleta("Venta", "Vigente", LocalDate.of(2026, 10, 10), "Luis", 420.0),
            boleta("Venta", "Vigente", LocalDate.of(2026, 9, 1), "Ana", 300.0),
            boleta("Alquiler", "Vigente", LocalDate.of(2026, 10, 10), "Jorge", 95.0)));

        byte[] archivo = servicio.generarExcel("Ventas", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        Sheet hoja = leer(archivo, "Ventas");

        assertThat(hoja.getLastRowNum()).isEqualTo(1);
        assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Fecha");
        assertThat(hoja.getRow(1).getCell(2).getStringCellValue()).isEqualTo("Luis Quispe");
        assertThat(hoja.getRow(1).getCell(7).getNumericCellValue()).isEqualTo(420.0);
    }

    @Test
    void losTextosQueEmpiezanComoFormulaSeNeutralizan() throws IOException {
        when(boletas.listar()).thenReturn(List.of(
            boleta("Venta", "Vigente", LocalDate.of(2026, 10, 10), "=HYPERLINK(\"http://x\")", 10.0)));

        Sheet hoja = leer(servicio.generarExcel("Ventas", null, null), "Ventas");

        assertThat(hoja.getRow(1).getCell(2).getStringCellValue()).startsWith("'=");
    }

    @Test
    void elInformeDeInventarioListaLasPrendas() throws IOException {
        Prenda p = new Prenda();
        p.setCodigo("TRN-001");
        p.setDiseno("Terno Slim");
        p.setPrecioVenta(420.0);
        p.setPrecioAlquiler(95.0);
        when(prendas.listar()).thenReturn(List.of(p));

        Sheet hoja = leer(servicio.generarExcel("Inventario", null, null), "Inventario");

        assertThat(hoja.getRow(1).getCell(0).getStringCellValue()).isEqualTo("TRN-001");
        assertThat(hoja.getRow(1).getCell(6).getNumericCellValue()).isEqualTo(420.0);
    }

    @Test
    void unTipoDeInformeDesconocidoFalla() {
        assertThatThrownBy(() -> servicio.generarExcel("Secretos", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Tipo de informe no válido");
    }

    @Test
    void laFechaInicialNoPuedeSerPosteriorALaFinal() {
        assertThatThrownBy(() -> servicio.generarExcel("Ventas", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
