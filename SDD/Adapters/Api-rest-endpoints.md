# API REST Endpoints Specification & Contracts

## 1. Overview

The REST API exposes the Role Input Ports (`domain/Input-ports.md`) over HTTP. Every route prefix belongs to exactly one role port, and the security configuration only lets the corresponding role use it.

| Prefix | Role Input Port | Controller | Access |
| ------ | --------------- | ---------- | ------ |
| `/api/v1/auth` | `PublicAccessPort` (authentication) | `AuthRestController` | `login`: public · `logout`, `password`: any authenticated role |
| `/api/v1/public` | `PublicAccessPort` | `PublicAccessRestController` | Public |
| `/api/v1/buyer` | `BuyerPort` | `BuyerRestController` | `BUYER` |
| `/api/v1/seller` | `SellerPort` | `SellerRestController` | `SELLER` |
| `/api/v1/operator` | `LogisticsOperatorPort` | `LogisticsOperatorRestController` | `LOGISTICS_OPERATOR` |
| `/api/v1/admin` | `AdministratorPort` | `AdministratorRestController` | `ADMINISTRATOR` |
| `/api/v1/supervisor` | `SupervisorPort` | `SupervisorRestController` | `SUPERVISOR` (GET only) |
| `/actuator/health` | — | Spring Boot Actuator | Public |

Any other route is denied.

---

## 2. Global Conventions

### Headers

| Header | Direction | Description |
| ------ | --------- | ----------- |
| `Authorization: Bearer <token>` | Request | Access token returned by `POST /api/v1/auth/login`. Required on every non-public route. |
| `Content-Type: application/json` | Request | Required on requests with a body. |
| `X-Request-Id` | Request / Response | Optional correlation id (1–64 chars `[A-Za-z0-9._-]`); generated when absent or invalid, always returned. |

### Success envelope

```json
{ "success": true, "message": "Order confirmed", "data": { ... } }
```

### Error envelope

See `Global-exception-handler.md`:

```json
{ "timestamp": "...", "status": 404, "code": "RESOURCE_NOT_FOUND", "message": "Order 'x' does not exist",
  "path": "/api/v1/admin/orders/x", "requestId": "req-...", "details": null }
```

### Status codes

`200` success · `201` resource created · `204` success without body · `400` invalid request · `401` missing/invalid token or invalid credentials · `403` role or ownership not allowed · `404` resource not found · `409` duplicate or invalid state transition · `422` business rule violation · `503` dependency unavailable · `500` unexpected error.

### Catalog values

Status and type fields are exchanged as the exact catalog **code** of the domain Value Object (`ACTIVE`, `SUSPENDED`, `RESERVATION_RELEASE`, `LOGISTICS_OPERATOR`, …). Unknown codes return `400`.

---

## 3. Authentication (`/api/v1/auth`)

| Method | Path | Request body | Response `data` | Status |
| ------ | ---- | ------------ | --------------- | ------ |
| POST | `/login` | `LoginRequest {email, password}` | `LoginResponse {token, tokenType, expiresIn, user}` | 200 · 401 `INVALID_CREDENTIALS` · 403 disabled account |
| POST | `/logout` | — | — | 204 (every previous token becomes invalid) |
| PUT | `/password` | `ChangePasswordRequest {currentPassword, newPassword}` | — | 204 · 401 wrong current password · 422 weak password |

The token is a JWT signed with HS256 whose claims are only `sub` (person identifier), `jti`, `ver` (token version), `iat` and `exp`.

---

## 4. Public Access (`/api/v1/public`)

| Method | Path | Request body | Response `data` | Status |
| ------ | ---- | ------------ | --------------- | ------ |
| POST | `/buyers` | `RegisterBuyerRequest {identifier, fullName, email, primaryAddress, password}` | `BuyerResponse` | 201 · 409 duplicate identifier/email |
| GET | `/products/{productId}` | — | `ProductResponse` (published products only) | 200 · 400 not available |
| GET | `/products?q=&sellerId=` | — (both filters optional) | `List<ProductResponse>` (published only, by name) | 200 |

