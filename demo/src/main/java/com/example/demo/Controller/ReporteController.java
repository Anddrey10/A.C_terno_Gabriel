package com.example.demo.Controller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.Service.ReporteService;

/** Descarga de informes en Excel (botón "Exportar Excel" de la pantalla de informes). */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final MediaType XLSX =
        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("excel")
    public ResponseEntity<byte[]> excel(@RequestParam String tipo,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] archivo = servicio.generarExcel(tipo, desde, hasta);
        /* "tipo" ya fue validado contra una lista fija, por eso es seguro usarlo en el nombre del archivo */
        ContentDisposition descarga = ContentDisposition.attachment().filename("informe-" + tipo.toLowerCase() + ".xlsx").build();
        return ResponseEntity.ok()
            .contentType(XLSX)
            .header(HttpHeaders.CONTENT_DISPOSITION, descarga.toString())
            .body(archivo);
    }
}
