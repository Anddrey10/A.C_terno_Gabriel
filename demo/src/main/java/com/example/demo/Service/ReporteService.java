package com.example.demo.Service;

import java.time.LocalDate;

/** Casos de uso de informes exportables. */
public interface ReporteService {

    /**
     * Genera un libro de Excel (.xlsx) con la información del informe.
     *
     * @param tipo  Ventas, Alquileres, Devoluciones, Confecciones, Inventario o Clientes
     * @param desde fecha inicial (opcional)
     * @param hasta fecha final (opcional)
     * @return los bytes del archivo .xlsx
     */
    byte[] generarExcel(String tipo, LocalDate desde, LocalDate hasta);
}
