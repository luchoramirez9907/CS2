# C4 Nivel 2 — Contenedores

## Propósito

Mostrar dónde se ejecuta cada parte y cómo viaja una petición desde el
navegador hasta las bases de datos.

## Diagrama

```mermaid
C4Container
    title Contenedores — NexusMarket

    Person(usuario, "Usuario por rol", "Navegador con el JWT en memoria")

    System_Boundary(boundary, "Plataforma (docker-compose.yml)") {
        Container(spa, "Frontend SPA", "Vite/React", "Origen FRONTEND_ORIGIN; envía el JWT como Bearer")
        Container(app, "nexusmarket-app", "Spring Boot 4.1 + Java 17", "Hexágono: rest → useCases → domain → persistence/security. Puerto 8080 (SERVER_PORT)")
        ContainerDb(mysql, "mysql-db", "MySQL 8 / nexusmarket :3306", "Tablas JPA: persons, users, products, warehouses, inventories, orders, returns...")
        ContainerDb(mongo, "mongo-db", "MongoDB 6 / nexusmarket :27017", "Colecciones inventory_movements y shipment_tracking")
    }

    Rel(usuario, spa, "Usa")
    Rel(spa, app, "JSON/HTTP :8080, Authorization: Bearer <jwt>, X-Request-Id")
    Rel(app, mysql, "JDBC DB_URL / DB_USERNAME / DB_PASSWORD, DDL automático")
    Rel(app, mongo, "MONGODB_URI (spring.mongodb.uri)")
```

## Contenedores y configuración

| Contenedor | Imagen / arranque | Variables relevantes | Papel |
|---|---|---|---|
| `spa` | Fuera de este repositorio (`:5173`) | `FRONTEND_ORIGIN` del lado de la app | Dispara todos los flujos; el preflight `OPTIONS` va sin JWT |
| `nexusmarket-app` | `nexusmarket/nexusmarketII/Dockerfile` (Maven + JDK 17) | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MONGODB_URI`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `FRONTEND_ORIGIN`, `SPRING_PROFILES_ACTIVE`, `SERVER_PORT` | Atiende `/api/v1/**`, valida el JWT, ejecuta el dominio y persiste |
| `mysql-db` | `mysql:8.0` con `healthcheck` | `MYSQL_DATABASE=nexusmarket`, `MYSQL_USER=nexusmarket_app` | Lecturas y escrituras de todos los agregados |
| `mongo-db` | `mongo:6.0` con `healthcheck` | — | Escritura de movimientos de inventario y eventos de rastreo; lectura del historial |

`nexusmarket-app` espera a que `mysql-db` y `mongo-db` estén sanos (`depends_on: condition: service_healthy`).
Se levanta todo con `docker compose up --build` desde la raíz del repositorio (ver `SETUP.md`, opción C).

Sin Docker, el perfil `memory` reemplaza ambos contenedores de datos por adaptadores en memoria
dentro del mismo proceso.

## Flujo de red de una petición

```mermaid
sequenceDiagram
    participant SPA as SPA
    participant APP as nexusmarket-app :8080
    participant MY as MySQL
    participant MO as MongoDB
    SPA->>APP: HTTP /api/v1/** + Bearer (salvo login y registro de comprador)
    APP->>APP: JwtAuthenticationFilter valida firma y vencimiento
    APP->>MY: LoadAuthenticatedUserService carga el User (activo y con versión de token vigente)
    APP->>APP: SecurityConfig autoriza por ROLE_* según el prefijo de la ruta
    APP->>MY: JPA (lectura/escritura del agregado del endpoint)
    APP->>MO: Movimiento de inventario o evento de rastreo (solo si la operación lo genera)
    APP-->>SPA: 200/201/204 + DTO, o sobre de error + X-Request-Id
```

Semántica: `200/201/204` éxito · `400` validación · `401` sin token, token inválido o credenciales
incorrectas · `403` rol o propiedad · `404` no encontrado · `409` duplicado o transición inválida ·
`422` regla de negocio.
