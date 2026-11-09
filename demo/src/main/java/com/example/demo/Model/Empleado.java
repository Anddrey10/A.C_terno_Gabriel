package com.example.demo.Model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Empleado extends Base {

    /* Código único de 4 caracteres (letras mayúsculas y números), por ejemplo E001; lo genera el sistema */
    @Pattern(regexp = "[A-Z0-9]{4}", message = "El código debe tener 4 caracteres (letras mayúsculas y números)")
    @Column(unique = true, length = 4)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe superar 100 caracteres")
    private String nombre;

    /* Se escribe justo después del nombre; en la tabla se muestra junto al nombre (columna NOMBRE).
       Es obligatorio al crear o editar (lo exige el servicio); no lleva @NotBlank para que los empleados antiguos,
       que todavía no tienen apellido, se puedan suspender o restablecer sin error. */
    @Size(max = 100, message = "El apellido es demasiado largo")
    private String apellido;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres")
    @Column(unique = true)
    private String usuario;

    /* Seguridad: se guarda cifrada (BCrypt) y nunca se devuelve en las respuestas JSON */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    /* Obligatoria al crear (lo exige el servicio); al editar es opcional: si no llega se conserva la actual */
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String contrasena;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no debe superar 100 caracteres")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9")
    private String telefono;

    /* Área o puesto de trabajo */
    @NotBlank(message = "El área o puesto es obligatorio")
    @Size(max = 100, message = "El área o puesto no debe superar 100 caracteres")
    private String area;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "Empleado|Administrador", message = "El rol debe ser Empleado o Administrador")
    private String rol = "Empleado";

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "Activo|Inactivo|Suspendido", message = "El estado debe ser Activo, Inactivo o Suspendido")
    private String estado = "Activo";

    private LocalDate desde = LocalDate.now();
}
