package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración del contexto completo. Necesita MySQL en ejecución con las credenciales
 * del perfil "local", por eso está desactivada en la compilación automática (mvn test).
 * Para correrla: quitar @Disabled con MySQL encendido.
 */
@Disabled("Requiere una base MySQL en ejecución")
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
