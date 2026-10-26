package com.example.demo.Seguridad;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Protección contra fuerza bruta: cuenta los intentos fallidos por usuario en una caché de
 * Guava que olvida cada registro pasado el tiempo de bloqueo configurado.
 */
@Component
public class LimitadorIntentos {

    private final int maximo;
    private final Cache<String, AtomicInteger> fallos;

    public LimitadorIntentos(@Value("${terno.seguridad.max-intentos:5}") int maximo,
                             @Value("${terno.seguridad.minutos-bloqueo:5}") long minutosBloqueo) {
        this.maximo = maximo;
        this.fallos = CacheBuilder.newBuilder()
            .expireAfterWrite(minutosBloqueo, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();
    }

    /** @return {@code true} si el usuario ya alcanzó el máximo de intentos fallidos */
    public boolean bloqueado(String usuario) {
        AtomicInteger cuenta = fallos.getIfPresent(clave(usuario));
        return cuenta != null && cuenta.get() >= maximo;
    }

    public void registrarFallo(String usuario) {
        fallos.asMap().computeIfAbsent(clave(usuario), k -> new AtomicInteger()).incrementAndGet();
    }

    public void reiniciar(String usuario) {
        fallos.invalidate(clave(usuario));
    }

    private String clave(String usuario) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(usuario));
    }
}
