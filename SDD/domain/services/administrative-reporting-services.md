# Administrative Reporting Services

## 1. Purpose

This document specifies the consolidated **read models** used for administrative review: the commercial report of the Marketplace and the performance report of a seller. Both are computed on demand, are immutable, and are not persisted. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Consult Commercial Report | `consultCommercialReport` | `ConsultCommercialReportService` |
| Consult Seller Performance Report | `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` |

**Paid orders.** Revenue and sales only count orders whose payment was confirmed: status `PAID`, `SHIPPED`, or `DELIVERED`.

---

## 2. Consult Commercial Report

### 2.1 Description

Consolidates orders, invoices, revenue, refunds, and inventory levels across every warehouse.

### 2.2 Actor

`ADMINISTRATOR`, `SUPERVISOR`.

### 2.3 Domain Service Method

```java
CommercialReport consultCommercialReport(String requesterId);
```

Role ports: `AdministratorPort.consultCommercialReport`, `SupervisorPort.consultCommercialReport`. REST: `GET /api/v1/admin/reports/commercial`, `GET /api/v1/supervisor/reports/commercial`.

### 2.4 Authorization

Requester has `CONSULT_COMMERCIAL_REPORT`.

### 2.5 Computation

| Field | Rule |
| ----- | ---- |
| `totalOrders` | Number of orders. |
| `ordersByStatus` | Number of orders per `OrderStatus` code. |
| `invoicesIssued` | Orders with an invoice. |
| `invoicedAmount` / `invoicedTaxAmount` | Sum of invoice totals / tax amounts. |
| `grossRevenue` | Sum of the totals of paid orders. |
| `refundedAmount` | Sum of the amounts of `PROCESSED` refunds. |
| `netRevenue` | `grossRevenue − refundedAmount`. |
| `inventoryLevels` | Available, reserved, and damaged stock of every product at every warehouse, sorted by warehouse and product. |
| `generatedAt` | Generation time. |

### 2.6 Output

`CommercialReport`. No state changes.

### 2.7 Output Ports

`OrderRepository`, `ReturnRepository`, `InventoryRepository`.

---

## 3. Consult Seller Performance Report

### 3.1 Description

Consolidates the products, orders, sales, and returns associated with a seller.

### 3.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any seller); `SELLER` (only itself).

### 3.3 Domain Service Method

```java
SellerPerformanceReport consultSellerPerformanceReport(String requesterId, String sellerId);
```

Role ports: `AdministratorPort` / `SupervisorPort.consultSellerPerformanceReport`, `SellerPort.consultMyPerformanceReport` (seller = the authenticated user). REST: `GET .../sellers/{sellerId}/performance-report`, `GET /api/v1/seller/reports/performance`.

### 3.4 Validations and Authorization

1. Requester has `CONSULT_SELLER_PERFORMANCE_REPORT`.
2. `sellerId` not blank; the seller exists → `EntityNotFoundException`.
3. `requireSellerAccess` (a seller only its own report) → `OwnershipAccessDeniedException`.

### 3.5 Computation

| Field | Rule |
| ----- | ---- |
| `totalProducts` / `productsByStatus` | Products of the seller, and their count per `ProductStatus` code. |
| `ordersCount` | Orders containing at least one product of the seller. |
| `unitsSold` / `salesAmount` | Quantity and subtotal of the seller's lines in paid orders. |
| `returnsCount` / `returnsByStatus` | Returns over those orders, and their count per `ReturnStatus`. |

### 3.6 Output

`SellerPerformanceReport`. No state changes.

### 3.7 Output Ports

`SellerRepository`, `ProductRepository`, `OrderRepository`, `ReturnRepository`.
