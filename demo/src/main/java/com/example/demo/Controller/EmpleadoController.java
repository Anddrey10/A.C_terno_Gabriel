package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.demo.Model.Empleado;
import com.example.demo.Service.EmpleadoService;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService servicio;

    public EmpleadoController(EmpleadoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Empleado> traerEmpleados() {
        return servicio.listar();
    }

    @PostMapping("nuevo")
    public Empleado guardarEmpleado(@Valid @RequestBody Empleado nuevo) {
        return servicio.crear(nuevo);
    }

    @PutMapping("actualizar/{id}")
    public Empleado editarEmpleado(@PathVariable Long id, @Valid @RequestBody Empleado datos) {
        return servicio.actualizar(id, datos);
    }

    @PutMapping("suspender/{id}")
    public Empleado suspender(@PathVariable Long id) {
        return servicio.suspender(id);
    }

    /* Devuelve al empleado suspendido a la lista principal */
    @PutMapping("reactivar/{id}")
    public Empleado reactivar(@PathVariable Long id) {
        return servicio.reactivar(id);
    }

    /* Genera una contraseña temporal y se la muestra al administrador para que se la entregue al empleado */
    @PutMapping("restablecer/{id}")
    public Map<String, String> restablecerContrasena(@PathVariable Long id) {
        return Map.of("mensaje", "Contraseña restablecida", "contrasena", servicio.restablecerContrasena(id));
    }

    @DeleteMapping("eliminar/{id}")
    public Map<String, String> eliminarEmpleado(@PathVariable Long id) {
        servicio.eliminar(id);
        return Map.of("mensaje", "EMPLEADO ELIMINADO");
    }
}
