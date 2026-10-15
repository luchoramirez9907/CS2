# Persistence Adapters Specification (Output Adapters)

## 1. Overview

Persistence adapters reside in `application/adapters/out/persistence/`. They implement the persistence **Output Ports** defined in `application/domain/ports/out/` (see `domain/Output-ports.md`).

To keep the domain independent from any persistence technology:

1. Domain models are **never** used as database entities or documents.
2. Every adapter defines its own **Entities / Documents** (repository DTOs), **Mappers** (Domain ↔ Entity/Document), and **Spring Data Repositories**.
3. Relational data uses **Spring Data JPA / Hibernate** (MySQL); high-volume, append-only operational history uses **Spring Data MongoDB**.

---

## 2. Architecture and Data Flow

```text
Domain Service (domain/services)
      │  calls Output Port
      ▼
Output Port (domain/ports/out)
      ▲
      │  implemented by
Persistence Adapter (adapters/out/persistence/*/adapters)
      │  1. Mapper: Domain Model → Entity / Document
      │  2. Spring Data Repository call
      │  3. Mapper: Entity / Document → Domain Model
      ▼
Spring Data Repository (JpaRepository / MongoRepository)
      │
      ▼
MySQL (3306) · MongoDB (27017)
```

---

## 3. Structure

```text
adapters/out/persistence/
├── mysql/                                 Relational persistence (default profile)
│   ├── entities/      @Entity classes (repository DTOs)
│   ├── mappers/       Domain ↔ Entity
│   ├── repositories/  Spring Data JPA repositories
│   └── adapters/      Mysql*RepositoryAdapter (implement Output Ports)
│
├── mongodb/                               Operational history (default profile)
│   ├── documents/     @Document classes (repository DTOs)
│   ├── mappers/       Domain ↔ Document
│   ├── repositories/  Spring Data MongoDB repositories
│   └── adapters/      Mongo*RepositoryAdapter (implement Output Ports)
│
└── memory/                                In-memory persistence ("memory" profile)
    ├── InMemoryDataStore
    └── InMemory*RepositoryAdapter
```

---

## 4. Relational Model (MySQL)

| Output Port | Adapter | Entities (tables) | Mapper | Repository |
| ----------- | ------- | ----------------- | ------ | ---------- |
| `PersonRepository` | `MysqlPersonRepositoryAdapter` | `PersonEntity` (`persons`), `BuyerEntity` (`buyers`), `SellerEntity` (`sellers`), `AddressEmbeddable` | `PersonMapper` | `PersonJpaRepository` |
| `BuyerRepository` | `MysqlBuyerRepositoryAdapter` | `BuyerEntity` | `PersonMapper` | `PersonJpaRepository` |
| `SellerRepository` | `MysqlSellerRepositoryAdapter` | `SellerEntity` | `PersonMapper` | `PersonJpaRepository` |
| `UserRepository` | `MysqlUserRepositoryAdapter` | `UserEntity` (`users`) | `UserMapper` | `UserJpaRepository` |
| `WarehouseRepository` | `MysqlWarehouseRepositoryAdapter` | `WarehouseEntity` (`warehouses`), `MarketplaceWarehouseEntity`, `SellerWarehouseEntity` | `WarehouseMapper` | `WarehouseJpaRepository` |
| `ProductRepository` | `MysqlProductRepositoryAdapter` | `ProductEntity` (`products`), `PhysicalProductEntity`, `DigitalProductEntity`, `ProductVariantEntity` | `ProductMapper` | `ProductJpaRepository` |
| `InventoryRepository` | `MysqlInventoryRepositoryAdapter` | `InventoryEntity` (`inventories`) | `InventoryMapper` | `InventoryJpaRepository` |
| `ShoppingCartRepository` | `MysqlShoppingCartRepositoryAdapter` | `ShoppingCartEntity` (`shopping_carts`), `CartItemEntity` (`cart_items`) | `CartMapper` | `ShoppingCartJpaRepository` |
| `OrderRepository` | `MysqlOrderRepositoryAdapter` | `OrderEntity` (`orders`), `OrderItemEntity` (`order_items`) | `OrderMapper` | `OrderJpaRepository` |
| `ReturnRepository` | `MysqlReturnRepositoryAdapter` | `ReturnEntity` (`returns`) | `ReturnMapper` | `ReturnJpaRepository` |

