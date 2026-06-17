# SmartLogix — ms-usuarios

Microservicio de gestión de usuarios. Maneja el registro, autenticación y generación de tokens JWT para toda la plataforma SmartLogix.

## Tecnologías

- Java 17
- Spring Boot 3.5.14
- Spring Data JPA
- Spring Security
- PostgreSQL
- JJWT (JSON Web Tokens)
- JUnit 5 + Mockito

## Patrón de diseño implementado

**Factory Method** — `factory/UsuarioFactory.java` decide si crea un `UsuarioPyme` o un `UsuarioAdmin` según el tipo indicado en el registro, sin que el resto del código necesite conocer los detalles de cada subtipo.

## Requisitos

- Java 17 (JDK)
- Maven (incluido como `mvnw`)
- PostgreSQL corriendo en el puerto configurado (o usar Docker Compose desde el repositorio principal)

## Configuración

Las credenciales de base de datos se definen en `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/db_usuarios
    username: smartlogix
    password: smartlogix123
```

## Instalación y ejecución

```bash
./mvnw clean install
./mvnw spring-boot:run
```

El microservicio queda disponible en `http://localhost:8084`.

## Cómo ejecutar las pruebas

```bash
./mvnw test
```

Se ejecutan 5 pruebas unitarias sobre `AuthService` (registro y login), usando Mockito para simular el repositorio y el codificador de contraseñas.

## Endpoints principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/registro` | Registra un nuevo usuario PYME o ADMIN |
| POST | `/api/auth/login` | Autentica y devuelve un token JWT |

Ver la colección Postman en el repositorio principal para ejemplos completos de petición y respuesta.

## Estructura del proyecto

```
ms-usuarios/
├── src/main/java/com/smartlogix/usuarios/
│   ├── controller/      AuthController
│   ├── service/         AuthService
│   ├── factory/         UsuarioFactory (Factory Method)
│   ├── model/            Usuario (entidad JPA)
│   ├── repository/       UsuarioRepository
│   ├── dto/               RegistroDTO, LoginDTO
│   └── security/          JwtUtil, filtros de seguridad
├── src/test/java/com/smartlogix/usuarios/
│   └── AuthServiceTest.java
├── Dockerfile
└── pom.xml
```
