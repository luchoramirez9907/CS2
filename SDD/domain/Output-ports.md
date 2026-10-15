# Output Ports

## Introduction

Output Ports define what the NexusMarket domain needs from external systems: persistence, notifications, password hashing, and access tokens.

The domain owns every output port interface (`domain/ports/out/`). Adapters in `adapters/out/` and `infrastructure/` implement them; the domain never knows which technology is behind a port.

---

# Architectural Rule

```text
Domain Service (domain/services)
      │  depends on
      ▼
Output Port (domain/ports/out)          ← owned by the domain
      ▲
      │  implemented by
Output Adapter (adapters/out, infrastructure)
      │
      ▼
MySQL · MongoDB · In-memory store · Log · BCrypt · JWT
```

- Output ports use **Domain Models** and **Value Objects** in their signatures; they never expose JPA entities, MongoDB documents, or framework types.
- Lookups by reference use identifiers (`findById(String)`); persistence operations receive the domain aggregate (`save(Order)`).
- Every persistence port has two implementations: MySQL/MongoDB (default profile) and in-memory (`memory` profile). Exactly one is active at a time.

---

# Output Ports

## 1. PersonRepository

### Responsibility

Persists every participant (`Person` hierarchy) and guarantees identifier and email uniqueness checks.

### Methods

```java
void save(Person person);
Optional<Person> findById(String identifier);
Optional<Person> findByEmail(String email);
boolean existsByIdentifier(String identifier);
boolean existsByEmail(String email);
List<Person> findAll();
```

### Implementations

`MysqlPersonRepositoryAdapter` (tables `persons`, `buyers`, `sellers`), `InMemoryPersonRepositoryAdapter`.

### Main Consumers

`CreateShipmentService`, `ManageBuyerService`, `ManageSellerService`, `ManageUserService`, `RegisterBuyerService`, `RegisterSellerService`, `RegisterUserService`, and `ValidatePermissionsService`.

---

## 2. BuyerRepository

### Responsibility

Persists `Buyer` aggregates (addresses, commercial status, active cart reference).

### Methods

```java
void save(Buyer buyer);
Optional<Buyer> findById(String identifier);
```

### Implementations

`MysqlBuyerRepositoryAdapter`, `InMemoryBuyerRepositoryAdapter`.

### Main Consumers

`ConfirmOrderService`, `ConsultCartService`, `ManageBuyerService`, `ManageCartItemsService`, `RegisterBuyerService`, and `RequestReturnService`.

---

## 3. SellerRepository

### Responsibility

Persists `Seller` aggregates (registering administrator, warehouses, products).

### Methods

```java
void save(Seller seller);
Optional<Seller> findById(String identifier);
```

### Implementations

`MysqlSellerRepositoryAdapter`, `InMemorySellerRepositoryAdapter`.

### Main Consumers

`ConsultSellerPerformanceReportService`, `ManageSellerService`, `PublishProductService`, `RegisterSellerService`, and `RegisterWarehouseService`.

---

## 4. UserRepository

### Responsibility

Persists the access accounts (`User`) of the participants: password hash and token version.

### Methods

```java
void save(User user);
Optional<User> findByPersonId(String personIdentifier);
Optional<User> findByEmail(String email);
```

### Implementations

`MysqlUserRepositoryAdapter` (table `users`; resolves the owning `Person` through `PersonRepository`), `InMemoryUserRepositoryAdapter`.

### Main Consumers

`UserAccountService`, `AuthenticateUserService`, and `LoadAuthenticatedUserService` (used by the JWT authentication filter of `adapters/security`).

---

## 5. WarehouseRepository

### Responsibility

Persists `MarketplaceWarehouse` and `SellerWarehouse` instances.

### Methods

```java
void save(Warehouse warehouse);
Optional<Warehouse> findById(String identifier);
List<Warehouse> findAll();
```

### Implementations

`MysqlWarehouseRepositoryAdapter` (tables `warehouses`, `marketplace_warehouses`, `seller_warehouses`), `InMemoryWarehouseRepositoryAdapter`.

### Main Consumers

`ConsultInventoryService`, `CreateShipmentService`, `ManageWarehouseService`, `RegisterInventoryMovementService`, and `RegisterWarehouseService`.

---

## 6. ProductRepository

### Responsibility

Persists the catalog (`PhysicalProduct`, `DigitalProduct`) with its variants and price.

### Methods

```java
void save(Product product);
Optional<Product> findById(String identifier);
List<Product> findBySellerId(String sellerId);
List<Product> findAll();
```

### Implementations

`MysqlProductRepositoryAdapter` (tables `products`, `physical_products`, `digital_products`, `product_variants`), `InMemoryProductRepositoryAdapter`.

### Main Consumers

`ChangeProductStatusService`, `ConsultInventoryService`, `ConsultSellerPerformanceReportService`, `ManageCartItemsService`, `ManageProductService`, `PublishProductService`, and `RegisterInventoryMovementService`.

---

## 7. InventoryRepository

### Responsibility

Persists the current stock of each product per warehouse (available, reserved, damaged).

### Methods

```java
void save(Inventory inventory);
Optional<Inventory> findById(String identifier);
List<Inventory> findByProductId(String productId);
Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId);
List<Inventory> findAll();
```

### Implementations

`MysqlInventoryRepositoryAdapter` (table `inventories`), `InMemoryInventoryRepositoryAdapter`.

### Main Consumers

