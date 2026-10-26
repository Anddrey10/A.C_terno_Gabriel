package com.example.demo.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import com.example.demo.Model.Configuracion;
import com.example.demo.Service.ConfiguracionService;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService servicio;

    public ConfiguracionController(ConfiguracionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Configuracion> traerConfiguracion() {
        return servicio.listar();
    }

    /* El administrador cambia un valor (penalidadDemora, penalidadMalEstado) y el empleado lo usa al registrar devoluciones */
    @PutMapping("{clave}")
    public Configuracion cambiar(@PathVariable String clave, @RequestBody Map<String, Object> datos) {
        return servicio.cambiar(clave, datos.get("valor"), datos.get("modificadoPor"));
    }
}
