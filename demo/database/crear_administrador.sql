-- =====================================================================
--  Terno Gabriel - Cuenta del administrador (se inserta directo en la base)
--  Base: AC_terno_gabriel
--
--  CÓMO USARLO
--   (La aplicación ya crea este administrador sola al arrancar si Empleado está vacía;
--    usa este script solo si prefieres insertarlo a mano.)
--   1) Ejecuta la aplicación UNA vez (Hibernate crea las tablas solo).
--   2) Abre MySQL Workbench y ejecuta este archivo completo.
--   3) Reinicia la aplicación: la contraseña se cifra con BCrypt automáticamente.
--   4) Inicia sesión con:  usuario  admin   (o el correo)  ·  contraseña  Admin1pru
--
--  Se puede ejecutar varias veces: INSERT IGNORE no duplica el registro
--  porque código, usuario y correo son únicos.
-- =====================================================================
USE AC_terno_gabriel;

INSERT IGNORE INTO `Empleado` (codigo, nombre, apellido, usuario, contrasena, correo, telefono, area, rol, estado, desde) VALUES
('E001', 'Jefferson', 'Encarnacion', 'admin', 'Admin1pru', 'jeffersonadmin1@gmail.com', '974673845', 'Administración', 'Administrador', 'Activo', CURDATE());
