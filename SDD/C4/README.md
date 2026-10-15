# Documentación C4 — NexusMarket

Modelo C4 en 4 niveles, una ficha por cada capa del proyecto y la matriz de
flujos por endpoint. Los diagramas están en **Mermaid** (se ven en GitHub y
en VS Code con la extensión de Mermaid).

## Mapa de documentos

| # | Archivo | Contenido |
|---|---------|-----------|
| — | `README.md` | Este índice |
| L1 | `01-nivel-1-contexto.md` | Contexto: actores (5 roles + público) y sistemas externos |
| L2 | `02-nivel-2-contenedores.md` | Contenedores (`nexusmarket-app`, MySQL, MongoDB, frontend) y red |
| L3 | `03-nivel-3-componentes.md` | Componentes internos y ciclo de vida de una petición |
| L4 | `04-nivel-4-codigo.md` | Clases clave y sus colaboraciones (flujo de compra) |
| Capa | `05-capa-adapters-rest.md` | Adaptador de entrada REST: controladores, DTOs, mappers, errores |
| Capa | `06-capa-adapters-usecases.md` | Adaptadores de casos de uso (implementan los puertos por rol) |
| Capa | `07-capa-adapters-persistence.md` | Persistencia MySQL (JPA), MongoDB (historial) y memoria |
| Capa | `08-capa-adapters-security.md` | Seguridad: JWT, filtro, `SecurityConfig`, BCrypt, principal |
| Capa | `09-capa-domain.md` | Dominio puro: modelos, value objects, servicios, puertos, excepciones |
| Capa | `10-capa-infrastructure.md` | Infraestructura: `DomainBeanConfig`, `DemoDataSeeder`, verificación de bases |
| Flujos | `11-endpoints-flujos.md` | Matriz de los 138 endpoints y secuencias por rol |

## Correspondencia capas ↔ código

```text
nexusmarket/nexusmarketII/src/main/java/application/
├── adapters/in/rest/          → 05 (entrada HTTP)
├── adapters/useCases/         → 06 (implementan domain/ports/in)
├── adapters/out/persistence/  → 07 (implementan domain/ports/out contra MySQL/MongoDB/memoria)
├── adapters/out/notification/ → 07 §5 (LoggingNotificationAdapter)
├── adapters/security/         → 08 (JWT con dtos/ y mappers/, filtro, config, BCrypt)
├── domain/                    → 09 (models, valueobjects, enums, services,
│                                    ports/in, ports/out, exceptions)
└── infrastructure/            → 10 (config, seed, database)
```

Regla transversal: **las dependencias apuntan al dominio**.
`rest → ports/in → useCases → services → ports/out → persistence/security`.
El dominio nunca importa Spring, JPA, MongoDB, HTTP ni JSON.

## Convenciones de los diagramas

- `C4Context`, `C4Container` y `C4Component` para los niveles 1 a 3 (sintaxis C4 de Mermaid).
- `sequenceDiagram` para el flujo de cada grupo de endpoints.
- `classDiagram` simplificado para el nivel 4 (solo las clases que participan en el flujo mostrado).
