package com.example.demo.Model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/* Cada vez que el empleado califica a un cliente al terminar una boleta se guarda un registro;
   la calificación del cliente es el promedio de todos sus registros */
@Entity
@Getter
@Setter
public class Calificacion extends Base {

    @NotNull(message = "Debe seleccionar un cliente")
    @ManyToOne
    @JoinColumn(name = "idCliente")
    private Cliente cliente;

    @Min(value = 1, message = "La calificación mínima es 1 estrella")
    @Max(value = 5, message = "La calificación máxima es 5 estrellas")
    private int estrellas;

    @Size(max = 255, message = "El comentario no debe superar 255 caracteres")
    private String comentario;

    @NotNull(message = "La fecha de la calificación es obligatoria")
    private LocalDate fecha = LocalDate.now();
}
