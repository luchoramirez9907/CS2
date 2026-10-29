# NexusMarket — Construcción de Software 2

Sistema de gestión de un **marketplace** en **Java 17 + Spring Boot 4.1** con
**arquitectura hexagonal** (DDD + Ports & Adapters), desarrollado a partir de la
especificación del `SDD/` (Software Design Document). Incluye roles de negocio
(comprador, vendedor, operador logístico, administrador y supervisor),
autenticación JWT con invalidación de sesiones, persistencia relacional en
**MySQL** e historial operativo (movimientos de inventario y seguimiento de
envíos) en **MongoDB**.

> **Para poner el proyecto en marcha paso a paso, ver [`SETUP.md`](SETUP.md).**

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje / Build | Java 17, Maven (wrapper `nexusmarket/nexusmarketII/mvnw.cmd`) |
| Framework | Spring Boot 4.1, Spring MVC, Spring Security (JWT stateless, BCrypt) |
| Persistencia relacional | Spring Data JPA / Hibernate → MySQL 8 (`nexusmarket`, puerto 3306, DDL automático) |
| Historial operativo | Spring Data MongoDB → MongoDB (`nexusmarket`: `inventory_movements`, `shipment_tracking`, puerto 27017) |
| Modo sin bases de datos | Perfil `memory` (adaptadores en memoria) + perfil `demo` (datos de ejemplo) |
| JWT | JJWT 0.12.6, HS256 (`sub`, `jti`, `ver`, `iat`, `exp`; sin datos personales ni rol) |
| Validación / errores | Jakarta Bean Validation, `@RestControllerAdvice` con envelope de error + `X-Request-Id` |
| Observabilidad | Spring Boot Actuator (`/actuator/health`) |
| Pruebas | JUnit 5 + Mockito + MockMvc — **112 tests en verde** (`.\mvnw.cmd test`) |

## Estructura del repositorio

```text
.
├── nexusmarket/nexusmarketII/     # Aplicación Spring Boot (código + tests + resources)
│   └── src/main/java/application/
│       ├── domain/                # Núcleo puro: models, valueobjects, enums, exceptions,
│       │                          #   services (una clase por servicio del catálogo), ports/in (6 puertos por rol), ports/out
│       ├── adapters/
│       │   ├── in/rest/           # Controladores por rol, DTOs, mappers, GlobalExceptionHandler
│       │   ├── useCases/          # Implementan los puertos por rol llamando a los servicios del dominio
│       │   ├── security/          # JWT (dtos, mappers), filtro, SecurityConfig, BCrypt, CORS
│       │   └── out/               # persistence (mysql, mongodb, memory), notification
│       └── infrastructure/        # config (DomainBeanConfig), seed (datos demo), database
│   ├── Dockerfile                 # Imagen de la aplicación (JDK 17 + Maven)
├── SDD/                           # Especificación: arquitectura, dominio, puertos, adapters y C4
├── docker-compose.yml             # MySQL 3306 + MongoDB 27017 + aplicación (8080)
└── SETUP.md                       # Guía paso a paso para ejecutar el proyecto
```

## Documentación (SDD)

| Documento | Contenido |
|---|---|
| `SDD/Software Architecture/Software Architecture.md` | Capas, paquetes, perfiles y flujo de dependencias |
| `SDD/domain/Domain Model.md` | Entidades, relaciones, ciclos de vida y reglas de negocio |
| `SDD/domain/Domain Value Objects.md` | Catálogos (`DomainCatalog`), `Address` y enums |
| `SDD/domain/Services.md` | Catálogo de servicios, trazabilidad servicio → puerto → clase y matriz de permisos |
| `SDD/domain/services/*.md` | Especificación detallada de cada operación (13 documentos, uno por subdominio) |
| `SDD/domain/Input-ports.md` / `Output-ports.md` | Puertos de entrada por rol y puertos de salida |
| `SDD/Adapters/*.md` | Casos de uso, REST (endpoints, validación, errores, seguridad/CORS) y persistencia |
| `SDD/C4/*.md` | Modelo C4 (contexto, contenedores, componentes, código), una ficha por capa y flujos por endpoint |

## Seguridad y roles

- Autenticación JWT sin sesión. El token solo lleva identidad (`sub` = identificador de la persona,
  `jti`, `ver` = `User.authTokenVersion`); el rol y el estado se leen siempre del modelo vigente.
- Logout y cambio de contraseña incrementan la versión e **invalidan todos los tokens anteriores**.
- **Toda petición exige token**, salvo el login (que entrega el token) y el registro de comprador (que crea la cuenta). El catálogo y `/actuator/health` también lo piden.
- Un puerto de entrada y un prefijo de rutas por rol:
  `/api/v1/buyer`, `/api/v1/seller`, `/api/v1/operator`, `/api/v1/admin`, `/api/v1/supervisor`,
  más `/api/v1/public` (registro de compradores, sin token, y catálogo, con token) y `/api/v1/auth` (login, logout, contraseña).
- El dominio aplica además la matriz de permisos y las reglas de propiedad (un comprador no ve pedidos ajenos).
- Semántica HTTP: 200/201/204 en éxito, 400 validación, 401 sin token o credenciales inválidas,
  403 rol o propiedad, 404 no encontrado, 409 duplicado o transición inválida, 422 regla de negocio.
- CORS restringido a `FRONTEND_ORIGIN` (por defecto `http://localhost:5173`).

## Variables de entorno

| Variable | Default | Uso |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/nexusmarket?...` | JDBC MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `root_password` | Credenciales MySQL (coinciden con `docker-compose.yml`) |
| `MONGODB_URI` | `mongodb://localhost:27017/nexusmarket` | URI MongoDB |
| `JWT_SECRET` / `JWT_EXPIRATION_MS` | valor de desarrollo / `3600000` | Firma (≥ 32 bytes) y vigencia del JWT |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Origen CORS permitido |
| `SERVER_PORT` | `8080` | Puerto HTTP |
| `TAX_RATE` | `0.19` | IVA incluido en los precios del catálogo |
| `DEMO_PASSWORD` | ver `application.properties` | Contraseña de las cuentas del perfil `demo` |

## Estado de validación

- [x] Compilación limpia + 112/112 tests (dominio, aislamiento entre roles, autenticación, carga del usuario autenticado, JWT, validación de catálogos, mappers, adaptadores, casos de uso por rol, API REST con MockMvc)
- [x] Endpoints sobre los adaptadores JPA reales (entidades, mappers y repositorios de MySQL) con H2 en modo MySQL: login → carrito → checkout → pago → factura (`RelationalPersistenceIntegrationTest`)
- [x] Arranque verificado en modo `memory,demo`: login → endpoint protegido → carrito → checkout → pago → factura → logout → token viejo rechazado (401)
- [x] Arranque con `docker compose up --build` (MySQL 8 + MongoDB 6 + aplicación) verificado: conexión OK a ambas bases, datos demo sembrados y flujo completo por HTTP (carrito → checkout → pago → envío → entrega → finalización → factura → devolución → reembolso → reingreso de stock) con los datos guardados en MySQL (18 tablas) y el historial en MongoDB (`inventory_movements`, `shipment_tracking`)

## Notas

- Spring Boot 4.x usa el prefijo **`spring.mongodb.uri`** (no `spring.data.mongodb.uri`).
- Sin bases de datos se puede ejecutar todo con el perfil `memory` (ver `SETUP.md`).
