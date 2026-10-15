# Billing Management Services

## 1. Purpose

This document specifies the generation and consultation of **invoices**. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Generate Invoice | `generateInvoice` | `GenerateInvoiceService` |
| Consult Invoice | `consultInvoice` | `ConsultInvoiceService` |

**Tax rule.** Catalog prices already include VAT. The invoiced total equals the order total and the tax amount is the VAT contained in it:

```text
netAmount = round(total / (1 + taxRate), 2, HALF_UP)
taxAmount = total − netAmount
```

The rate is configurable (`nexusmarket.billing.tax-rate`, environment variable `TAX_RATE`, default `0.19`). Example: total 199.80 → tax 31.90.

**Automatic invoicing.** Every order receives its invoice at checkout (`GenerateInvoiceService.issueInvoice`, called by `ConfirmOrderService`). *Generate Invoice* only covers orders that, exceptionally, do not have one yet.

---

## 2. Issue Invoice (internal operation)

### 2.1 Description

Creates and attaches the invoice of a confirmed order. It is a domain operation shared with the checkout; it does not persist by itself.

### 2.2 Signature

```java
Invoice issueInvoice(Order order);
```

### 2.3 Validations

1. `order` not null; it is not in `CART` status → `IllegalArgumentException`.
2. `Order.attachInvoice`: the order is not finalized (`OrderAlreadyFinalizedException`) and has no invoice yet (`IllegalArgumentException`).

### 2.4 Domain Behavior

`Invoice.issueFor(order, id, now, taxRate)` computes `totalAmount` and `taxAmount` with the tax rule above; the invoice is attached to the order.

---

## 3. Generate Invoice

### 3.1 Description

Generates the invoice of an order that has none.

### 3.2 Actor

`ADMINISTRATOR`.

### 3.3 Domain Service Method

```java
Invoice generateInvoice(String requesterId, String orderId);
```

Role port: `AdministratorPort.generateInvoice`. REST: `POST /api/v1/admin/orders/{orderId}/invoice`.

### 3.4 Validations and Authorization

1. Requester has `GENERATE_INVOICE`.
2. `orderId` not blank; the order exists → `EntityNotFoundException`.
3. The order has no invoice → `IllegalArgumentException`.
4. The rules of *Issue Invoice*.

### 3.5 Persistence and Notification

`OrderRepository.save(order)` (the invoice is stored with its order); notifies the buyer "Invoice issued".

### 3.6 Output

The new `Invoice`.

### 3.7 Output Ports

`OrderRepository`, `NotificationService`.

---

## 4. Consult Invoice

### 4.1 Description

Retrieves the invoice of an order.

### 4.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any order); `BUYER` (its own orders); `SELLER` (orders containing its products).

### 4.3 Domain Service Method

```java
Invoice consultInvoice(String requesterId, String orderId);
```

Role ports: `consultInvoice` in `AdministratorPort`, `SupervisorPort`, `BuyerPort`, `SellerPort`.

### 4.4 Validations and Authorization

1. Requester has `CONSULT_INVOICE`; the order exists.
2. `requireOrderAccess` → `OwnershipAccessDeniedException`.
3. The order has an invoice → `IllegalArgumentException`.

### 4.5 Output

The `Invoice`. No state changes.