---

## 5. Buyer (`/api/v1/buyer`, role `BUYER`)

| Method | Path | Request body | Response `data` | Status |
| ------ | ---- | ------------ | --------------- | ------ |
| GET | `/profile` | — | `BuyerResponse` | 200 |
| PUT | `/profile` | `UpdateBuyerRequest {fullName, email, primaryAddress?, additionalAddresses?}` | `BuyerResponse` | 200 |
| GET | `/products/{productId}` | — | `ProductResponse` | 200 |
| GET | `/cart` | — | `CartResponse` or `null` when there is no active cart | 200 |
| POST | `/cart/items` | `AddCartItemRequest {productId, quantity}` (price taken from the catalog) | `CartResponse` | 201 · 422 insufficient inventory |
| PUT | `/cart/items/{productId}` | `UpdateCartItemRequest {quantity}` | `CartResponse` | 200 |
| DELETE | `/cart/items/{productId}` | — | `CartResponse` | 200 |
| DELETE | `/cart` | — | `CartResponse` | 200 · 400 no active cart |
| POST | `/orders` | — (checks out the active cart) | `OrderResponse` (`PENDING_PAYMENT`) | 201 |
| GET | `/orders` | — | `List<OrderResponse>` | 200 |
| GET | `/orders/{orderId}` | — | `OrderResponse` | 200 · 403 not owner · 404 |
| GET | `/orders/{orderId}/invoice` | — | `InvoiceResponse` | 200 |
| GET | `/orders/{orderId}/shipment` | — | `ShipmentResponse` | 200 |
| GET | `/orders/{orderId}/tracking` | — | `List<TrackingEventResponse>` | 200 |
| POST | `/orders/{orderId}/returns` | `RequestReturnRequest {reason}` | `ReturnResponse` | 201 · 422 order not delivered |
| GET | `/returns` | — | `List<ReturnResponse>` | 200 |
| GET | `/returns/{returnId}` | — | `ReturnResponse` | 200 |
| GET | `/returns/{returnId}/refund` | — | `RefundResponse` | 200 |
| GET | `/refunds` | — | `List<RefundResponse>` | 200 |

---

## 6. Seller (`/api/v1/seller`, role `SELLER`)

