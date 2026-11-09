package com.example.demo.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.demo.Model.Confeccion;
import com.example.demo.Service.ConfeccionService;

@RestController
@RequestMapping("/api/confecciones")
public class ConfeccionController {

    private final ConfeccionService servicio;

    public ConfeccionController(ConfeccionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Confeccion> traerConfecciones() {
        return servicio.listar();
    }

    /* Registrar la orden emite el TICKET (T001-0001) */
    @PostMapping("nuevo")
    public Confeccion guardarConfeccion(@Valid @RequestBody Confeccion nueva) {
        return servicio.registrar(nueva);
    }

    /* Prueba de confección: guarda el comentario y la fecha de entrega (que se puede ampliar) */
    @PutMapping("prueba/{id}")
    public Confeccion registrarPrueba(@PathVariable Long id, @RequestBody Map<String, String> datos) {
        String fecha = datos.get("entregaFinal");
        LocalDate entrega;
        try {
            entrega = fecha == null ? null : LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha de entrega no es válida");
        }
        return servicio.registrarPrueba(id, datos.get("comentario"), entrega);
    }
}
