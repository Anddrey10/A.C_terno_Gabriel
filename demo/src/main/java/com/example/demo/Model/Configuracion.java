package com.example.demo.Model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/* Valores que define el administrador y usa el empleado (por ejemplo, el monto de las penalizaciones) */
@Entity
@Getter
@Setter
public class Configuracion extends Base {

    @NotBlank(message = "La clave de la configuración es obligatoria")
    @Pattern(regexp = "penalidadDemora|penalidadMalEstado", message = "La clave debe ser penalidadDemora o penalidadMalEstado")
    @Column(unique = true)
    private String clave;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double valor;

    /* Se conserva modificadoPor como texto para no alterar el JSON existente. */
    @Size(max = 255, message = "El nombre de quien modificó no debe superar 255 caracteres")
    private String modificadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEmpleado")
    @JsonIgnore
    private Empleado empleadoResponsable;

    @NotNull(message = "La fecha de modificación es obligatoria")
    private LocalDateTime modificado = LocalDateTime.now();
}
