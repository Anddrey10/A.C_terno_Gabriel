# Actualización de dependencias — 26 de octubre de 2026

Se actualizaron las versiones declaradas del proyecto a versiones estables recientes verificadas en las páginas oficiales:

- Spring Boot: 4.1.2 (versión del parent y BOM que administra las versiones compatibles de Spring y otras dependencias).
- Guava: 33.7.2-jre.
- Apache POI OOXML: 6.0.0.
- Apache Commons Lang: 3.21.0.
- Maven Javadoc Plugin: 3.12.0.
- Java de compilación: 21 (se conserva para evitar exigir a quien ejecute el proyecto una instalación de JDK más nueva).

Las versiones de MySQL Connector/J, Lombok y las dependencias de Spring gestionadas por Spring Boot se mantienen administradas por el parent/BOM para reducir incompatibilidades. Esto prioriza las versiones más recientes compatibles con el conjunto, en lugar de forzar individualmente versiones transitivas que podrían romper la aplicación.

## Verificación

Se comprobó que `pom.xml` tiene XML válido. Se intentó ejecutar `./mvnw -DskipTests package`, pero el entorno no pudo descargar Maven desde Maven Central; por ello, la compilación completa no pudo verificarse aquí. Ejecutar `mvnw.cmd -DskipTests package` en Windows (o `./mvnw -DskipTests package` en Linux/macOS con permisos de ejecución) con conexión a Internet para confirmar la resolución de dependencias y la compilación.