`ConsultCommercialReportService`, `ConsultInventoryService`, `InventoryReservationService`, and `RegisterInventoryMovementService`.

---

## 8. InventoryMovementRepository

### Responsibility

Appends every inventory movement to the movement history (high-volume, append-only operational data) and retrieves the history of an inventory record.

### Methods

```java
void save(InventoryMovement movement);
List<InventoryMovement> findByInventory(Inventory inventory);   // chronological order
```

### Implementations

`MongoInventoryMovementRepositoryAdapter` (collection `inventory_movements`); the performer of each movement is resolved through `PersonRepository`, `InMemoryInventoryMovementRepositoryAdapter`.

### Main Consumers

`ConsultInventoryService`, `InventoryReservationService`, and `RegisterInventoryMovementService`.

---

## 9. ShoppingCartRepository

### Responsibility

Persists the provisional shopping carts of the buyers.

### Methods

```java
void save(ShoppingCart cart);
Optional<ShoppingCart> findById(String cartId);
Optional<ShoppingCart> findActiveByBuyerId(String buyerId);
void delete(ShoppingCart cart);
```

### Implementations

`MysqlShoppingCartRepositoryAdapter` (tables `shopping_carts`, `cart_items`), `InMemoryShoppingCartRepositoryAdapter`.

### Main Consumers

`ConfirmOrderService`, `ConsultCartService`, and `ManageCartItemsService`.

---

## 10. OrderRepository

### Responsibility

Persists the `Order` aggregate, including its items, `Invoice`, and `Shipment`.

### Methods

```java
void save(Order order);
Optional<Order> findById(String orderId);
List<Order> findByBuyerId(String buyerId);
List<Order> findAll();
```

### Implementations

`MysqlOrderRepositoryAdapter` (tables `orders`, `order_items`), `InMemoryOrderRepositoryAdapter`.

### Main Consumers

`ConfirmOrderService`, `ConsultCommercialReportService`, `ConsultInvoiceService`, `ConsultOrderService`, `ConsultSellerPerformanceReportService`, `ConsultShipmentService`, `CreateShipmentService`, `GenerateInvoiceService`, `ManageBuyerService`, `RequestReturnService`, `UpdateOrderStatusService`, and `UpdateShipmentStatusService`.

---

## 11. ReturnRepository

### Responsibility

Persists the `Return` aggregate, including its `Refund`.

### Methods

```java
void save(Return returnRequest);
Optional<Return> findById(String returnId);
List<Return> findByBuyerId(String buyerId);
List<Return> findAll();
```

### Implementations

`MysqlReturnRepositoryAdapter` (table `returns`), `InMemoryReturnRepositoryAdapter`.

### Main Consumers

`ConsultCommercialReportService`, `ConsultSellerPerformanceReportService`, `ManageBuyerService`, `ProcessRefundService`, `RequestReturnService`, and `ResolveReturnService`.

---

## 12. ShipmentTrackingRepository

### Responsibility

Appends and retrieves the tracking history of each shipment (high-volume, append-only operational data).

### Methods

```java
void record(String shipmentId, String orderId, TrackingEventType event, LocalDateTime occurredAt, String details);
List<ShipmentTrackingEvent> findByShipmentId(String shipmentId);
```

### Implementations

`MongoShipmentTrackingRepositoryAdapter` (collection `shipment_tracking`), `InMemoryShipmentTrackingRepositoryAdapter`.

### Main Consumers

`ConsultShipmentService`, `CreateShipmentService`, and `UpdateShipmentStatusService`.

---

## 13. NotificationService

### Responsibility

Notifies a participant about a relevant business event (order confirmed, shipment delivered, refund processed, …).

### Methods

```java
void notify(Person recipient, String subject, String message);
```

### Implementations

`LoggingNotificationAdapter` (writes the notification to the application log).

### Main Consumers

Most Domain Services that change the state of an entity owned by a participant.

---

## 14. PasswordService

### Responsibility

One-way hashing and verification of passwords. The domain never stores or compares raw passwords.

### Methods

```java
String hash(String rawPassword);
boolean matches(String rawPassword, String passwordHash);
```

### Implementations

`BCryptPasswordServiceAdapter` (`adapters/security`).

### Main Consumers

`UserAccountService`, `AuthenticateUserService`.

---

## 15. TokenService

### Responsibility

Issues and validates access tokens. Tokens carry only identity claims: subject (person identifier), token id, token version, issue and expiration dates. Role and status are always read from the current `User`, never from the token.

### Methods

```java
String issueToken(User user);
Optional<TokenClaims> parseToken(String token);
long expirationSeconds();

record TokenClaims(String subject, String tokenId, int tokenVersion, Instant issuedAt, Instant expiresAt) { }
```

### Implementations

`JwtTokenServiceAdapter` (`adapters/security`, HS256 signed JWT with JJWT; claims mapped by `JwtClaimsMapper`).

### Main Consumers

`AuthenticateUserService` (issues the token at login) and the JWT authentication filter (validates it on every request).

---

# General Parameter Rule

| Operation kind | Parameter | Example |
| -------------- | --------- | ------- |
| Store an aggregate | Domain Model | `save(Order order)` |
| Load by reference | Identifier | `findById(String orderId)` |
| Query by relationship | Identifier of the related entity | `findByBuyerId(String buyerId)` |
| Append operational history | Domain values | `record(..., TrackingEventType event, ...)` |

Output ports never receive or return DTOs, JPA entities, MongoDB documents, or framework types.
