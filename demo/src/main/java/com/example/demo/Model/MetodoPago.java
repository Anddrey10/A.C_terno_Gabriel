package com.example.demo.Model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/*
 * Datos de cobro que define el administrador y que el empleado muestra al cliente.
 * Hay un registro por tipo: YAPE (QR + número de celular) y TRANSFERENCIA (banco, cuenta, CCI y titular).
 */
@Entity
@Getter
@Setter
public class MetodoPago extends Base {

    /* YAPE o TRANSFERENCIA */
    @NotBlank(message = "El tipo de método de pago es obligatorio")
    @Pattern(regexp = "YAPE|TRANSFERENCIA", message = "El método de pago debe ser YAPE o TRANSFERENCIA")
    @Column(unique = true, nullable = false, length = 20)
    private String tipo;

    /* Nombre del titular de la cuenta o del Yape */
    @NotBlank(message = "El titular es obligatorio")
    @Size(max = 100, message = "El titular no debe superar 100 caracteres")
    private String titular;

    /* Yape: número de celular. Transferencia: número de cuenta */
    @NotBlank(message = "El número es obligatorio")
    @Pattern(regexp = "\\d{8,20}", message = "El número debe tener entre 8 y 20 dígitos")
    private String numero;

    /* Solo transferencia */
    @Size(max = 60, message = "El nombre del banco no debe superar 60 caracteres")
    private String banco;

    /* Código de cuenta interbancario (20 dígitos), solo transferencia */
    @Pattern(regexp = "(\\d{20})?", message = "El CCI debe tener exactamente 20 dígitos")
    private String cci;

    /* Solo Yape: imagen del QR como data URL (la reduce el navegador antes de enviarla) */
    @Size(max = 3_000_000, message = "La imagen del QR es demasiado pesada")
    @Column(columnDefinition = "LONGTEXT")
    private String qr;

    @Size(max = 255, message = "El nombre de quien modificó no debe superar 255 caracteres")
    private String modificadoPor;

    @NotNull(message = "La fecha de modificación es obligatoria")
    private LocalDateTime modificado = LocalDateTime.now();
}
