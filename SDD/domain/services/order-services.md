# Order Management Services

## 1. Purpose

This document specifies the order lifecycle: checkout of the cart, payment confirmation, finalization, and consultation. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Confirm Order | `checkout` | `ConfirmOrderService` |
| Update Order Status | `confirmPayment` / `finalizeOrder` | `UpdateOrderStatusService` |
| Consult Order | `consultOrder` / `listOrders` | `ConsultOrderService` |

**Lifecycle (`OrderStatus`):** `CART → PENDING_PAYMENT → PAID → SHIPPED → DELIVERED`. Transitions must follow this exact order; any other transition raises `IllegalArgumentException`. **A finalized order is an order in `DELIVERED` status**: any further change raises `OrderAlreadyFinalizedException` (HTTP 409).

---

## 2. Confirm Order (Checkout)

### 2.1 Description

Converts the buyer's shopping cart into a formal order in `PENDING_PAYMENT`, issues its invoice, and consumes the cart.

### 2.2 Actor

`BUYER`.

### 2.3 Domain Service Method

```java
Order checkout(String buyerId, String cartId);
```

`cartId` is optional: `null` checks out the buyer's active cart. Role port: `BuyerPort.checkout`. REST: `POST /api/v1/buyer/orders`.

### 2.4 Validations (in order)

1. `buyerId` not blank → `IllegalArgumentException`.
2. The buyer exists → `EntityNotFoundException`; is active → `IllegalStateException`.
3. The buyer's commercial status is `ACTIVE` → `BuyerNotAuthorizedException`.
4. The cart is resolved: by `cartId` (must exist → `EntityNotFoundException`, and belong to the buyer → `IllegalArgumentException`) or the active cart (must exist → `IllegalArgumentException`).
5. The cart is not empty → `IllegalArgumentException`.

### 2.5 Domain Behavior

1. `Order.fromCart(cart)`: one `OrderItem` per cart item, at the price captured in the cart; the total is recalculated. The order starts in `CART`.
2. `order.confirm()` → `PENDING_PAYMENT`. The stock reserved while the items were in the cart becomes the reservation of the order (no new movement).
3. `GenerateInvoiceService.issueInvoice(order)` attaches the invoice (see `billing-services.md`).
4. `buyer.addOrder(order)`, `cart.clear()`, `buyer.detachCart()`.

### 2.6 Persistence and Notification

`OrderRepository.save(order)`, `BuyerRepository.save(buyer)`, `ShoppingCartRepository.delete(cart)`; notifies the buyer "Order confirmed".

### 2.7 Output

The new `Order` (`PENDING_PAYMENT`, with its invoice).

### 2.8 Exceptions

`IllegalArgumentException`, `EntityNotFoundException`, `IllegalStateException`, `BuyerNotAuthorizedException`.

### 2.9 Output Ports

`BuyerRepository`, `ShoppingCartRepository`, `OrderRepository`, `NotificationService`.

### 2.10 Processing Flow

```text
load active buyer → require commercial ACTIVE → resolve cart → cart not empty
→ Order.fromCart → confirm (PENDING_PAYMENT) → issue invoice
→ save order → buyer.addOrder → save buyer → clear, detach and delete cart → notify
```

---

## 3. Confirm Payment

### 3.1 Description

Registers the financial confirmation of an order. Orders made **only of digital products** are delivered immediately: they go through `PAID → SHIPPED → DELIVERED` at once, without a shipment.

### 3.2 Actor

`ADMINISTRATOR`.

### 3.3 Domain Service Method

```java
Order confirmPayment(String requesterId, String orderId);
```

Role port: `AdministratorPort.confirmOrderPayment`. REST: `POST /api/v1/admin/orders/{orderId}/payment`.

### 3.4 Validations and Authorization

1. Requester has `CONFIRM_ORDER_PAYMENT`.
2. `orderId` not blank; the order exists → `EntityNotFoundException`.
3. The order is not finalized → `OrderAlreadyFinalizedException`; it is in `PENDING_PAYMENT` → `IllegalArgumentException`.

### 3.5 Domain Behavior

`order.registerPayment()`: `PAID`; if no item requires physical dispatch, also `SHIPPED` and `DELIVERED`.

### 3.6 Persistence and Notification

`OrderRepository.save(order)`; notifies the buyer "Payment confirmed" (preparation begins, or digital products delivered).

### 3.7 Output

The updated `Order`.

---

## 4. Finalize Order

### 4.1 Description

Finalizes an order whose shipment has been delivered (`SHIPPED → DELIVERED`). From then on the order cannot be modified.

### 4.2 Actor

`ADMINISTRATOR` (any order); `LOGISTICS_OPERATOR` (only orders whose shipment is assigned to it).

### 4.3 Domain Service Method

```java
Order finalizeOrder(String requesterId, String orderId);
```

Role ports: `AdministratorPort.finalizeOrder`, `LogisticsOperatorPort.finalizeOrder`. REST: `POST .../orders/{orderId}/finalization`.

### 4.4 Validations and Authorization

1. Requester has `FINALIZE_ORDER`; the order exists.
2. A logistics operator must be the one assigned to the shipment → `OwnershipAccessDeniedException`.
3. The order is not already finalized → `OrderAlreadyFinalizedException`.
4. The order has a shipment in `DELIVERED` status → `IllegalArgumentException`.

### 4.5 Persistence and Notification

`order.finalizeOrder()`, `OrderRepository.save(order)`; notifies the buyer "Order delivered".

### 4.6 Output

The finalized `Order` (`DELIVERED`).

---

## 5. Consult Order

### 5.1 Description

Retrieves an order with its items, invoice, and shipment.

### 5.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any order); `BUYER` (its own orders); `SELLER` (orders containing its products); `LOGISTICS_OPERATOR` (orders whose shipment is assigned to it).

### 5.3 Domain Service Method

```java
Order consultOrder(String requesterId, String orderId);
```

Role ports: `consultOrder` in every authenticated role port.

### 5.4 Validations and Authorization

1. Requester has `CONSULT_ORDER`; the order exists → `EntityNotFoundException`.
2. `requireOrderAccess` (rules above) → `OwnershipAccessDeniedException`.

### 5.5 Output

The `Order`. No state changes. Output port: `OrderRepository`.

---

## 6. List Orders

### 6.1 Description

Lists the orders the requester may access, newest first, optionally filtered by status.

### 6.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (all orders); `BUYER` (its own orders); `SELLER` (orders containing its products); `LOGISTICS_OPERATOR` (orders whose shipment is assigned to it).

### 6.3 Domain Service Method

```java
List<Order> listOrders(String requesterId, OrderStatus status);
```

`status` is optional. Role ports: `AdministratorPort.listOrders`, `SupervisorPort.listOrders`, `SellerPort.consultMyOrders`, `LogisticsOperatorPort.consultAssignedOrders` (buyers use `BuyerPort.consultMyOrders`, see `buyer-services.md`). REST: `GET .../orders?status=`.

### 6.4 Authorization and Output

Requester has `CONSULT_ORDER`; every order is filtered with `canAccessOrder` and by status. An unknown status code returns `400`. Output port: `OrderRepository.findAll`.
