# Software Architecture

## Overview

The NexusMarket Marketplace Management System follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The primary objective of this architecture is to isolate the business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology independence.

The C4 views of this architecture (context, containers, components, code), one sheet per layer and the flow of every endpoint are in `SDD/C4/`.

---

# Architectural Principles

The architecture is based on the following principles:

- Domain-first design.
- Separation of concerns.
- Dependency inversion.
- Technology independence.
- High cohesion.
- Low coupling.
- Explicit boundaries between layers.

The domain contains all business rules and never depends on external technologies.

---

# Architecture Layers

The application is organized into four major components:

```
Application
│
├── Adapters
│
├── Domain
│
└── Infrastructure
```

Each component has a clearly defined responsibility.

---

# Package Structure

```text
src/
└── main/
    └── java/
        └── application/
            │
            ├── NexusmarketApplication.java
            │
            ├── adapters/
            │   │
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       ├── mappers/
            │   │       └── exception/
            │   │
            │   ├── useCases/
            │   │
            │   ├── security/
            │   │   ├── dtos/
            │   │   └── mappers/
            │   │
            │   └── out/
            │       ├── notification/
            │       │
            │       └── persistence/
            │           ├── mysql/
            │           │   ├── adapters/
            │           │   ├── entities/
            │           │   ├── repositories/
            │           │   └── mappers/
            │           │
            │           ├── mongodb/
            │           │   ├── adapters/
            │           │   ├── documents/
            │           │   ├── repositories/
            │           │   └── mappers/
            │           │
            │           └── memory/
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   │   ├── authentication/   (one class per catalog service, grouped by subdomain)
            │   │   ├── authorization/
            │   │   ├── user/ seller/ buyer/ warehouse/
            │   │   ├── catalog/ inventory/ cart/ order/
            │   │   ├── billing/ logistics/ returns/
            │   │   └── reporting/
            │   ├── exceptions/
            │   └── ports/
            │       ├── in/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── seed/
```

---

# Layer Responsibilities

## Application

The `application` package represents the root of the project.

It contains the application entry point and all architectural components.

### Responsibilities

- Application bootstrap.
- Component organization.
- Dependency composition.

---

## NexusmarketApplication.java

### Description

`NexusmarketApplication.java` is the application's entry point (Spring Boot `@SpringBootApplication` class in the root `application` package).

### Responsibilities

- Initialize the application.
- Load the infrastructure.
- Configure dependency injection.
- Start the REST server.

---

# Adapters

The adapters connect external technologies with the business domain.

Adapters translate external requests into domain operations and transform domain objects into technology-specific representations.

The domain never communicates directly with external systems.

---

## Input Adapters

Input adapters expose the application to external clients.

Current implementation:

```
adapters/in/rest
```

### Responsibilities

- Receive HTTP requests.
- Validate incoming data.
- Convert Request DTOs into Domain Models.
- Execute application use cases.
- Convert domain results into Response DTOs.

---

### Controllers

Controllers expose REST endpoints.

Responsibilities:

- Receive HTTP requests.
- Delegate execution to the domain.
- Return HTTP responses.

Controllers must never implement business rules.

---

### Requests

Request DTOs represent incoming HTTP payloads.

Responsibilities:

- Receive client data.
- Validate input.
- Transport data into the application.

These objects must not contain business logic.

---

### Responses

Response DTOs represent outgoing HTTP responses.

Responsibilities:

- Return processed information.
- Hide internal domain implementation.
- Standardize API responses.

---

### Mappers

Responsible for converting between:

- Request DTO ↔ Domain Model
- Domain Model ↔ Response DTO

This prevents the domain from depending on transport objects.

---

## Use Case Adapters

Located in `adapters/useCases/`. Each class implements one Role Input Port (`PublicAccessUseCaseImpl`, `BuyerUseCaseImpl`, `SellerUseCaseImpl`, `LogisticsOperatorUseCaseImpl`, `AdministratorUseCaseImpl`, `SupervisorUseCaseImpl`).

### Responsibilities

- Implement the Role Input Ports.
- Inject the Domain Services directly (there are no per-operation input interfaces).
- Pass the authenticated `User` as the requester and fix the parameters implied by the role.
- Never implement business rules.

See `Adapters/Use-cases-adapters.md`.

---

## Security Adapter

Located in `adapters/security/`. The security classes are adapters: the filter is an input adapter for every request, and the token and password services implement output ports.

Components:

