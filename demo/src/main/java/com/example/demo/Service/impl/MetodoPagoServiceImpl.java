package com.example.demo.Service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Dao.MetodoPagoDao;
import com.example.demo.Model.MetodoPago;
import com.example.demo.Service.MetodoPagoService;

@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    /* Una imagen de QR en base64 no debe pasar de ~3 MB de texto */
    private static final int MAX_QR = 3_000_000;

    private final MetodoPagoDao dao;

    public MetodoPagoServiceImpl(MetodoPagoDao dao) {
        this.dao = dao;
    }

    @Override
    public List<MetodoPago> listar() {
        return dao.listar();
    }

    @Override
    @Transactional
    public MetodoPago guardar(String tipoRecibido, Map<String, Object> datos) {
        String tipo = tipoRecibido == null ? "" : tipoRecibido.trim().toUpperCase();
        if (!tipo.equals("YAPE") && !tipo.equals("TRANSFERENCIA"))
            throw new IllegalArgumentException("El método de pago debe ser YAPE o TRANSFERENCIA");

        MetodoPago m = dao.buscarPorTipo(tipo).orElseGet(() -> {
            MetodoPago nuevo = new MetodoPago();
            nuevo.setTipo(tipo);
            return nuevo;
        });

        String titular = texto(datos.get("titular"));
        String numero = texto(datos.get("numero"));
        if (titular.isEmpty())
            throw new IllegalArgumentException("El titular es obligatorio");
        if (!numero.matches("\\d+"))
            throw new IllegalArgumentException("El número solo puede contener dígitos");

        if (tipo.equals("YAPE")) {
            if (!numero.matches("9\\d{8}"))
                throw new IllegalArgumentException("El número de Yape debe tener exactamente 9 dígitos y empezar con 9");
            String qr = texto(datos.get("qr"));
            if (!qr.isEmpty()) {
                if (!qr.startsWith("data:image/"))
                    throw new IllegalArgumentException("El QR debe ser una imagen válida");
                if (qr.length() > MAX_QR)
                    throw new IllegalArgumentException("La imagen del QR es demasiado pesada");
                m.setQr(qr);
            } else if (Boolean.TRUE.equals(datos.get("quitarQr"))) {
                m.setQr(null);
            }
        } else {
            String banco = texto(datos.get("banco"));
            String cci = texto(datos.get("cci"));
            if (banco.isEmpty())
                throw new IllegalArgumentException("El banco es obligatorio");
            int[] largos = largosCuenta(banco);
            if (largos.length > 0 && java.util.Arrays.stream(largos).noneMatch(l -> l == numero.length()))
                throw new IllegalArgumentException("El número de cuenta de " + banco + " debe tener exactamente "
                    + java.util.Arrays.stream(largos).mapToObj(String::valueOf).collect(java.util.stream.Collectors.joining(" o ")) + " dígitos");
            if (largos.length == 0 && !numero.matches("\\d{8,20}"))
                throw new IllegalArgumentException("El número de cuenta debe tener entre 8 y 20 dígitos");
            if (!cci.matches("\\d{20}"))
                throw new IllegalArgumentException("El CCI debe tener exactamente 20 dígitos");
            m.setBanco(banco);
            m.setCci(cci);
        }

        m.setTitular(titular.isEmpty() ? null : titular);
        m.setNumero(numero);
        String quien = texto(datos.get("modificadoPor"));
        m.setModificadoPor(quien.isEmpty() ? "Administrador" : quien);
        m.setModificado(LocalDateTime.now());
        return dao.guardar(m);
    }

    private static String texto(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    /* Dígitos de la cuenta según el banco (sin guiones). Banco no listado: se acepta de 8 a 20 dígitos */
    private static int[] largosCuenta(String banco) {
        String b = banco.trim().toLowerCase();
        if (b.equals("bcp") || b.contains("crédito del perú") || b.contains("credito del peru")) return new int[] { 13, 14 };
        if (b.equals("interbank")) return new int[] { 13 };
        if (b.equals("scotiabank")) return new int[] { 10 };
        if (b.equals("bbva")) return new int[] { 18, 20 };
        if (b.equals("banbif")) return new int[] { 10 };
        return new int[0];
    }
}
