package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.demo.Model.Boleta;
import com.example.demo.Service.BoletaService;

@RestController
@RequestMapping("/api/boletas")
public class BoletaController {

    private final BoletaService servicio;

    public BoletaController(BoletaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Boleta> traerBoletas() {
        return servicio.listar();
    }

    @PostMapping("nuevo")
    public Boleta guardarBoleta(@Valid @RequestBody Boleta nueva) {
        return servicio.emitir(nueva);
    }

    @PutMapping("devolver/{id}")
    public Boleta devolverBoleta(@PathVariable Long id,
                                 @RequestParam(defaultValue = "En lavandería") String estadoPrenda) {
        return servicio.devolver(id, estadoPrenda);
    }

    @PutMapping("anular/{id}")
    public Boleta anularBoleta(@PathVariable Long id, @RequestParam String motivo) {
        return servicio.anular(id, motivo);
    }

    @PutMapping("actualizar/{id}")
    public Boleta editarBoleta(@PathVariable Long id, @Valid @RequestBody Boleta datos) {
        return servicio.actualizar(id, datos);
    }

    @DeleteMapping("eliminar/{id}")
    public Map<String, String> eliminarBoleta(@PathVariable Long id) {
        servicio.eliminar(id);
        return Map.of("mensaje", "BOLETA ELIMINADO");
    }
}