- `SecurityConfig`: stateless `SecurityFilterChain` (JWT, no session, no CSRF, no HTTP Basic), role rules per route prefix, CORS, and the BCrypt `PasswordEncoder`.
- `JwtTokenServiceAdapter`: implements the `TokenService` output port (HS256 JWT with `sub`, `jti`, `ver`, `iat`, `exp`).
- `dtos/JwtClaimsDTO` and `mappers/JwtClaimsMapper`: the claims carried by the token and their conversion from the `User` and to the `TokenClaims` of the output port. The domain never sees JWT claims.
- `JwtAuthenticationFilter`: validates the bearer token and asks the domain service `LoadAuthenticatedUserService` for the current `User`, which rejects inactive persons and obsolete token versions. The token only carries identity, so a blocked account or a logout takes effect on the next request.
- `dtos/AuthenticatedUserPrincipal`: principal holding the `User` domain model passed by the controllers to the Role Input Ports.
- `BCryptPasswordServiceAdapter`: implements the `PasswordService` output port; passwords are only stored as BCrypt hashes (`users` table).
- `SecurityErrorHandler`: `401` / `403` responses with the standard error envelope.
- `RequestIdFilter`: `X-Request-Id` correlation.

Route authorization is the first boundary; the domain still enforces the permission matrix and ownership rules (`ValidatePermissionsService`, `ValidateOwnershipAccessService`). See `Adapters/Rest-security-cors.md`.

---

# Output Adapters

Output adapters connect the domain with external resources.

Examples:

- Databases
- Notification services
- External APIs
- Payment gateways

Current implementation:

```
Output adapters
├── Persistence
│   ├── MySQL     (default profile)
│   ├── MongoDB   (default profile)
│   └── Memory    ("memory" profile)
└── Notification
```

Each persistence technology implements the same Output Ports, so the active implementation is selected by Spring profile without any change to the domain.

---

## MySQL Adapter

Responsible for relational persistence.

### Components

#### Entities

Represent relational database tables, such as `Buyer`, `Seller`, `Product`, `Order`, and `Invoice`.

#### Repositories

Implement persistence operations.

#### Mappers

Convert Domain Models into database entities.

#### Adapters

Implement Domain Output Ports.

---

## MongoDB Adapter

Responsible for storing flexible, high-volume operational data such as inventory movement history and shipment tracking events.

### Components

#### Documents

Represent MongoDB collections.

#### Repositories

Provide document persistence.

#### Adapters

Implement Domain Output Ports for movement and tracking history (`MongoInventoryMovementRepositoryAdapter`, `MongoShipmentTrackingRepositoryAdapter`).

#### Mappers

Convert domain objects into MongoDB documents and back (`InventoryMovementDocumentMapper`, `ShipmentTrackingDocumentMapper`).

---

## Memory Adapter

Implements every persistence Output Port with in-memory collections (`InMemoryDataStore`). It is active only with the `memory` Spring profile, which also disables the MySQL and MongoDB auto-configuration (`application-memory.properties`).

Used to run the complete application and the Spring context test without external databases.

---

## Notification Adapter

`LoggingNotificationAdapter` implements the `NotificationService` Output Port by writing each notification to the application log. It can be replaced by an e-mail or messaging adapter without changing the domain.

---

## Persistence of Aggregates

The relational model stores some domain entities inside the table of their aggregate root:

- `Invoice` and `Shipment` data are stored as columns of the `orders` table (`OrderEntity`).
- `Refund` data is stored as columns of the `returns` table (`ReturnEntity`).
- `Buyer` and `Seller` are stored through the `PersonEntity` hierarchy.

The domain keeps them as separate entities; the persistence mappers rebuild them when loading the aggregate.

---

# Domain

The Domain layer is the core of the application.

It contains all business rules and must remain independent from any external technology.

No class inside the domain may depend on:

- Spring
- JPA
- MongoDB
- HTTP
- REST
- JSON
- SQL

---

## Models

Contain the business entities.

Examples:

- Person
- AuthenticationResult
- User
- Buyer
- Seller
- Warehouse
- Product
- Inventory
- ShoppingCart
- Order
- Invoice
- Shipment
- Return
- Refund

These objects represent the Marketplace business.

---

## Value Objects

Represent immutable business concepts.

Examples:

- SystemRole
- UserStatus
- ProductStatus
- OrderStatus
- Address

Value Objects are compared by value instead of identity.

---

## Enums

Contain technical enumerations that do not require business behavior.

Examples:

- ShipmentStatus
- TrackingEventType
- ReturnStatus
- RefundStatus
- BusinessOperation

---

## Services

Contain business logic that does not naturally belong to a single entity.

Examples:

- InventoryReservationService
- ConfirmOrderService
- UpdateShipmentStatusService
- ProcessRefundService

