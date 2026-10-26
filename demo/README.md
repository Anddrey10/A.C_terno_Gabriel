# Terno Gabriel

Sistema web de una sastería: ventas, alquileres, reservas, confección a medida, clientes, penalidades e informes.
**Stack:** Java 21 · Spring Boot 4 · Spring Data JPA · Thymeleaf · MySQL · Maven.

Arquitectura, diagramas, seguridad y pruebas: [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

## Requisitos
- JDK 21, MySQL 8 en ejecución (el esquema `A.C_terno_gabriel` se crea solo).

## Configuración de la base de datos
Las credenciales **no** se guardan en Git.
1. Copia `src/main/resources/application-local.properties.example` como `src/main/resources/application-local.properties`.
2. Escribe tu usuario y contraseña de MySQL.

Alternativa: variables de entorno `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`.

## Ejecutar
```bash
./mvnw spring-boot:run        # http://localhost:8080
./mvnw test                   # pruebas unitarias
./mvnw javadoc:javadoc        # documentación técnica
```

Usuarios de demostración (se guardan cifrados): `admin` / `admin` (Administrador) y `empleado` / `1234` (Empleado).
Cambiar estas contraseñas antes de cualquier uso real.

## Flujo de trabajo con Git
- Rama estable: `main`. Cada funcionalidad en una rama `feature/<nombre>` y se une con Pull Request.
- Mensajes de commit con prefijos: `feat:`, `fix:`, `test:`, `docs:`, `build:`, `refactor:`.
- Los registros (`logs/`) y las credenciales locales están en `.gitignore`.
