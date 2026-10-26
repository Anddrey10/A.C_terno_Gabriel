package com.example.demo.Service.impl;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.demo.Dao.EmpleadoDao;
import com.example.demo.Model.Empleado;
import com.example.demo.Seguridad.GestorContrasenas;
import com.example.demo.Seguridad.IntentosExcedidosException;
import com.example.demo.Seguridad.LimitadorIntentos;
import com.example.demo.Service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final EmpleadoDao empleados;
    private final GestorContrasenas contrasenas;
    private final LimitadorIntentos limitador;

    public AuthServiceImpl(EmpleadoDao empleados, GestorContrasenas contrasenas, LimitadorIntentos limitador) {
        this.empleados = empleados;
        this.contrasenas = contrasenas;
        this.limitador = limitador;
    }

    @Override
    public Optional<Empleado> autenticar(String usuario, String contrasena) {
        String nombreUsuario = StringUtils.trimToEmpty(usuario);
        if (limitador.bloqueado(nombreUsuario)) {
            LOG.warn("Inicio de sesión bloqueado temporalmente para el usuario '{}'", paraLog(nombreUsuario));
            throw new IntentosExcedidosException("Demasiados intentos fallidos. Espere unos minutos e intente de nuevo.");
        }

        Optional<Empleado> encontrado = empleados.buscarPorUsuario(nombreUsuario)
            .filter(e -> e.getUsuario().equals(nombreUsuario))
            .filter(e -> "Activo".equals(e.getEstado()))
            .filter(e -> contrasenas.coincide(contrasena, e.getContrasena()));

        if (encontrado.isEmpty()) {
            limitador.registrarFallo(nombreUsuario);
            LOG.warn("Inicio de sesión fallido para el usuario '{}'", paraLog(nombreUsuario));
            return Optional.empty();
        }

        limitador.reiniciar(nombreUsuario);
        Empleado empleado = encontrado.get();
        migrarSiEstabaEnTextoPlano(empleado, contrasena);
        LOG.info("Inicio de sesión correcto: usuario '{}' ({})", paraLog(nombreUsuario), empleado.getRol());
        return encontrado;
    }

    /* Una contraseña antigua en texto plano se cifra la primera vez que el empleado ingresa correctamente */
    private void migrarSiEstabaEnTextoPlano(Empleado empleado, String contrasenaEscrita) {
        if (contrasenas.estaCifrada(empleado.getContrasena())) return;
        try {
            empleado.setContrasena(contrasenas.cifrar(contrasenaEscrita));
            empleados.guardar(empleado);
        } catch (RuntimeException e) {
            LOG.warn("No se pudo cifrar la contraseña del usuario '{}': {}", paraLog(empleado.getUsuario()), e.getMessage());
        }
    }

    /* Evita la inyección de saltos de línea en los registros (log forging) */
    private String paraLog(String texto) {
        return StringUtils.abbreviate(StringUtils.replaceChars(StringUtils.defaultString(texto), "\r\n\t", "___"), 40);
    }
}
