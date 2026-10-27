# Warehouse Management Services

## 1. Purpose

This document specifies the registration, consultation, and update of **warehouses**: `MarketplaceWarehouse` (owned by the Marketplace, managed by an administrator) and `SellerWarehouse` (owned by a seller). General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Register Warehouse | `registerWarehouse` | `RegisterWarehouseService` |
| Manage Warehouse | `consultWarehouse` / `updateWarehouse` / `listWarehouses` | `ManageWarehouseService` |

---

## 2. Register Warehouse

### 2.1 Description

Creates a warehouse. The ownership decides the specialization: without an owner seller it is a `MarketplaceWarehouse` managed by the registering administrator; with an owner seller it is a `SellerWarehouse` of that seller.

### 2.2 Actor

`ADMINISTRATOR` (Marketplace or seller warehouses); `SELLER` (only warehouses it owns).

### 2.3 Domain Service Method

```java
Warehouse registerWarehouse(String requesterId, String identifier, String name, Address address,
                            String ownerSellerId);
```

Role ports: `AdministratorPort.registerWarehouse` (`ownerSellerId` optional), `SellerPort.registerWarehouse` (owner = the authenticated seller). REST: `POST /api/v1/admin/warehouses`, `POST /api/v1/seller/warehouses`.

### 2.4 Input

| Field | Rule |
| ----- | ---- |
| `identifier` | Required, not blank, unique among warehouses. |
| `name` | Required, not blank. |
| `address` | Required `Address`. |
| `ownerSellerId` | Optional. `null` → Marketplace warehouse. |

### 2.5 Validations and Authorization (in order)

1. `identifier` not blank → `IllegalArgumentException`.
2. Requester has `REGISTER_WAREHOUSE` (administrator or seller, active).
3. No warehouse with the same identifier → `DuplicateResourceException` (409).
4. **Marketplace warehouse** (`ownerSellerId` empty): the requester must be an administrator → `InvalidRoleAssignmentException`.
5. **Seller warehouse**: the owner seller exists → `EntityNotFoundException`; is active → `IllegalStateException`; `requireSellerAccess` (a seller can only register warehouses for itself) → `OwnershipAccessDeniedException`.
6. `name` not blank and `address` not null (warehouse constructor) → `IllegalArgumentException`.

### 2.6 Domain Behavior

- `new MarketplaceWarehouse(identifier, name, address, managedBy = administrator)`, or
- `new SellerWarehouse(identifier, name, address, owner = seller)` and `seller.addWarehouse(warehouse)`.

### 2.7 Persistence

`WarehouseRepository.save(warehouse)`.

### 2.8 Output

The registered `Warehouse`.

### 2.9 Exceptions

`IllegalArgumentException`, `InvalidRoleAssignmentException`, `EntityNotFoundException`, `IllegalStateException`, `OwnershipAccessDeniedException`.

### 2.10 Output Ports

`WarehouseRepository`, `SellerRepository`, `PersonRepository` (through `ValidatePermissionsService`).

### 2.11 Processing Flow

```text
validate identifier → require REGISTER_WAREHOUSE → require unique identifier
→ no owner: require ADMINISTRATOR → MarketplaceWarehouse(managedBy = requester)
→ owner:    load seller (exists, active) → require seller access → SellerWarehouse(owner) → seller.addWarehouse
→ save
```

---

## 3. Consult Warehouse

### 3.1 Description

Retrieves the information of a warehouse.

### 3.2 Actor

`ADMINISTRATOR`, `SUPERVISOR`, `LOGISTICS_OPERATOR` (any warehouse); `SELLER` (only its own warehouses).

### 3.3 Domain Service Method

```java
Warehouse consultWarehouse(String requesterId, String warehouseId);
```

Role ports: `consultWarehouse` in `AdministratorPort`, `SupervisorPort`, `LogisticsOperatorPort`, `SellerPort`.

### 3.4 Validations and Authorization

1. Requester has `CONSULT_WAREHOUSE`.
2. `warehouseId` not blank; the warehouse exists → `EntityNotFoundException`.
3. `requireWarehouseAccess`: administrative roles and logistics operators access every warehouse; a seller only the `SellerWarehouse` it owns → `OwnershipAccessDeniedException`.

### 3.5 Output

The `Warehouse`. Output port: `WarehouseRepository`.

---

## 4. Update Warehouse

### 4.1 Description

Updates the name and the address of a warehouse. The identifier and the ownership never change.

### 4.2 Actor

`ADMINISTRATOR` (any warehouse); `SELLER` (only its own warehouses).

### 4.3 Domain Service Method

```java
Warehouse updateWarehouse(String requesterId, String warehouseId, String name, Address address);
```

`address` is optional: `null` keeps the current location. Role ports: `AdministratorPort.updateWarehouse`, `SellerPort.updateWarehouse`.

### 4.4 Validations and Authorization

1. Requester has `UPDATE_WAREHOUSE`; the warehouse exists; `requireWarehouseAccess`.
2. `name` not blank → `IllegalArgumentException`.

### 4.5 Persistence

`Warehouse.updateInformation(name, address)`, `WarehouseRepository.save(warehouse)`.

### 4.6 Output

The updated `Warehouse`.

---

## 5. List Warehouses

### 5.1 Description

Lists the warehouses the requester may access, sorted by identifier.

### 5.2 Actor

`ADMINISTRATOR`, `SUPERVISOR`, `LOGISTICS_OPERATOR` (every warehouse); `SELLER` (only its own warehouses).

### 5.3 Domain Service Method

```java
List<Warehouse> listWarehouses(String requesterId);
```

Role ports: `AdministratorPort.listWarehouses`, `SupervisorPort.listWarehouses`, `LogisticsOperatorPort.listWarehouses`, `SellerPort.consultMyWarehouses`. REST: `GET .../warehouses`.

### 5.4 Authorization and Output

Requester has `CONSULT_WAREHOUSE`; every warehouse is filtered with `canAccessWarehouse`. Returns the list (possibly empty). Output port: `WarehouseRepository.findAll`.