Inheritance in the domain (`Person`, `Warehouse`, `Product`) is mapped with JPA `JOINED` inheritance: a parent table plus one table per specialization.

### Aggregate boundaries

Some domain entities are stored inside the table of their aggregate root, because they are always loaded and changed together with it:

| Domain entity | Stored in | Reason |
| ------------- | --------- | ------ |
| `Invoice` | columns `invoice_*` of `orders` | One invoice per order, issued at checkout; never accessed without its order. |
| `Shipment` | columns `shipment_*` of `orders` | One shipment per order; its lifecycle drives the order lifecycle. |
| `Refund` | columns `refund_*` of `returns` | At most one refund per return; always accessed through the return. |

The domain keeps them as separate entities with their own identifiers; the mappers rebuild them when the aggregate is loaded.

The schema is generated automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

---

## 5. Operational History (MongoDB)

| Output Port | Adapter | Document (collection) | Mapper | Repository |
| ----------- | ------- | --------------------- | ------ | ---------- |
| `InventoryMovementRepository` | `MongoInventoryMovementRepositoryAdapter` | `InventoryMovementDocument` (`inventory_movements`) | `InventoryMovementDocumentMapper` | `InventoryMovementMongoRepository` |
| `ShipmentTrackingRepository` | `MongoShipmentTrackingRepositoryAdapter` | `ShipmentTrackingDocument` (`shipment_tracking`) | `ShipmentTrackingDocumentMapper` | `ShipmentTrackingMongoRepository` |

Catalog values are stored by their code (`movementTypeCode`, `event`) and converted back into the domain Value Object or enum when read. When the movement history is read, the mapper rebuilds each `InventoryMovement` with the inventory record already loaded and the performer resolved through the `PersonRepository` output port. Collections are created automatically with the first document.

---

## 6. In-Memory Persistence

With the `memory` profile, every persistence port is served by an `InMemory*RepositoryAdapter` over a shared `InMemoryDataStore`, and the MySQL and MongoDB auto-configurations are disabled (`application-memory.properties`). It is used to run the application and the Spring context tests without databases.

---

## 7. Configuration

| Property | Environment variable | Default |
| -------- | -------------------- | ------- |
| `spring.datasource.url` | `DB_URL` | `jdbc:mysql://localhost:3306/nexusmarket?createDatabaseIfNotExist=true&...` |
| `spring.datasource.username` / `password` | `DB_USERNAME` / `DB_PASSWORD` | `root` / `root_password` |
| `spring.mongodb.uri` | `MONGODB_URI` | `mongodb://localhost:27017/nexusmarket` |

Spring Boot 4 uses the `spring.mongodb` prefix (not `spring.data.mongodb`). The local databases are started with the `docker-compose.yml` at the repository root.

---

## 8. Rules

1. Adapters implement Output Ports but never define business rules.
2. Entities and documents never leave the persistence adapter: they are not returned by output ports nor exposed through the API.
3. Inconsistent stored data (a reference to a missing row) raises `IllegalStateException` inside the adapter; a missing entity requested by the domain is reported by the domain as `EntityNotFoundException`.

---

## 9. Verification

`RelationalPersistenceIntegrationTest` starts the application on the relational adapters (JPA entities, mappers and Spring Data repositories) instead of the in-memory ones, over an H2 database in MySQL mode with the schema generated by Hibernate, and runs the purchase flow over HTTP: login, add to cart, checkout, payment confirmation and invoice. It checks that the order is stored in the `orders` table through `MysqlOrderRepositoryAdapter`. MongoDB is not available in that test, so only the two history ports use in-memory adapters. A run against real MySQL and MongoDB servers is done with `docker compose up --build` (see `SETUP.md`). That run was verified: both connections OK at startup, demo data seeded, and the complete flow (purchase, shipment, delivery, return, refund and stock reinstatement) stored in the 18 MySQL tables and in the two MongoDB collections.