Each catalog service is implemented by exactly one class (e.g. `ConfirmOrderService` implements "Confirm Order"), grouped in a subpackage per subdomain (`services/order/`, `services/logistics/`, ...). Shared checks live in `ValidatePermissionsService` and `ValidateOwnershipAccessService`, which the other services receive by constructor.

Services coordinate business operations while preserving domain integrity.

---

## Ports

Ports define communication contracts between the domain and external technologies.

The domain owns all interfaces.

---

### Input Ports

Represent application use cases (see `domain/Input-ports.md`). They are the **Role Input Ports**, one per system role, which REST controllers depend on: `PublicAccessPort`, `BuyerPort`, `SellerPort`, `LogisticsOperatorPort`, `AdministratorPort`, `SupervisorPort`. Each receives the authenticated `User` and exposes only the operations allowed for that role.

There are no per-operation input interfaces: the implementations of the Role Input Ports (`adapters/useCases/`) call the Domain Services directly, one class per catalog service.

Input ports define what the system can do.

---

### Output Ports

Represent dependencies required by the domain.

Examples:

- BuyerRepository
- SellerRepository
- ProductRepository
- InventoryRepository
- OrderRepository
- ShipmentTrackingRepository
- NotificationService
- UserRepository
- PasswordService
- TokenService

Output ports define what the domain needs from external systems.

---

## Exceptions

Contains business exceptions.

Examples:

- InsufficientInventoryException
- DuplicateEmailException
- OrderAlreadyFinalizedException
- InvalidRoleAssignmentException
- SellerSelfRegistrationNotAllowedException
- EntityNotFoundException (mapped to HTTP 404)
- WeakPasswordException
- InvalidCredentialsException (mapped to HTTP 401)
- AccountDisabledException
- BuyerNotAuthorizedException
- RefundNotAllowedException
- ReturnNotAllowedException

Business exceptions belong exclusively to the domain.

---

# Infrastructure

Infrastructure contains technical configuration required by the application.

It does not contain business logic.

---

## Config

Responsible for application configuration.

Current components:

- `DomainBeanConfig`: composes the domain by declaring every Domain Service as a Spring bean and injecting its Output Ports. The domain services themselves carry no Spring annotations.

---

## Seed

Loads reference data for development and demonstrations.

Current components:

- `DemoDataSeeder` (`infrastructure/seed/`): when the `demo` profile is active, loads an account for every role (administrator, supervisor, seller, buyer, logistics operator), a warehouse, a product and its inventory, so every group of endpoints can be tried right after startup. It is idempotent per identifier.

---

## Database

Contains database initialization and connection configuration.

Current components:

- `DatabaseConnectionVerifier`: checks the MySQL and MongoDB connections at startup and logs the result (inactive with the `memory` profile).
- Connection settings (URLs, credentials) are defined in `application.properties`.
- Business settings are also externalized there, e.g. `nexusmarket.billing.tax-rate` (VAT rate contained in catalog prices, default `0.19`), injected into `GenerateInvoiceService` by `DomainBeanConfig`.

---

## Spring Profiles

| Profile | Effect |
| ------- | ------ |
| *(default)* | MySQL and MongoDB persistence adapters. |
| `memory` | In-memory persistence adapters; MySQL and MongoDB auto-configuration disabled. |
| `demo` | Seeds demo data at startup (`DemoDataSeeder`). Can be combined with either persistence profile. |

---

# Dependency Flow

Dependencies always point toward the domain.

```
REST Controller
        │
        ▼
Role Input Port (*Port)
        │  implemented by
        ▼
Use Case Adapter (*UseCaseImpl)
        │  calls
        ▼
Domain Service
        │
        ▼
Output Port
        │
        ▼
Persistence Adapter
        │
        ▼
Database
```

The domain never depends on adapters or infrastructure.

---

# Benefits

This architecture provides:

- Technology independence.
- High maintainability.
- Clear separation of concerns.
- Improved testability.
- Easier scalability.
- Better support for Domain-Driven Design.
- Easy replacement of frameworks or databases.
- Reusable business logic.
- Long-term maintainability.

---

# Architectural Constraints

The following rules must always be respected:

1. Business logic belongs exclusively to the Domain layer.
2. Controllers must not contain business rules.
3. DTOs must never enter the Domain layer.
4. Persistence entities must never be exposed through the API.
5. Communication between technologies and the Domain must occur only through Ports.
6. Adapters implement Ports but never define business rules.
7. Infrastructure depends on the Domain, never the opposite.
8. Every dependency must point toward the Domain.
9. Business entities must remain framework-independent.
10. The Domain must be fully testable without requiring infrastructure components.
