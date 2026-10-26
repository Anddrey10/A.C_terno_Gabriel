package com.example.demo.Model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/* Valores que define el administrador y usa el empleado (por ejemplo, el monto de las penalizaciones) */
@Entity
@Getter
@Setter
public class Configuracion extends Base {

    @Column(unique = true)
    private String clave;

    private Double valor;

    /* Se conserva modificadoPor como texto para no alterar el JSON existente. */
    private String modificadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEmpleado")
    @JsonIgnore
    private Empleado empleadoResponsable;

    private LocalDateTime modificado = LocalDateTime.now();
}
