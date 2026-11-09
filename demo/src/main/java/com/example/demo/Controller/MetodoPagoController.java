package com.example.demo.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Model.MetodoPago;
import com.example.demo.Service.MetodoPagoService;

/* El administrador edita los datos de cobro; el empleado los consulta al cobrar con Yape o transferencia */
@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService servicio;

    public MetodoPagoController(MetodoPagoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<MetodoPago> listar() {
        return servicio.listar();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("{tipo}")
    public MetodoPago guardar(@PathVariable String tipo, @RequestBody Map<String, Object> datos) {
        return servicio.guardar(tipo, datos);
    }
}
