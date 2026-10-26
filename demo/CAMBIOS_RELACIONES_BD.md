# Cambios de seguridad y relaciones — Terno Gabriel

## Qué se modificó

- Se conserva el modelo y los nombres JSON usados por el frontend; no se cambió el diseño de las pantallas ni los endpoints.
- `Boleta` ahora tiene una relación JPA real con `Empleado` mediante `idEmpleado`. Se conserva el campo de texto `empleado` por compatibilidad con la interfaz y se oculta la nueva relación en JSON para evitar ciclos y no alterar el contrato del frontend.
- `Confeccion` también se relaciona con `Empleado` mediante `idEmpleado`, manteniendo el campo de texto legado.
- `Configuracion` tiene una relación opcional con el empleado que modificó la configuración, conservando `modificadoPor`.
- Al emitir boletas, registrar confecciones o modificar una configuración, el backend intenta resolver el empleado por usuario, código o nombre para guardar la clave foránea.
- El arranque ya no borra la base de datos en la primera ejecución. Solo inserta datos de ejemplo si las tablas correspondientes están vacías. El borrado completo solo ocurre si se activa explícitamente `terno.reiniciar-datos=true`.

## Relaciones existentes que se mantienen

- `Boleta -> Cliente`
- `Boleta -> Prenda` (opcional para confección)
- `Boleta -> Confeccion` (opcional para boletas de confección)
- `Confeccion -> Cliente`
- `Penalidad -> Cliente`

Hibernate `ddl-auto=update` agregará las columnas de relación nuevas al arrancar. Haz una copia de seguridad de la base antes de iniciar la aplicación por primera vez con esta versión.

## Compatibilidad y verificación

No se cambió el frontend. Los constructores anteriores de servicios se mantienen para compatibilidad con pruebas/código legado. La ejecución de Maven no pudo verificarse en este entorno porque el wrapper no pudo descargar Maven desde Maven Central; por eso, ejecuta `./mvnw test` (Windows: `mvnw.cmd test`) en un entorno con conexión a Maven Central antes de desplegar.
