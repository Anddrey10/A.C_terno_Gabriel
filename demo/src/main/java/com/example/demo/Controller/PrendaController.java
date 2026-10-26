package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.demo.Model.Prenda;
import com.example.demo.Service.PrendaService;

@RestController
@RequestMapping("/api/prendas")
public class PrendaController {

    private final PrendaService servicio;

    public PrendaController(PrendaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Prenda> traerPrendas() {
        return servicio.listar();
    }

    /* Siguiente correlativo para un prefijo de 3 letras (TRN-005) */
    @GetMapping("siguiente-codigo")
    public Map<String, String> siguienteCodigo(@RequestParam String prefijo) {
        return Map.of("codigo", servicio.siguienteCodigo(prefijo));
    }

    @PostMapping("nuevo")
    public Prenda guardarPrenda(@Valid @RequestBody Prenda nuevo) {
        return servicio.crear(nuevo);
    }

    @PutMapping("actualizar/{id}")
    public Prenda editarPrenda(@PathVariable Long id, @Valid @RequestBody Prenda datos) {
        return servicio.actualizar(id, datos);
    }

    @PutMapping("estado/{id}")
    public Prenda cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return servicio.cambiarEstado(id, estado);
    }

    @PutMapping("suspender/{id}")
    public Prenda suspender(@PathVariable Long id) {
        return servicio.suspender(id);
    }

    @PutMapping("reactivar/{id}")
    public Prenda reactivar(@PathVariable Long id) {
        return servicio.reactivar(id);
    }

    @DeleteMapping("eliminar/{id}")
    public Map<String, String> eliminarPrenda(@PathVariable Long id) {
        servicio.eliminar(id);
        return Map.of("mensaje", "PRENDA ELIMINADO");
    }
}
