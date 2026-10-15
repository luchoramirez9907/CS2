# Inventory Management Services

## 1. Purpose

This document specifies the registration of **inventory movements** and the consultation of the stock of a product at a warehouse, plus the internal reservation service used by the cart, logistics, and returns. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Register Inventory Movement | `registerMovement` / `registerDamagedStock` | `RegisterInventoryMovementService` |
| Consult Inventory | `consultInventory` / `consultMovementHistory` | `ConsultInventoryService` |
| — (internal) | — | `InventoryReservationService` |

**Invariants (entity `Inventory`):** no quantity (available, reserved, damaged) is ever negative; damaged stock can never be reserved; every change generates exactly one `InventoryMovement`, persisted in the movement history (MongoDB). Only `PhysicalProduct` instances have inventory.

**Stock access rule (`requireStockAccess`):** a seller may only operate on the stock of its own products; the other roles need access to the warehouse (administrators and supervisors: any warehouse; logistics operators: any warehouse).

---

## 2. Register Inventory Movement

### 2.1 Description

Records a manual change on the stock of a physical product at a warehouse. The movement type decides the effect.

| Movement type | Effect on the record | Creates the record if missing |
| ------------- | -------------------- | ----------------------------- |
| `STOCK_IN` | `available += quantity` | Yes |
| `RESERVATION` | available → reserved | No |
| `RESERVATION_RELEASE` | reserved → available | No |
| `ADJUSTMENT` | `available += quantity` (signed correction) | No |
| `RETURN` | `available += quantity` | No |
| `DAMAGE` | available → damaged | No |
| `SALE_EXIT` | Not allowed manually: it is recorded when a shipment is dispatched. | — |

### 2.2 Actor

`ADMINISTRATOR`, `LOGISTICS_OPERATOR`, `SELLER` (only stock of its own products).

### 2.3 Domain Service Method

```java
Inventory registerMovement(String requesterId, String productId, String warehouseId,
                           InventoryMovementType type, int quantity);
Inventory registerDamagedStock(String requesterId, String productId, String warehouseId, int quantity);
```

Role ports: `registerInventoryMovement` / `registerDamagedStock` in `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort`. REST: `POST .../inventory/movements`, `POST .../inventory/damaged`.

### 2.4 Input

| Field | Rule |
| ----- | ---- |
| `productId` | Existing **physical** product. |
| `warehouseId` | Existing warehouse. |
| `type` | Not null; `InventoryMovementType` code. |
| `quantity` | Not zero. Positive for every type except `ADJUSTMENT` (signed). |

### 2.5 Validations and Authorization (in order)

1. `type` not null, `quantity` not zero → `IllegalArgumentException`.
2. Requester has `REGISTER_INVENTORY_MOVEMENT`.
3. The product exists → `EntityNotFoundException`; it is physical → `IllegalArgumentException`.
4. The warehouse exists → `EntityNotFoundException`.
5. `requireStockAccess` → `OwnershipAccessDeniedException`.
6. For every type except `STOCK_IN`, an inventory record for (product, warehouse) exists → `IllegalArgumentException`.
7. Entity rules: positive quantity where required; not enough available stock to reserve or mark as damaged → `InsufficientInventoryException`; not enough reserved stock to release, or an adjustment that would make stock negative → `IllegalArgumentException`.

### 2.6 Domain Behavior

`STOCK_IN` on a missing record creates a new `Inventory` (0 / 0) linked to the product and the warehouse (`warehouse.addInventoryRecord`). Then the corresponding `Inventory` operation is applied (`stockIn`, `reserve`, `releaseReservation`, `adjust`, `reinstate`, `markDamaged`), which registers its movement.

### 2.7 Persistence

`InventoryRepository.save(inventory)` and one `InventoryMovementRepository.save(movement)` per pending movement.

### 2.8 Output

The updated `Inventory`.

### 2.9 Exceptions

`IllegalArgumentException`, `InvalidRoleAssignmentException`, `EntityNotFoundException`, `OwnershipAccessDeniedException`, `InsufficientInventoryException`.

### 2.10 Output Ports

`InventoryRepository`, `InventoryMovementRepository`, `ProductRepository`, `WarehouseRepository`.

### 2.11 Processing Flow

```text
validate type and quantity → require REGISTER_INVENTORY_MOVEMENT → load physical product → load warehouse
→ require stock access → STOCK_IN: find or create record | other: find record
→ apply the Inventory operation (registers the movement) → save record → save movements
```

---

## 3. Consult Inventory

### 3.1 Description

Retrieves the available, reserved, and damaged stock of a product at a warehouse.

### 3.2 Actor

`ADMINISTRATOR`, `SUPERVISOR`, `LOGISTICS_OPERATOR`, `SELLER` (only its own products).

### 3.3 Domain Service Method

```java
Inventory consultInventory(String requesterId, String productId, String warehouseId);
```

Role ports: `consultInventory` in `AdministratorPort`, `SupervisorPort`, `LogisticsOperatorPort`, `SellerPort`. REST: `GET .../inventory?productId=&warehouseId=`.

### 3.4 Validations and Authorization

1. Requester has `CONSULT_INVENTORY`.
2. The product and the warehouse exist → `EntityNotFoundException`.
3. `requireStockAccess` → `OwnershipAccessDeniedException`.
4. The inventory record exists → `IllegalArgumentException`.

### 3.5 Output

The `Inventory`. No state changes.

---

## 4. Inventory Reservation Service (internal)

`InventoryReservationService` is not exposed through any Role Input Port. It coordinates the stock of a product across **all its warehouses** on behalf of other services:

| Operation | Used by | Behavior |
| --------- | ------- | -------- |
| `reserveStock(product, quantity, performer)` | `ManageCartItemsService` | Reserves in the first record with enough available stock; otherwise `InsufficientInventoryException` (reports the best availability). |
| `releaseReservation(product, quantity, performer)` | `ManageCartItemsService` | Releases from the first record holding enough reserved units; otherwise `IllegalStateException`. |
| `confirmSale(product, quantity, performer)` | `UpdateShipmentStatusService` | Removes reserved units (`SALE_EXIT`) when the shipment is dispatched. |
| `reinstateStock(product, quantity, performer)` | `ResolveReturnService` | Returns units to the available stock (`RETURN`) when an approved return is completed. |

Every operation persists the record and its pending movements.

---

## 5. Consult Movement History

### 5.1 Description

Retrieves the movement history of the inventory record of a product at a warehouse, in chronological order (movement number).

### 5.2 Actor

`ADMINISTRATOR`, `SUPERVISOR`, `LOGISTICS_OPERATOR`, `SELLER` (only its own products).

### 5.3 Domain Service Method

```java
List<InventoryMovement> consultMovementHistory(String requesterId, String productId, String warehouseId);
```

Role ports: `consultInventoryMovements` in `AdministratorPort`, `SupervisorPort`, `LogisticsOperatorPort`, `SellerPort`. REST: `GET .../inventory/movements?productId=&warehouseId=`.

### 5.4 Validations and Authorization

Same as *Consult Inventory* (`CONSULT_INVENTORY`, existing product and warehouse, stock access, existing record).

### 5.5 Output

The list of `InventoryMovement` (type, quantity, date, person who performed it). No state changes.

### 5.6 Output Ports

`InventoryRepository`, `InventoryMovementRepository.findByInventory` (MongoDB collection `inventory_movements`; the performer is resolved through `PersonRepository`).
