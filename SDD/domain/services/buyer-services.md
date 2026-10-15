# Buyer Management Services

## 1. Purpose

This document specifies the registration and management of **buyers** and the consultation of their orders, returns, and refunds. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Register Buyer | `registerBuyer` | `RegisterBuyerService` |
| Manage Buyer | `consultBuyer`, `updateBuyer`, `changeCommercialStatus`, `consultBuyerOrders`, `consultBuyerReturns`, `consultBuyerRefunds` | `ManageBuyerService` |

`BuyerCommercialStatus` (`ACTIVE`, `RESTRICTED`, `BLOCKED`) is independent from `UserStatus`: it controls the ability to purchase, not the access to the system. Only an `ACTIVE` commercial status can add items to the cart and check out (`Buyer.requirePurchaseAuthorization`).

---

## 2. Register Buyer

### 2.1 Description

Creates a new buyer with its primary delivery address, commercial status `ACTIVE`, user status `ACTIVE`, and access account. A buyer may register itself or be registered by an administrator.

### 2.2 Actor

Anonymous visitor (self-registration) or `ADMINISTRATOR`.

### 2.3 Domain Service Method

```java
Buyer registerBuyer(String performerId, String identifier, String fullName, String email,
                    Address primaryAddress, String password);
```

`performerId` is `null` for self-registration. Role ports: `PublicAccessPort.registerBuyer` (performer `null`), `AdministratorPort.registerBuyer`. REST: `POST /api/v1/public/buyers`, `POST /api/v1/admin/buyers`.

### 2.4 Input

| Field | Rule |
| ----- | ---- |
| `identifier` | Required, not blank, unique across the platform. |
| `fullName` | Required, not blank. |
| `email` | Required, valid, unique across the platform. |
| `primaryAddress` | Required `Address` (street, city, state, country, postal code; all not blank). |
| `password` | Password policy (8–100 characters, at least one letter and one digit). |

### 2.5 Validations (in order)

1. `identifier`, `fullName`, `email` not blank; `primaryAddress` not null → `IllegalArgumentException`.
2. Password policy → `WeakPasswordException`.
3. When a performer is given, it has `REGISTER_BUYER` (administrator, active).
4. `identifier` and `email` not already used → `DuplicateEmailException`.

### 2.6 Domain Behavior

Creates `Buyer(identifier, fullName, email, primaryAddress, commercialStatus = ACTIVE, status = ACTIVE)` with no additional addresses, cart, or orders.

### 2.7 Persistence

`BuyerRepository.save(buyer)`, then `UserAccountService.createAccount(buyer, password)`.

### 2.8 Output

The registered `Buyer`.

### 2.9 Exceptions

`IllegalArgumentException`, `WeakPasswordException`, `InvalidRoleAssignmentException` (performer is not an administrator), `DuplicateEmailException`.

### 2.10 Output Ports

`BuyerRepository`, `PersonRepository`, `UserRepository`, `PasswordService`, `NotificationService`.

### 2.11 Processing Flow

```text
validate fields and address → validate password policy → [performer given] require REGISTER_BUYER
→ require unique identifier and email → create Buyer (ACTIVE / ACTIVE) → save
→ create access account → notify "Welcome"
```

---

## 3. Consult Buyer

### 3.1 Description

Retrieves the information of a buyer: identity, addresses, commercial status.

### 3.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any buyer); `BUYER` (only itself).

### 3.3 Domain Service Method

```java
Buyer consultBuyer(String requesterId, String buyerId);
```

Role ports: `AdministratorPort.consultBuyer`, `SupervisorPort.consultBuyer`, `BuyerPort.consultMyProfile`.

### 3.4 Validations and Authorization

1. Requester has `CONSULT_BUYER`.
2. `buyerId` not blank; the buyer exists → `EntityNotFoundException`.
3. `requireBuyerAccess`: a buyer only accesses itself → `OwnershipAccessDeniedException`.

### 3.5 Output

The `Buyer`. Output port: `BuyerRepository`.

---

## 4. Update Buyer

### 4.1 Description

Updates the full name, email, and delivery addresses of a buyer.

### 4.2 Actor

`ADMINISTRATOR` (any buyer); `BUYER` (only itself).

### 4.3 Domain Service Method

```java
Buyer updateBuyer(String requesterId, String buyerId, String fullName, String email,
                  Address primaryAddress, List<Address> additionalAddresses);
```

`primaryAddress` and `additionalAddresses` are optional: `null` keeps the current value. Role ports: `AdministratorPort.updateBuyer`, `BuyerPort.updateMyProfile`.

### 4.4 Validations and Authorization

1. Requester has `UPDATE_BUYER`; the buyer exists; `requireBuyerAccess`.
2. The new email is not used by another person → `DuplicateEmailException`.
3. `fullName` not blank and `email` valid → `IllegalArgumentException`.

### 4.5 Domain Behavior

`updateContactInformation`, then `setPrimaryAddress` and `replaceAdditionalAddresses` when given. The commercial status, user status, and credentials never change here.

### 4.6 Persistence and Notification

`BuyerRepository.save(buyer)`; notifies "Buyer updated".

### 4.7 Output

The updated `Buyer`.

---

## 5. Change Buyer Commercial Status

### 5.1 Description

Changes the commercial status of a buyer (`ACTIVE`, `RESTRICTED`, `BLOCKED`).

### 5.2 Actor

`ADMINISTRATOR`.

### 5.3 Domain Service Method

```java
Buyer changeCommercialStatus(String requesterId, String buyerId, BuyerCommercialStatus status);
```

Role port: `AdministratorPort.changeBuyerCommercialStatus`. REST: `PATCH /api/v1/admin/buyers/{buyerId}/commercial-status`.

### 5.4 Validations and Authorization

1. `status` not null → `IllegalArgumentException`.
2. Requester has `CHANGE_BUYER_COMMERCIAL_STATUS`; the buyer exists.

### 5.5 Domain Behavior

`Buyer.setCommercialStatus(status)`. From then on, a buyer that is not `ACTIVE` cannot add items to its cart nor check out (`BuyerNotAuthorizedException`).

### 5.6 Persistence and Notification

`BuyerRepository.save(buyer)`; notifies "Commercial status changed".

### 5.7 Output

The updated `Buyer`.

---

## 6. Consult Buyer Orders, Returns and Refunds

### 6.1 Description

Retrieves the orders, the return requests, or the refunds of a buyer.

### 6.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any buyer); `BUYER` (only its own).

### 6.3 Domain Service Method

```java
List<Order> consultBuyerOrders(String requesterId, String buyerId);
List<Return> consultBuyerReturns(String requesterId, String buyerId);
List<Refund> consultBuyerRefunds(String requesterId, String buyerId);
```

Role ports: `AdministratorPort` / `SupervisorPort` (`consultBuyerOrders`, `consultBuyerReturns`, `consultBuyerRefunds`), `BuyerPort` (`consultMyOrders`, `consultMyReturns`, `consultMyRefunds`).

### 6.4 Validations and Authorization

Same as *Consult Buyer* (`CONSULT_BUYER` + `requireBuyerAccess`).

### 6.5 Domain Behavior

- Orders: `OrderRepository.findByBuyerId`.
- Returns: `ReturnRepository.findByBuyerId`.
- Refunds: the refunds of the buyer's returns that have one (a return generates at most one refund).

### 6.6 Output

The list (possibly empty). No state changes.

### 6.7 Output Ports

`BuyerRepository`, `OrderRepository`, `ReturnRepository`.
