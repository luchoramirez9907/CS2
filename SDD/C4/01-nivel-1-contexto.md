# C4 Nivel 1 — Contexto

## Propósito

Mostrar NexusMarket como una caja negra: quién lo usa y con qué sistemas
externos se comunica.

## Diagrama

```mermaid
C4Context
    title Contexto — NexusMarket

    Person(publico, "Visitante", "Se registra como comprador y hace login (sin token)")
    Person(comprador, "Comprador (BUYER)", "Carrito, pedidos, facturas, devoluciones")
    Person(vendedor, "Vendedor (SELLER)", "Publica productos, maneja bodegas e inventario")
    Person(operador, "Operador logístico (LOGISTICS_OPERATOR)", "Crea, despacha y entrega envíos")
    Person(admin, "Administrador (ADMINISTRATOR)", "Registra participantes, confirma pagos, resuelve devoluciones")
    Person(supervisor, "Supervisor (SUPERVISOR)", "Solo consulta: pedidos, devoluciones y reportes")

    System(nexus, "NexusMarket API", "Marketplace multi-vendedor. API REST /api/v1/** protegida con JWT")

    SystemDb_Ext(mysql, "MySQL 8", "Datos relacionales del negocio")
    SystemDb_Ext(mongo, "MongoDB 6", "Historial: movimientos de inventario y rastreo de envíos")
    System_Ext(front, "Frontend SPA", "Cliente web en FRONTEND_ORIGIN (fuera de este repositorio)")

    Rel(publico, front, "Usa")
    Rel(comprador, front, "Usa")
    Rel(vendedor, front, "Usa")
    Rel(operador, front, "Usa")
    Rel(admin, front, "Usa")
    Rel(supervisor, front, "Usa")
    Rel(front, nexus, "JSON sobre HTTP, Authorization: Bearer <jwt>")
    Rel(nexus, mysql, "JDBC / JPA")
    Rel(nexus, mongo, "Spring Data MongoDB")
```

## Actores y lo que pueden hacer

| Actor | Prefijo de rutas | Puerto de entrada | Ejemplos |
|---|---|---|---|
| Visitante | `/api/v1/public`, `/api/v1/auth` | `PublicAccessPort` | Registrarse y hacer login (las únicas peticiones sin token); con token, buscar en el catálogo |
| Comprador | `/api/v1/buyer` | `BuyerPort` | Carrito, checkout, factura, devolución |
| Vendedor | `/api/v1/seller` | `SellerPort` | Publicar producto, inventario, reporte de desempeño |
| Operador logístico | `/api/v1/operator` | `LogisticsOperatorPort` | Crear, despachar y entregar envíos |
| Administrador | `/api/v1/admin` | `AdministratorPort` | Usuarios, pagos, facturas, devoluciones, reembolsos |
| Supervisor | `/api/v1/supervisor` | `SupervisorPort` | Consultas y reportes, sin modificar nada |

## Sistemas externos

| Sistema | Para qué |
|---|---|
| MySQL | Personas, usuarios, productos, bodegas, inventario, carritos, pedidos, devoluciones |
| MongoDB | `inventory_movements` y `shipment_tracking` (historiales que solo crecen) |
| Frontend SPA | Único origen permitido por CORS (`FRONTEND_ORIGIN`, por defecto `http://localhost:5173`) |
