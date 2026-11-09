package com.example.demo.Model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Boleta extends Base {

    /* Lo genera el sistema al emitir (B001-0001); por eso no es obligatorio al recibir la solicitud */
    @Pattern(regexp = "B\\d{3}-\\d{4,}", message = "El número de boleta debe tener el formato B001-0001")
    @Column(unique = true)
    private String numero;

    @NotBlank(message = "El tipo de operación es obligatorio")
    @Pattern(regexp = "Venta|Alquiler|Reserva|Confección", message = "El tipo debe ser Venta, Alquiler, Reserva o Confección")
    private String tipo;

    /* Muchas boletas pertenecen a un cliente */
    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    /* Muchas boletas pueden referirse a una misma prenda (en distintas fechas); la boleta de confección no tiene prenda */
    @ManyToOne
    @JoinColumn(name = "idPrenda")
    private Prenda prenda;

    @NotNull(message = "La fecha de la boleta es obligatoria")
    private LocalDate fecha = LocalDate.now();

    /* La fecha no puede ser pasada al crear la boleta (se valida en el controlador, no aquí, para poder devolver con retraso) */
    private LocalDate fechaDevolucion;

    /* Solo alquileres: fecha de recojo; con ella y la de devolución se calculan los días del alquiler */
    private LocalDate fechaRecojo;

    /* Solo alquileres: si el empleado lo marca, los días 1 y 2 se cobran como un solo día */
    private Boolean cobrarDosDiasComoUno = Boolean.FALSE;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;

    @NotBlank(message = "El estado de la boleta es obligatorio")
    @Pattern(regexp = "Vigente|Devuelta|Anulada", message = "El estado debe ser Vigente, Devuelta o Anulada")
    private String estado = "Vigente";

    /* Nombre visible conservado para no cambiar el JSON que consume el frontend. */
    @Size(max = 255, message = "El nombre del empleado no debe superar 255 caracteres")
    private String empleado;

    /* Relación real con el empleado que emitió la boleta. Se oculta en JSON para evitar ciclos
       y mantener exactamente la respuesta que ya utiliza la interfaz. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEmpleado")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Empleado empleadoResponsable;

    @Pattern(regexp = "Efectivo|Yape|Transferencia", message = "El método de pago debe ser Efectivo, Yape o Transferencia")
    private String metodoPago;

    /* Solo para boletas de confección: la orden (ticket) que se está entregando */
    @ManyToOne
    @JoinColumn(name = "idConfeccion")
    private Confeccion confeccion;

    @Size(max = 255, message = "El motivo de anulación no debe superar 255 caracteres")
    private String motivoAnulacion;
}
