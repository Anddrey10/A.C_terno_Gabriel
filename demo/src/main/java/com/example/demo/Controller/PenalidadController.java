package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.demo.Model.Penalidad;
import com.example.demo.Service.PenalidadService;

@RestController
@RequestMapping("/api/penalidades")
public class PenalidadController {

    private final PenalidadService servicio;

    public PenalidadController(PenalidadService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Penalidad> traerPenalidads() {
        return servicio.listar();
    }

    @PostMapping("nuevo")
    public Penalidad guardarPenalidad(@Valid @RequestBody Penalidad nuevo) {
        return servicio.crear(nuevo);
    }

    @PutMapping("actualizar/{id}")
    public Penalidad editarPenalidad(@PathVariable Long id, @Valid @RequestBody Penalidad datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("eliminar/{id}")
    public Map<String, String> eliminarPenalidad(@PathVariable Long id) {
        servicio.eliminar(id);
        return Map.of("mensaje", "PENALIDAD ELIMINADO");
    }
}
