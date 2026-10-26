package com.example.demo.Controller;

import java.util.List;
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
}
