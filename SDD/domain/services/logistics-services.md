# Logistics Management Services

## 1. Purpose

This document specifies the creation, dispatch, delivery, and consultation of **shipments** and their tracking history. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Create Shipment | `createShipment` | `CreateShipmentService` |
| Update Shipment Status | `dispatchShipment` / `confirmDelivery` / `registerShipmentReturn` | `UpdateShipmentStatusService` |
| Consult Shipment | `consultShipment` / `consultTracking` | `ConsultShipmentService` |

**Shipment lifecycle (`ShipmentStatus`):** `PENDING → IN_TRANSIT → DELIVERED`, with `IN_TRANSIT → RETURNED → IN_TRANSIT` for a failed delivery that is dispatched again. Every step is recorded as a `ShipmentTrackingEvent` (`CREATED`, `DISPATCHED`, `RETURNED`, `DELIVERED`) in the tracking history (MongoDB).

**Operator scope.** A logistics operator only dispatches, delivers, and consults the shipments assigned to it (`requireOrderAccess`). Administrators operate on every shipment.

---

## 2. Create Shipment

### 2.1 Description

Creates the shipment of a paid order that contains physical products, sets the origin warehouse and the buyer's delivery address, and assigns the responsible logistics operator.

### 2.2 Actor

`ADMINISTRATOR` (chooses the operator); `LOGISTICS_OPERATOR` (the shipment is assigned to itself).

### 2.3 Domain Service Method

```java
Shipment createShipment(String requesterId, String orderId, String originWarehouseId,
                        String logisticsOperatorId);
```

Role ports: `AdministratorPort.createShipment`, `LogisticsOperatorPort.createShipment` (operator = the authenticated user). REST: `POST .../orders/{orderId}/shipment`.

### 2.4 Validations (in order)

1. Requester has `CREATE_SHIPMENT`.
2. The order exists → `EntityNotFoundException`; it is `PAID` → `IllegalArgumentException`.
3. The order contains physical products (digital-only orders need no shipment) → `IllegalArgumentException`.
4. The order has no shipment yet → `IllegalArgumentException`.
5. The buyer has a primary delivery address → `IllegalArgumentException`.
6. The origin warehouse exists → `EntityNotFoundException`.
7. The operator exists → `EntityNotFoundException`; it is a `LogisticsOperator` → `IllegalArgumentException`; it is active → `IllegalStateException`.

### 2.5 Domain Behavior

`new Shipment(id, order, operator, originWarehouse, buyer.primaryAddress)` in `PENDING`; `order.attachShipment(shipment)` (rejected for finalized orders); `operator.assignShipment(shipment)`.

### 2.6 Persistence and Notification

`OrderRepository.save(order)` (the shipment is stored with its order); tracking event `CREATED`; notifies the operator "Shipment assigned".

### 2.7 Output

The new `Shipment`.

### 2.8 Output Ports

`OrderRepository`, `WarehouseRepository`, `PersonRepository`, `ShipmentTrackingRepository`, `NotificationService`.

### 2.9 Processing Flow

```text
require CREATE_SHIPMENT → load order (PAID, physical, no shipment, address)
→ load warehouse → load active operator → create Shipment (PENDING)
→ attach to order → assign to operator → save order → track CREATED → notify operator
```

---

## 3. Dispatch Shipment

### 3.1 Description

Registers that the order left the warehouse: the shipment goes `IN_TRANSIT`, the order goes `SHIPPED`, and the reserved stock becomes a completed sale.

### 3.2 Actor

`ADMINISTRATOR`; `LOGISTICS_OPERATOR` (assigned shipments only).

### 3.3 Domain Service Method

```java
Shipment dispatchShipment(String requesterId, String orderId);
```

Role ports: `AdministratorPort.dispatchShipment`, `LogisticsOperatorPort.dispatchShipment`. REST: `POST .../orders/{orderId}/shipment/dispatch`.

### 3.4 Validations

1. Requester has `UPDATE_SHIPMENT_STATUS`; the order exists; it has a shipment → `IllegalArgumentException`.
2. Operator scope → `OwnershipAccessDeniedException`.
3. The order is `PAID`; the shipment is `PENDING` → `IllegalArgumentException`. A `RETURNED` shipment is dispatched again instead (see section 6.8).

### 3.5 Domain Behavior

