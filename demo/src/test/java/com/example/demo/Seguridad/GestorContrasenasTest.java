package com.example.demo.Seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class GestorContrasenasTest {

    private final GestorContrasenas gestor = new GestorContrasenas(new BCryptPasswordEncoder(4));

    @Test
    void cifrarGeneraUnHashBcryptQueCoincideConLaOriginal() {
        String hash = gestor.cifrar("clave123");

        assertThat(hash).isNotEqualTo("clave123");
        assertThat(gestor.estaCifrada(hash)).isTrue();
        assertThat(gestor.coincide("clave123", hash)).isTrue();
        assertThat(gestor.coincide("otra", hash)).isFalse();
    }

    @Test
    void dosCifradosDeLaMismaClaveSonDistintosPorLaSal() {
        assertThat(gestor.cifrar("clave123")).isNotEqualTo(gestor.cifrar("clave123"));
    }

    @Test
    void aceptaContrasenasAntiguasEnTextoPlanoParaMigrarlas() {
        assertThat(gestor.estaCifrada("1234")).isFalse();
        assertThat(gestor.coincide("1234", "1234")).isTrue();
        assertThat(gestor.coincide("9999", "1234")).isFalse();
    }

    @Test
    void valoresNulosNuncaCoinciden() {
        assertThat(gestor.coincide(null, "1234")).isFalse();
        assertThat(gestor.coincide("1234", null)).isFalse();
    }

    @Test
    void laContrasenaTemporalTiene8CaracteresPermitidos() {
        String temporal = gestor.generarTemporal();

        assertThat(temporal).hasSize(8).matches("[a-km-z2-9]{8}");
    }
}
