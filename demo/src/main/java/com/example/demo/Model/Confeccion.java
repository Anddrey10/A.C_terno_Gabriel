package com.example.demo.Model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/* Orden de confección: al registrarla se emite un TICKET (T001-0001); al entregarla y pagar el saldo se emite la BOLETA */
@Entity
@Getter
@Setter
public class Confeccion extends Base {

    /* Lo genera el sistema al registrar la orden (T001-0001); por eso no es obligatorio al recibir la solicitud */
    @Pattern(regexp = "T\\d{3}-\\d{4,}", message = "El ticket debe tener el formato T001-0001")
    @Column(unique = true)
    private String ticket;

    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    @NotNull(message = "El costo total es obligatorio")
    @Positive(message = "El costo total debe ser mayor a 0")
    private Double costoTotal;

    @NotNull(message = "El adelanto es obligatorio")
    @PositiveOrZero(message = "El adelanto no puede ser negativo")
    private Double adelanto;

    private LocalDate fecha = LocalDate.now();

    private LocalDate primeraPrueba;

    @NotNull(message = "La fecha de entrega final es obligatoria")
    private LocalDate entregaFinal;

    /* Compatibilidad con el esquema MySQL existente, que también exige fechaEntrega. */
    @Column(name = "fechaEntrega")
    private LocalDate fechaEntrega;

    /* En confección | Entregada */
    @NotBlank(message = "El estado de la orden es obligatorio")
    @Pattern(regexp = "En confección|Entregada", message = "El estado debe ser En confección o Entregada")
    private String estado = "En confección";

    @Pattern(regexp = "Efectivo|Yape|Transferencia", message = "El método de pago debe ser Efectivo, Yape o Transferencia")
    private String metodoPago;

    /* Texto legado para mantener compatibilidad con la interfaz. */
    @Size(max = 255, message = "El nombre del empleado no debe superar 255 caracteres")
    private String empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEmpleado")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Empleado empleadoResponsable;

    /* Color principal de la confección (el controlador siempre guarda un valor) */
    @Size(max = 255, message = "El color no debe superar 255 caracteres")
    @Column(name = "color", length = 255)
    private String color = "No especificado";

    /* Diseño, telas, colores y medidas (JSON). TEXT para que nunca se corte, aunque la ficha sea larga */
    @NotBlank(message = "La ficha de medidas es obligatoria")
    @Column(columnDefinition = "TEXT")
    private String detalle;

    /* Número de la boleta emitida al entregar */
    @Pattern(regexp = "B\\d{3}-\\d{4,}", message = "El número de boleta debe tener el formato B001-0001")
    private String boleta;

    /* Cuántas pruebas de confección se han registrado; con 1 o más, la pantalla propone "Entrega" por defecto */
    @PositiveOrZero(message = "La cantidad de pruebas no puede ser negativa")
    private Integer pruebas = 0;

    /* Comentarios de las pruebas (qué se puede mejorar), uno por línea con su fecha */
    @Column(columnDefinition = "TEXT")
    private String comentarioPrueba;
}
