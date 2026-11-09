package com.example.demo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.demo.Model.Empleado;
import com.example.demo.Service.EmpleadoService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

/*
 * Arranque de la base de datos:
 *  1) Repara el esquema de una base MySQL antigua (columnas viejas NOT NULL que ya no existen en las entidades
 *     y columnas demasiado cortas). Esto evita errores como "Field 'x' doesn't have a default value" o
 *     "Data too long for column" al guardar tickets, boletas o prendas con foto.
 *  2) Cifra con BCrypt las contraseñas de empleados que estén en texto plano.
 *
 * El sistema arranca con la base de datos VACÍA: no se carga ningún dato de ejemplo. El único registro inicial es
 * la cuenta del administrador, que se guarda en la tabla Empleado al arrancar si la tabla está vacía (también hay un
 * script equivalente en demo/database/crear_administrador.sql); desde ahí el administrador agrega empleados, clientes, prendas, penalidades y métodos de pago.
 * El borrado completo requiere activar explícitamente terno.reiniciar-datos=true en application.properties
 * (úsalo una sola vez para limpiar una base antigua y vuelve a ponerlo en false).
 */
@Configuration
public class DatosIniciales {

    private static final Logger LOG = LoggerFactory.getLogger(DatosIniciales.class);

    @Value("${terno.reiniciar-datos:false}")
    private boolean forzarReinicio;

    @Bean
    CommandLineRunner cargar(DataSource ds, EntityManagerFactory emf, EmpleadoService empleados) {
        return args -> {
            repararEsquema(ds, emf);

            /* El arranque normal nunca borra datos. El reinicio destructivo solo se permite si el operador
               lo activa explícitamente con terno.reiniciar-datos=true. */
            if (forzarReinicio) {
                LOG.warn("Se solicitó explícitamente terno.reiniciar-datos=true; se vaciarán todas las tablas.");
                borrarTodo(ds);
            }

            /* Si la tabla de empleados está vacía, se inserta en la base la cuenta del administrador */
            crearAdministradorInicial(empleados);

            /* Seguridad: las contraseñas cargadas desde el script SQL (texto plano) se guardan cifradas (BCrypt) */
            empleados.cifrarContrasenasPendientes();
        };
    }

    /* ---------- Administrador inicial ---------- */

    /* Solo cuando NO hay ningún empleado: guarda en la tabla Empleado al administrador (contraseña cifrada con BCrypt).
       Si ya existe cualquier empleado no hace nada, así que borrar o cambiar este usuario después no lo vuelve a crear. */
    private void crearAdministradorInicial(EmpleadoService empleados) {
        try {
            if (!empleados.listar().isEmpty()) return;
            Empleado admin = new Empleado();
            admin.setNombre("Jefferson");
            admin.setApellido("Encarnacion");
            admin.setUsuario("admin");
            admin.setContrasena("Admin1pru");
            admin.setCorreo("jeffersonadmin1@gmail.com");
            admin.setTelefono("974673845");
            admin.setArea("Administración");
            admin.setRol("Administrador");
            empleados.crear(admin);
            LOG.info("Se creó la cuenta del administrador en la tabla Empleado.");
        } catch (RuntimeException e) {
            LOG.error("No se pudo crear el administrador inicial: {}", e.getMessage());
        }
    }

    /* ---------- Esquema ---------- */

    private void repararEsquema(DataSource ds, EntityManagerFactory emf) {
        /* Columnas que sí usa cada entidad (nombre del atributo y nombre de la clave foránea idXxx) */
        Map<String, Set<String>> conocidas = new HashMap<>();
        for (EntityType<?> et : emf.getMetamodel().getEntities()) {
            Set<String> cols = new HashSet<>();
            cols.add("id");
            for (Attribute<?, ?> a : et.getAttributes()) {
                String n = a.getName().toLowerCase();
                cols.add(n);
                cols.add("id" + n);
            }
            conocidas.put(et.getName().toLowerCase(), cols);
        }
        try (Connection con = ds.getConnection()) {
            /* Columnas antiguas NOT NULL sin valor por defecto que la entidad actual no conoce: se vuelven opcionales */
            List<String[]> viejas = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS "
                     + "WHERE TABLE_SCHEMA = DATABASE() AND IS_NULLABLE = 'NO' AND COLUMN_DEFAULT IS NULL "
                     + "AND EXTRA NOT LIKE '%auto_increment%' AND EXTRA NOT LIKE '%GENERATED%' AND COLUMN_KEY <> 'PRI'")) {
                while (rs.next()) viejas.add(new String[] { rs.getString(1), rs.getString(2), rs.getString(3) });
            }
            for (String[] c : viejas) {
                Set<String> cols = conocidas.get(c[0].toLowerCase());
                if (cols == null || cols.contains(c[1].toLowerCase())) continue;
                ejecutar(con, "ALTER TABLE `" + c[0] + "` MODIFY `" + c[1] + "` " + c[2] + " NULL");
                LOG.warn("Columna antigua {}.{} ahora acepta valores vacíos.", c[0], c[1]);
            }
            /* Columnas de texto que en bases antiguas quedaron cortas (la ficha de confección y las fotos) */
            asegurarTexto(con, "Confeccion", "detalle", "TEXT", Set.of("text", "mediumtext", "longtext"));
            asegurarTexto(con, "Prenda", "imagen", "LONGTEXT", Set.of("longtext"));
        } catch (SQLException e) {
            LOG.error("No se pudo revisar el esquema de la base de datos: {}", e.getMessage());
        }
    }

    private void asegurarTexto(Connection con, String tabla, String columna, String tipoNuevo, Set<String> aceptados) throws SQLException {
        String tipoActual = null, tablaReal = null, columnaReal = null;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                 + "AND LOWER(TABLE_NAME) = '" + tabla.toLowerCase() + "' AND LOWER(COLUMN_NAME) = '" + columna.toLowerCase() + "'")) {
            if (rs.next()) { tablaReal = rs.getString(1); columnaReal = rs.getString(2); tipoActual = rs.getString(3); }
        }
        if (tipoActual != null && !aceptados.contains(tipoActual.toLowerCase())) {
            ejecutar(con, "ALTER TABLE `" + tablaReal + "` MODIFY `" + columnaReal + "` " + tipoNuevo + " NULL");
            LOG.warn("Columna {}.{} ampliada a {}.", tablaReal, columnaReal, tipoNuevo);
        }
    }

    private void ejecutar(Connection con, String sql) {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            LOG.error("No se pudo ejecutar [{}]: {}", sql, e.getMessage());
        }
    }

    /* Vacía todas las tablas de la base (reinicia también los contadores de id) */
    private void borrarTodo(DataSource ds) throws SQLException {
        try (Connection con = ds.getConnection()) {
            List<String> tablas = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'")) {
                while (rs.next()) tablas.add(rs.getString(1));
            }
            try (Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
                try {
                    for (String t : tablas) st.execute("TRUNCATE TABLE `" + t + "`");
                } finally {
                    st.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }
        }
    }
}
