package com.example.demo.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rutas públicas: no exigen iniciar sesión (/public/**). */
@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/home")
    public String home() {
        return "Vista Pública de Marcos Web";
    }
}
