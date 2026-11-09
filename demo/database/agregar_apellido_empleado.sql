-- =====================================================================
--  Terno Gabriel - Columna APELLIDO en la tabla Empleado
--  Base: AC_terno_gabriel
--
--  La aplicación ya agrega esta columna sola al arrancar (Hibernate,
--  ddl-auto=update), así que normalmente NO necesitas ejecutar este script.
--  Úsalo solo si prefieres crear la columna a mano ANTES de arrancar.
--  Si la columna ya existe, MySQL mostrará "Duplicate column name": no pasa nada.
-- =====================================================================
USE AC_terno_gabriel;

ALTER TABLE `Empleado` ADD COLUMN `apellido` VARCHAR(100) NULL AFTER `nombre`;

-- Opcional: separar "Nombre Apellido" de los empleados que ya existían
-- (toma la última palabra como apellido). Revisa el resultado antes de usarlo
-- si algún empleado tiene dos nombres o dos apellidos.
-- UPDATE `Empleado`
--    SET `apellido` = SUBSTRING_INDEX(`nombre`, ' ', -1),
--        `nombre`   = TRIM(SUBSTRING(`nombre`, 1, CHAR_LENGTH(`nombre`) - CHAR_LENGTH(SUBSTRING_INDEX(`nombre`, ' ', -1))))
--  WHERE `apellido` IS NULL AND `nombre` LIKE '% %';