| Method | Path | Request body | Response `data` | Status |
| ------ | ---- | ------------ | --------------- | ------ |
| GET | `/profile` | — | `SellerResponse` | 200 |
| PUT | `/profile` | `UpdatePersonRequest {fullName, email}` | `SellerResponse` | 200 |
| POST | `/warehouses` | `RegisterWarehouseRequest {identifier, name, address}` (owner = seller) | `WarehouseResponse` | 201 |
| GET | `/warehouses/{warehouseId}` | — | `WarehouseResponse` | 200 |
| PUT | `/warehouses/{warehouseId}` | `UpdateWarehouseRequest {name, address?}` | `WarehouseResponse` | 200 |
| POST | `/products` | `PublishProductRequest {identifier, name, price, description?, digital, digitalDeliveryDetails?, variants?}` | `ProductResponse` | 201 |
| GET | `/products/{productId}` | — | `ProductResponse` | 200 |
| PUT | `/products/{productId}` | `UpdateProductRequest {name, description?, price?, variants?}` | `ProductResponse` | 200 |
| PATCH | `/products/{productId}/status` | `ChangeStatusRequest {status}` (`ProductStatus` code) | `ProductResponse` | 200 |
| POST | `/inventory/movements` | `InventoryMovementRequest {productId, warehouseId, movementType, quantity}` | `InventoryResponse` | 201 · 422 insufficient inventory |
| POST | `/inventory/damaged` | `DamagedStockRequest {productId, warehouseId, quantity}` | `InventoryResponse` | 201 |
| GET | `/inventory?productId=&warehouseId=` | — | `InventoryResponse` | 200 |
| GET | `/orders/{orderId}` | — | `OrderResponse` (orders containing the seller's products) | 200 |
| GET | `/orders/{orderId}/invoice` | — | `InvoiceResponse` | 200 |
| GET | `/orders/{orderId}/shipment` | — | `ShipmentResponse` | 200 |
| GET | `/orders/{orderId}/tracking` | — | `List<TrackingEventResponse>` | 200 |
| GET | `/returns/{returnId}` | — | `ReturnResponse` | 200 |
| GET | `/reports/performance` | — | `SellerPerformanceReportResponse` | 200 |
| GET | `/products` | — | `List<ProductResponse>` (every status) | 200 |
| GET | `/warehouses` | — | `List<WarehouseResponse>` (own warehouses) | 200 |
| GET | `/orders?status=` | — | `List<OrderResponse>` (orders containing the seller's products) | 200 · 400 unknown status |
| GET | `/returns?status=` | — | `List<ReturnResponse>` | 200 |
| GET | `/inventory/movements?productId=&warehouseId=` | — | `List<InventoryMovementResponse>` (chronological) | 200 |

---

## 7. Logistics Operator (`/api/v1/operator`, role `LOGISTICS_OPERATOR`)

| Method | Path | Request body | Response `data` | Status |
| ------ | ---- | ------------ | --------------- | ------ |
| GET | `/profile` | — | `UserResponse` | 200 |
| PUT | `/profile` | `UpdatePersonRequest {fullName, email}` | `UserResponse` | 200 |
| GET | `/warehouses/{warehouseId}` | — | `WarehouseResponse` | 200 |
| GET | `/products/{productId}` | — | `ProductResponse` | 200 |
| POST | `/inventory/movements` | `InventoryMovementRequest` | `InventoryResponse` | 201 |
| POST | `/inventory/damaged` | `DamagedStockRequest` | `InventoryResponse` | 201 |
| GET | `/inventory?productId=&warehouseId=` | — | `InventoryResponse` | 200 |
| POST | `/orders/{orderId}/shipment` | `CreateShipmentRequest {originWarehouseId}` (operator = user) | `ShipmentResponse` | 201 · 400 order not paid |
| POST | `/orders/{orderId}/shipment/dispatch` | — (also dispatches again a `RETURNED` shipment) | `ShipmentResponse` | 200 |
| POST | `/orders/{orderId}/shipment/delivery` | — | `ShipmentResponse` | 200 |
| GET | `/orders/{orderId}/shipment` | — | `ShipmentResponse` | 200 |
| GET | `/orders/{orderId}/tracking` | — | `List<TrackingEventResponse>` | 200 |
| GET | `/orders/{orderId}` | — | `OrderResponse` | 200 |
| POST | `/orders/{orderId}/finalization` | — | `OrderResponse` (`DELIVERED`) | 200 · 409 already finalized |
| GET | `/warehouses` | — | `List<WarehouseResponse>` | 200 |
| GET | `/orders?status=` | — | `List<OrderResponse>` (assigned shipments) | 200 |
| POST | `/orders/{orderId}/shipment/return` | `ShipmentReturnRequest {reason}` | `ShipmentResponse` (`RETURNED`) | 200 · 400 not in transit |
| GET | `/inventory/movements?productId=&warehouseId=` | — | `List<InventoryMovementResponse>` (chronological) | 200 |

---

## 8. Administrator (`/api/v1/admin`, role `ADMINISTRATOR`)

| Method | Path | Request body | Response `data` | Status |
| ------ | ---- | ------------ | --------------- | ------ |
| POST | `/users` | `RegisterUserRequest {identifier, fullName, email, role, password}` | `UserResponse` | 201 · 409 duplicate |
| GET | `/users/{userId}` | — | `UserResponse` | 200 |
| PUT | `/users/{userId}` | `UpdatePersonRequest` | `UserResponse` | 200 |
| PATCH | `/users/{userId}/status` | `ChangeStatusRequest` (`UserStatus` code) | `UserResponse` | 200 |
| POST | `/sellers` | `RegisterSellerRequest {identifier, fullName, email, password}` | `SellerResponse` | 201 · 409 duplicate |
| GET | `/sellers/{sellerId}` | — | `SellerResponse` | 200 |
| PUT | `/sellers/{sellerId}` | `UpdatePersonRequest` | `SellerResponse` | 200 |
| PATCH | `/sellers/{sellerId}/status` | `ChangeStatusRequest` (`UserStatus` code) | `SellerResponse` | 200 |
| GET | `/sellers/{sellerId}/performance-report` | — | `SellerPerformanceReportResponse` | 200 |
| POST | `/buyers` | `RegisterBuyerRequest` | `BuyerResponse` | 201 |
| GET | `/buyers/{buyerId}` | — | `BuyerResponse` | 200 |
| PUT | `/buyers/{buyerId}` | `UpdateBuyerRequest` | `BuyerResponse` | 200 |
| PATCH | `/buyers/{buyerId}/commercial-status` | `ChangeStatusRequest` (`BuyerCommercialStatus` code) | `BuyerResponse` | 200 |
| GET | `/buyers/{buyerId}/orders` | — | `List<OrderResponse>` | 200 |
| GET | `/buyers/{buyerId}/returns` | — | `List<ReturnResponse>` | 200 |
| GET | `/buyers/{buyerId}/refunds` | — | `List<RefundResponse>` | 200 |
| POST | `/warehouses` | `RegisterWarehouseRequest {identifier, name, address, ownerSellerId?}` | `WarehouseResponse` | 201 |
| GET | `/warehouses/{warehouseId}` | — | `WarehouseResponse` | 200 |
| PUT | `/warehouses/{warehouseId}` | `UpdateWarehouseRequest` | `WarehouseResponse` | 200 |
| GET | `/products/{productId}` | — | `ProductResponse` | 200 |
| PATCH | `/products/{productId}/status` | `ChangeStatusRequest` (`ProductStatus` code) | `ProductResponse` | 200 |
| POST | `/inventory/movements` | `InventoryMovementRequest` | `InventoryResponse` | 201 |
| POST | `/inventory/damaged` | `DamagedStockRequest` | `InventoryResponse` | 201 |
| GET | `/inventory?productId=&warehouseId=` | — | `InventoryResponse` | 200 |
| GET | `/orders/{orderId}` | — | `OrderResponse` | 200 |
| POST | `/orders/{orderId}/payment` | — | `OrderResponse` (`PAID`) | 200 · 409 finalized |
| POST | `/orders/{orderId}/finalization` | — | `OrderResponse` (`DELIVERED`) | 200 |
| POST | `/orders/{orderId}/invoice` | — | `InvoiceResponse` | 201 · 400 already invoiced |
| GET | `/orders/{orderId}/invoice` | — | `InvoiceResponse` | 200 |
| POST | `/orders/{orderId}/shipment` | `CreateShipmentRequest {originWarehouseId, logisticsOperatorId}` | `ShipmentResponse` | 201 |
| POST | `/orders/{orderId}/shipment/dispatch` | — (also dispatches again a `RETURNED` shipment) | `ShipmentResponse` | 200 |
| POST | `/orders/{orderId}/shipment/delivery` | — | `ShipmentResponse` | 200 |
| GET | `/orders/{orderId}/shipment` | — | `ShipmentResponse` | 200 |
| GET | `/orders/{orderId}/tracking` | — | `List<TrackingEventResponse>` | 200 |
| POST | `/returns/{returnId}/approval` | — | `ReturnResponse` | 200 |
| POST | `/returns/{returnId}/rejection` | — | `ReturnResponse` | 200 |
| POST | `/returns/{returnId}/completion` | — (returned products go back to stock) | `ReturnResponse` (`COMPLETED`) | 200 · 400 return not approved |
| GET | `/returns/{returnId}` | — | `ReturnResponse` | 200 |
| POST | `/returns/{returnId}/refund` | `ProcessRefundRequest {amount?}` (optional body; default: order total) | `RefundResponse` | 201 · 422 return not approved |
| GET | `/returns/{returnId}/refund` | — | `RefundResponse` | 200 |
| GET | `/reports/commercial` | — | `CommercialReportResponse` | 200 |
| GET | `/users?role=` | — | `List<UserResponse>` | 200 · 400 unknown role |
| GET | `/warehouses` | — | `List<WarehouseResponse>` | 200 |
| GET | `/sellers/{sellerId}/products` | — | `List<ProductResponse>` (every status) | 200 |
| GET | `/orders?status=` | — | `List<OrderResponse>` | 200 · 400 unknown status |
| GET | `/returns?status=` | — | `List<ReturnResponse>` | 200 |
| GET | `/inventory/movements?productId=&warehouseId=` | — | `List<InventoryMovementResponse>` (chronological) | 200 |
| POST | `/orders/{orderId}/shipment/return` | `ShipmentReturnRequest {reason}` | `ShipmentResponse` (`RETURNED`) | 200 |
| POST | `/returns/{returnId}/refund/rejection` | — | `RefundResponse` (`REJECTED`) | 200 · 422 return not approved |

---

## 9. Supervisor (`/api/v1/supervisor`, role `SUPERVISOR`, read-only)

| Method | Path | Response `data` |
| ------ | ---- | --------------- |
| GET | `/users/{userId}` | `UserResponse` |
| GET | `/sellers/{sellerId}` | `SellerResponse` |
| GET | `/sellers/{sellerId}/performance-report` | `SellerPerformanceReportResponse` |
| GET | `/buyers/{buyerId}` | `BuyerResponse` |
| GET | `/buyers/{buyerId}/orders` | `List<OrderResponse>` |
| GET | `/buyers/{buyerId}/returns` | `List<ReturnResponse>` |
| GET | `/buyers/{buyerId}/refunds` | `List<RefundResponse>` |
| GET | `/warehouses/{warehouseId}` | `WarehouseResponse` |
| GET | `/products/{productId}` | `ProductResponse` |
| GET | `/inventory?productId=&warehouseId=` | `InventoryResponse` |
| GET | `/orders/{orderId}` | `OrderResponse` |
| GET | `/orders/{orderId}/invoice` | `InvoiceResponse` |
| GET | `/orders/{orderId}/shipment` | `ShipmentResponse` |
| GET | `/orders/{orderId}/tracking` | `List<TrackingEventResponse>` |
| GET | `/returns/{returnId}` | `ReturnResponse` |
| GET | `/returns/{returnId}/refund` | `RefundResponse` |
| GET | `/reports/commercial` | `CommercialReportResponse` |
| GET | `/users?role=` | `List<UserResponse>` |
| GET | `/warehouses` | `List<WarehouseResponse>` |
| GET | `/sellers/{sellerId}/products` | `List<ProductResponse>` |
| GET | `/orders?status=` | `List<OrderResponse>` |
| GET | `/returns?status=` | `List<ReturnResponse>` |
| GET | `/inventory/movements?productId=&warehouseId=` | `List<InventoryMovementResponse>` |

All return `200`; the supervisor has no write endpoint.

---

## 10. Example: Login and Checkout

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "buyer@nexusmarket.com", "password": "<password>" }
```

```json
{ "success": true, "message": "Authenticated",
  "data": { "token": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 3600,
            "user": { "identifier": "buyer-1", "fullName": "Bruno Comprador", "email": "buyer@nexusmarket.com",
                      "role": "BUYER", "status": "ACTIVE" } } }
```

```http
POST /api/v1/buyer/cart/items
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{ "productId": "product-1", "quantity": 2 }
```

```http
POST /api/v1/buyer/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Response `201`: `OrderResponse` with `orderStatus = PENDING_PAYMENT`, the items at the catalog price, and the invoice (`taxAmount` = VAT contained in the total).
