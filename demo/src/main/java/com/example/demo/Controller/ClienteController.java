package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.demo.Model.Cliente;
import com.example.demo.Service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService servicio;

    public ClienteController(ClienteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Cliente> traerClientes() {
        return servicio.listar();
    }

    @PostMapping("nuevo")
    public Cliente guardarCliente(@Valid @RequestBody Cliente nuevo) {
        return servicio.crear(nuevo);
    }

    @PutMapping("actualizar/{id}")
    public Cliente editarCliente(@PathVariable Long id, @Valid @RequestBody Cliente datos) {
        return servicio.actualizar(id, datos);
    }

    /* El empleado califica al cliente al terminar la boleta o la devolución; el administrador lo ve en Calificaciones */
    @PutMapping("calificar/{id}")
    public Cliente calificar(@PathVariable Long id, @RequestBody Map<String, Object> datos) {
        return servicio.calificar(id, datos.get("calificacion"), datos.get("comentario"));
    }

    @DeleteMapping("eliminar/{id}")
    public Map<String, String> eliminarCliente(@PathVariable Long id) {
        servicio.eliminar(id);
        return Map.of("mensaje", "CLIENTE ELIMINADO");
    }
}
