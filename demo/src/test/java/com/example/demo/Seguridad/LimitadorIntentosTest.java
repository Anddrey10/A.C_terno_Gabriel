package com.example.demo.Seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class LimitadorIntentosTest {

    @Test
    void bloqueaAlLlegarAlMaximoDeFallos() {
        LimitadorIntentos limitador = new LimitadorIntentos(3, 5);

        limitador.registrarFallo("Carlos");
        limitador.registrarFallo("carlos ");
        assertThat(limitador.bloqueado("carlos")).isFalse();

        limitador.registrarFallo("CARLOS");
        assertThat(limitador.bloqueado("carlos")).isTrue();
    }

    @Test
    void reiniciarDesbloqueaAlUsuario() {
        LimitadorIntentos limitador = new LimitadorIntentos(1, 5);
        limitador.registrarFallo("ana");
        assertThat(limitador.bloqueado("ana")).isTrue();

        limitador.reiniciar("ana");

        assertThat(limitador.bloqueado("ana")).isFalse();
    }

    @Test
    void elBloqueoDeUnUsuarioNoAfectaAOtro() {
        LimitadorIntentos limitador = new LimitadorIntentos(1, 5);
        limitador.registrarFallo("ana");

        assertThat(limitador.bloqueado("luis")).isFalse();
    }
}
