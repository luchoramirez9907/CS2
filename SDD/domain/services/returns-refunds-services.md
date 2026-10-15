# Returns and Refunds Management Services

## 1. Purpose

This document specifies the post-sale processes: **return requests** over delivered orders, their resolution and completion, and the **refunds** of approved returns. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Request Return | `requestReturn` | `RequestReturnService` |
| Resolve Return | `approveReturn`, `rejectReturn`, `completeReturn`, `consultReturn`, `listReturns` | `ResolveReturnService` |
| Process Refund | `processRefund` / `rejectRefund` / `consultRefund` | `ProcessRefundService` |

**Return lifecycle (`ReturnStatus`):**

```text
REQUESTED ──approve──> APPROVED ──complete──> COMPLETED
    │
    └──reject──> REJECTED
```

**Refund lifecycle (`RefundStatus`):** a refund is created and decided in the same operation: `PENDING → PROCESSED` (*Process Refund*) or `PENDING → REJECTED` (*Reject Refund*). A return generates at most one refund, and only from `APPROVED` or `COMPLETED` status.

Only the **administrator** resolves returns and approves refunds; the supervisor is read-only.

---

## 2. Request Return

### 2.1 Description

Creates a return request over a delivered order, with the reason given by the buyer.

### 2.2 Actor

`BUYER` (only over its own orders).

### 2.3 Domain Service Method

```java
Return requestReturn(String buyerId, String orderId, String reason);
```

Role port: `BuyerPort.requestReturn`. REST: `POST /api/v1/buyer/orders/{orderId}/returns`.

### 2.4 Validations (in order)

1. `buyerId`, `orderId`, `reason` not blank → `IllegalArgumentException`.
2. The buyer exists → `EntityNotFoundException`; is active → `IllegalStateException`.
3. The order exists → `EntityNotFoundException`.
4. The order was placed by that buyer → `ReturnNotAllowedException`.
5. The order is `DELIVERED` → `ReturnNotAllowedException`.

### 2.5 Domain Behavior

`Return.request(order, buyer, reason, id, now)` in `REQUESTED`; `order.addReturn(return)`. An order may have several return requests.

### 2.6 Persistence and Notification

`ReturnRepository.save(return)`; notifies the buyer "Return requested".

### 2.7 Output

The new `Return`.

### 2.8 Output Ports

`BuyerRepository`, `OrderRepository`, `ReturnRepository`, `NotificationService`.

---

## 3. Approve Return

### 3.1 Description

Approves a requested return, allowing its refund and its completion.

### 3.2 Actor

`ADMINISTRATOR`.

### 3.3 Domain Service Method

```java
Return approveReturn(String administratorId, String returnId);
```

Role port: `AdministratorPort.approveReturn`. REST: `POST /api/v1/admin/returns/{returnId}/approval`.

### 3.4 Validations and Authorization

1. `returnId` not blank; requester has `RESOLVE_RETURN`.
2. The return exists → `EntityNotFoundException`.
3. The return is `REQUESTED` → `IllegalArgumentException`.

### 3.5 Persistence and Notification

`return.approve(administrator)` → `APPROVED`; `ReturnRepository.save`; notifies the buyer "Return approved".

### 3.6 Output

The approved `Return`.

---

## 4. Reject Return

### 4.1 Description

Rejects a requested return. A rejected return cannot be refunded nor completed.

### 4.2 Actor

`ADMINISTRATOR`.

### 4.3 Domain Service Method

```java
Return rejectReturn(String administratorId, String returnId);
```

Role port: `AdministratorPort.rejectReturn`. REST: `POST /api/v1/admin/returns/{returnId}/rejection`.

### 4.4 Validations, Persistence and Output

Same as *Approve Return*; `return.reject()` → `REJECTED`; notifies the buyer "Return rejected". Returns the rejected `Return`.

---

## 5. Complete Return

### 5.1 Description

Closes an approved return once the products have been received back: every physical item of the order returns to the available stock.

### 5.2 Actor

`ADMINISTRATOR`.

### 5.3 Domain Service Method

```java
Return completeReturn(String administratorId, String returnId);
```

Role port: `AdministratorPort.completeReturn`. REST: `POST /api/v1/admin/returns/{returnId}/completion`.

### 5.4 Validations and Authorization

1. `returnId` not blank; requester has `RESOLVE_RETURN`; the return exists.
2. The return is `APPROVED` → `IllegalArgumentException`.

### 5.5 Domain Behavior