`shipment.dispatch()`: order `SHIPPED`, shipment `IN_TRANSIT`, dispatch date set. For every physical item: `InventoryReservationService.confirmSale` (movement `SALE_EXIT`).

### 3.6 Persistence and Notification

`OrderRepository.save(order)`, inventory records and movements; tracking event `DISPATCHED`; notifies the buyer "Order shipped".

### 3.7 Output

The updated `Shipment`.

---

## 4. Confirm Delivery

### 4.1 Description

Registers the delivery of an in-transit shipment to the buyer. The order is finalized afterwards through *Finalize Order* (`order-services.md`).

### 4.2 Actor

`ADMINISTRATOR`; `LOGISTICS_OPERATOR` (assigned shipments only).

### 4.3 Domain Service Method

```java
Shipment confirmDelivery(String requesterId, String orderId);
```

Role ports: `AdministratorPort.confirmDelivery`, `LogisticsOperatorPort.confirmDelivery`. REST: `POST .../orders/{orderId}/shipment/delivery`.

### 4.4 Validations

1. Requester has `UPDATE_SHIPMENT_STATUS`; the order exists and has a shipment; operator scope.
2. The shipment is `IN_TRANSIT` → `IllegalArgumentException`.

### 4.5 Persistence and Notification

`shipment.markDelivered()` (delivery date set), `OrderRepository.save(order)`; tracking event `DELIVERED`; notifies the buyer "Shipment delivered".

### 4.6 Output

The updated `Shipment`.

---

## 5. Consult Shipment and Tracking

### 5.1 Description

Retrieves the shipment of an order, or its tracking history (oldest event first).

### 5.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any order); `BUYER` (its own orders); `SELLER` (orders containing its products); `LOGISTICS_OPERATOR` (assigned shipments).

### 5.3 Domain Service Method

```java
Shipment consultShipment(String requesterId, String orderId);
List<ShipmentTrackingEvent> consultTracking(String requesterId, String orderId);
```

Role ports: `consultShipment` / `consultTracking` in every authenticated role port.

### 5.4 Validations and Authorization

1. Requester has `CONSULT_SHIPMENT`; the order exists; `requireOrderAccess` → `OwnershipAccessDeniedException`.
2. The order has a shipment → `IllegalArgumentException`.

### 5.5 Output

The `Shipment`, or the list of `ShipmentTrackingEvent`. No state changes.

### 5.6 Output Ports

`OrderRepository`, `ShipmentTrackingRepository`.

---

## 6. Register Shipment Return (failed delivery)

### 6.1 Description

Registers that an in-transit shipment could not be delivered and came back to the origin warehouse. The goods stay allocated to the order: the order remains `SHIPPED`, its stock stays recorded as sold, and the shipment can be **dispatched again** with *Dispatch Shipment*.

### 6.2 Actor

`ADMINISTRATOR`; `LOGISTICS_OPERATOR` (assigned shipments only).

### 6.3 Domain Service Method

```java
Shipment registerShipmentReturn(String requesterId, String orderId, String reason);
```

Role ports: `AdministratorPort.registerShipmentReturn`, `LogisticsOperatorPort.registerShipmentReturn`. REST: `POST .../orders/{orderId}/shipment/return` with `{reason}`.

### 6.4 Validations

1. `reason` not blank → `IllegalArgumentException`.
2. Requester has `UPDATE_SHIPMENT_STATUS`; the order exists and has a shipment; operator scope → `OwnershipAccessDeniedException`.
3. The shipment is `IN_TRANSIT` → `IllegalArgumentException`.

### 6.5 Domain Behavior

`shipment.markReturned()` → `RETURNED`. While returned, the order cannot be finalized (its shipment is not delivered).

### 6.6 Persistence and Notification

`OrderRepository.save(order)`; tracking event `RETURNED` with the reason; notifies the buyer "Delivery failed".

### 6.7 Output

The returned `Shipment`.

### 6.8 Dispatching again

*Dispatch Shipment* on a `RETURNED` shipment calls `shipment.redispatch()` → `IN_TRANSIT` (new dispatch date), records a new `DISPATCHED` tracking event, and notifies the buyer "Order shipped again". It performs **no** order transition and **no** stock movement, because both already happened at the first dispatch.

Complete sequence of a failed delivery: `CREATED → DISPATCHED → RETURNED → DISPATCHED → DELIVERED`.
