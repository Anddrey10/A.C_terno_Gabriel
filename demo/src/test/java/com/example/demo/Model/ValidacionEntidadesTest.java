package com.example.demo.Model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/* Comprueba las anotaciones de validación (Bean Validation) de las entidades: un objeto correcto no produce
   errores y cada dato inválido devuelve el mensaje personalizado que definimos en la entidad */
class ValidacionEntidadesTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrar() {
        fabrica.close();
    }

    private static Set<String> mensajes(Object objeto) {
        return validador.validate(objeto).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    /* ---------- Calificacion ---------- */

    private Calificacion calificacionValida() {
        Calificacion c = new Calificacion();
        c.setCliente(new Cliente());
        c.setEstrellas(4);
        c.setComentario("Devolvió a tiempo");
        return c;
    }

    @Test
    void calificacionCorrectaNoTieneErrores() {
        assertThat(mensajes(calificacionValida())).isEmpty();
    }

    @Test
    void calificacionConEstrellasFueraDeRangoMuestraSuMensaje() {
        Calificacion c = calificacionValida();
        c.setEstrellas(0);
        assertThat(mensajes(c)).containsExactly("La calificación mínima es 1 estrella");
        c.setEstrellas(6);
        assertThat(mensajes(c)).containsExactly("La calificación máxima es 5 estrellas");
    }

    @Test
    void calificacionSinClienteEsInvalida() {
        Calificacion c = calificacionValida();
        c.setCliente(null);
        assertThat(mensajes(c)).containsExactly("Debe seleccionar un cliente");
    }

    /* ---------- Configuracion ---------- */

    @Test
    void configuracionRechazaMontoNegativoYClaveDesconocida() {
        Configuracion c = new Configuracion();
        c.setClave("penalidadDemora");
        c.setValor(10.0);
        assertThat(mensajes(c)).isEmpty();

        c.setValor(-5.0);
        assertThat(mensajes(c)).containsExactly("El monto debe ser mayor a 0");

        c.setValor(10.0);
        c.setClave("otraClave");
        assertThat(mensajes(c)).containsExactly("La clave debe ser penalidadDemora o penalidadMalEstado");
    }

    /* ---------- MetodoPago ---------- */

    private MetodoPago yapeValido() {
        MetodoPago m = new MetodoPago();
        m.setTipo("YAPE");
        m.setTitular("Ana Torres");
        m.setNumero("987654321");
        return m;
    }

    @Test
    void metodoPagoCorrectoNoTieneErrores() {
        assertThat(mensajes(yapeValido())).isEmpty();
    }

    @Test
    void metodoPagoRechazaNumeroCortoYCciIncorrecto() {
        MetodoPago m = yapeValido();
        m.setNumero("1234");
        assertThat(mensajes(m)).containsExactly("El número debe tener entre 8 y 20 dígitos");

        m = yapeValido();
        m.setCci("123");
        assertThat(mensajes(m)).containsExactly("El CCI debe tener exactamente 20 dígitos");
    }

    @Test
    void metodoPagoSinTitularEsInvalido() {
        MetodoPago m = yapeValido();
        m.setTitular(" ");
        assertThat(mensajes(m)).containsExactly("El titular es obligatorio");
    }

    /* ---------- Boleta ---------- */

    private Boleta boletaValida() {
        Boleta b = new Boleta();
        b.setTipo("Venta");
        b.setCliente(new Cliente());
        b.setMonto(250.0);
        return b;
    }

    @Test
    void boletaSinNumeroEsValidaPorqueLoGeneraElSistema() {
        assertThat(mensajes(boletaValida())).isEmpty();
    }

    @Test
    void boletaRechazaMetodoDePagoYNumeroConFormatoIncorrecto() {
        Boleta b = boletaValida();
        b.setMetodoPago("Tarjeta");
        b.setNumero("123");
        assertThat(mensajes(b)).containsExactlyInAnyOrder(
            "El método de pago debe ser Efectivo, Yape o Transferencia",
            "El número de boleta debe tener el formato B001-0001");
    }

    @Test
    void boletaAceptaNumeroGeneradoPorElSistema() {
        Boleta b = boletaValida();
        b.setNumero("B001-0012");
        b.setMetodoPago("Yape");
        assertThat(mensajes(b)).isEmpty();
    }

    /* ---------- Confeccion ---------- */

    private Confeccion confeccionValida() {
        Confeccion c = new Confeccion();
        c.setCliente(new Cliente());
        c.setCostoTotal(500.0);
        c.setAdelanto(100.0);
        c.setEntregaFinal(LocalDate.now().plusDays(10));
        c.setDetalle("{\"medidas\":{\"pecho\":\"90\"}}");
        return c;
    }

    @Test
    void confeccionCorrectaNoTieneErrores() {
        assertThat(mensajes(confeccionValida())).isEmpty();
    }

    @Test
    void confeccionSinFichaDeMedidasEsInvalida() {
        Confeccion c = confeccionValida();
        c.setDetalle("");
        assertThat(mensajes(c)).containsExactly("La ficha de medidas es obligatoria");
    }

    @Test
    void confeccionRechazaAdelantoNegativoYTicketIncorrecto() {
        Confeccion c = confeccionValida();
        c.setAdelanto(-1.0);
        c.setTicket("X1");
        assertThat(mensajes(c)).containsExactlyInAnyOrder(
            "El adelanto no puede ser negativo",
            "El ticket debe tener el formato T001-0001");
    }

    /* ---------- Penalidad ---------- */

    @Test
    void penalidadRechazaMontosNegativos() {
        Penalidad p = new Penalidad();
        p.setCliente(new Cliente());
        p.setTipo("Demora");
        p.setMonto(20.0);
        assertThat(mensajes(p)).isEmpty();

        p.setMontoDemora(-1.0);
        p.setMontoMalEstado(-2.0);
        assertThat(mensajes(p)).containsExactlyInAnyOrder(
            "El monto por demora no puede ser negativo",
            "El monto por mal estado no puede ser negativo");
    }

    /* ---------- Prenda ---------- */

    private Prenda prendaValida() {
        Prenda p = new Prenda();
        p.setCodigo("TRN-001");
        p.setDiseno("Clásico");
        p.setTipo("Nuevo");
        p.setTalla("M");
        p.setPrecioVenta(300.0);
        p.setPrecioAlquiler(80.0);
        p.setCategoria("Adultos");
        return p;
    }

    @Test
    void prendaCorrectaNoTieneErrores() {
        assertThat(mensajes(prendaValida())).isEmpty();
    }

    @Test
    void prendaRechazaCategoriaDesconocida() {
        Prenda p = prendaValida();
        p.setCategoria("Mascotas");
        assertThat(mensajes(p)).containsExactly("La categoría debe ser Niños, Jóvenes o Adultos");
    }

    /* ---------- Cliente y Empleado ---------- */

    @Test
    void clienteRechazaComentarioDemasiadoLargo() {
        Cliente c = new Cliente();
        c.setNombres("Luis");
        c.setApellidos("García Toledo");
        c.setDni("12345678");
        c.setCelular("987654321");
        c.setCorreo("luis@gmail.com");
        c.setDireccion("Trujillo");
        assertThat(mensajes(c)).isEmpty();

        c.setComentario("x".repeat(256));
        assertThat(mensajes(c)).containsExactly("El comentario no debe superar 255 caracteres");
    }

    @Test
    void empleadoRechazaUsuarioMuyLargoOCorto() {
        Empleado e = new Empleado();
        e.setNombre("Jefferson");
        e.setApellido("Encarnacion");
        e.setUsuario("admin");
        e.setCorreo("admin@gmail.com");
        e.setTelefono("974673845");
        e.setArea("Administración");
        assertThat(mensajes(e)).isEmpty();

        e.setUsuario("abc");
        assertThat(mensajes(e)).containsExactly("El usuario debe tener entre 4 y 50 caracteres");
        e.setUsuario("u".repeat(51));
        assertThat(mensajes(e)).containsExactly("El usuario debe tener entre 4 y 50 caracteres");
    }
}