`return.complete()` → `COMPLETED`. For every physical item of the order: `InventoryReservationService.reinstateStock(product, quantity, administrator)` (movement `RETURN` on the product's inventory record).

### 5.6 Persistence and Notification

`ReturnRepository.save(return)`, inventory records and movements; notifies the buyer "Return completed".

### 5.7 Output

The completed `Return`.

### 5.8 Output Ports

`ReturnRepository`, `InventoryRepository`, `InventoryMovementRepository`, `NotificationService`.

---

## 6. Consult Return

### 6.1 Description

Retrieves a return request.

### 6.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any return); `BUYER` (returns over its own orders); `SELLER` (returns over orders containing its products).

### 6.3 Domain Service Method

```java
Return consultReturn(String requesterId, String returnId);
```

Role ports: `consultReturn` in `AdministratorPort`, `SupervisorPort`, `BuyerPort`, `SellerPort`.

### 6.4 Validations and Authorization

Requester has `CONSULT_RETURN`; the return exists; `requireOrderAccess` over the order of the return → `OwnershipAccessDeniedException`.

### 6.5 Output

The `Return`. No state changes.

---

## 7. Process Refund

### 7.1 Description

Creates and processes the monetary reimbursement of an approved return.

### 7.2 Actor

`ADMINISTRATOR`.

### 7.3 Domain Service Method

```java
Refund processRefund(String approverId, String returnId, BigDecimal amount);
```

`amount` is optional: `null` refunds the total of the order. Role port: `AdministratorPort.processRefund`. REST: `POST /api/v1/admin/returns/{returnId}/refund`.

### 7.4 Validations (in order)

1. Requester has `PROCESS_REFUND` (administrator only).
2. The return exists → `EntityNotFoundException`.
3. The amount does not exceed the order total → `IllegalArgumentException`; it is positive → `IllegalArgumentException`.
4. The return is `APPROVED` or `COMPLETED` → `RefundNotAllowedException`.
5. The return has no refund yet → `IllegalArgumentException`.

### 7.5 Domain Behavior

`Refund.issueFor(return, amount, approvedBy = administrator, id, now)` attaches the refund to the return; `refund.process()` → `PROCESSED`. `Refund.approvedBy` is typed as `Administrator`, so no other role can approve a refund.

### 7.6 Persistence and Notification

`ReturnRepository.save(return)` (the refund is stored with its return); notifies the buyer "Refund processed".

### 7.7 Output

The processed `Refund`.

### 7.8 Output Ports

`ReturnRepository`, `NotificationService`.

### 7.9 Processing Flow

```text
require PROCESS_REFUND → load return → amount = given or order total (≤ total)
→ return APPROVED/COMPLETED and without refund → issue refund (approvedBy = administrator)
→ process → save return → notify
```

---

## 8. Consult Refund

### 8.1 Description

Retrieves the refund of a return.

### 8.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any refund); `BUYER` (only refunds of its own returns).

### 8.3 Domain Service Method

```java
Refund consultRefund(String requesterId, String returnId);
```

Role ports: `consultRefund` in `AdministratorPort`, `SupervisorPort`, `BuyerPort`.

### 8.4 Validations and Authorization

Requester has `CONSULT_REFUND`; the return exists; `requireBuyerAccess` over the buyer of the return → `OwnershipAccessDeniedException`; the return has a refund → `IllegalArgumentException`.

### 8.5 Output

The `Refund`. No state changes.

---

## 9. Reject Refund

### 9.1 Description

Records the decision of **not** reimbursing an approved return (for example, after inspecting the returned products). A refund in `REJECTED` status is attached to the return, so it can no longer be refunded.

### 9.2 Actor

`ADMINISTRATOR`.

### 9.3 Domain Service Method

```java
Refund rejectRefund(String approverId, String returnId);
```

Role port: `AdministratorPort.rejectRefund`. REST: `POST /api/v1/admin/returns/{returnId}/refund/rejection`.

### 9.4 Validations (in order)

1. Requester has `PROCESS_REFUND`.
2. The return exists → `EntityNotFoundException`.
3. The return is `APPROVED` or `COMPLETED` → `RefundNotAllowedException`.
4. The return has no refund yet → `IllegalArgumentException`.

### 9.5 Domain Behavior

`Refund.issueFor(return, orderTotal, approvedBy = administrator, id, now)` followed by `refund.reject()` → `REJECTED`. The order total is kept as the claimed amount for traceability; only `PROCESSED` refunds count as refunded money in the reports.

### 9.6 Persistence and Notification

`ReturnRepository.save(return)`; notifies the buyer "Refund rejected".

### 9.7 Output

The rejected `Refund`.

---

## 10. List Returns

### 10.1 Description

Lists the return requests the requester may access, newest first, optionally filtered by status.

### 10.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (all returns); `SELLER` (returns over orders containing its products); `BUYER` (its own returns, through `consultBuyerReturns`).

### 10.3 Domain Service Method

```java
List<Return> listReturns(String requesterId, ReturnStatus status);
```

`status` is optional. Role ports: `AdministratorPort.listReturns`, `SupervisorPort.listReturns`, `SellerPort.consultMyReturns`. REST: `GET .../returns?status=`.

### 10.4 Authorization and Output

Requester has `CONSULT_RETURN`; every return is filtered with `canAccessOrder` over its order and by status. Output port: `ReturnRepository.findAll`.
