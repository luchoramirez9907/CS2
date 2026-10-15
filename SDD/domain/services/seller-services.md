# Seller Management Services

## 1. Purpose

This document specifies the registration and management of **sellers**. A seller cannot self-register: it is always incorporated by an administrator. General conventions (requester, authorization, HTTP mapping) are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Register Seller | `registerSeller` | `RegisterSellerService` |
| Manage Seller | `consultSeller` / `updateSeller` / `changeSellerStatus` | `ManageSellerService` |

---

## 2. Register Seller

### 2.1 Description

Creates a new seller, links it to the administrator who incorporates it (`Seller.registeredBy`), and creates its access account.

### 2.2 Actor

`ADMINISTRATOR`.

### 2.3 Domain Service Method

```java
Seller registerSeller(String administratorId, String identifier, String fullName, String email,
                      String password);
```

Role port: `AdministratorPort.registerSeller`. REST: `POST /api/v1/admin/sellers`.

### 2.4 Input

| Field | Rule |
| ----- | ---- |
| `identifier` | Required, not blank, unique across the platform. |
| `fullName` | Required, not blank. |
| `email` | Required, valid, unique across the platform. |
| `password` | Password policy (8–100 characters, at least one letter and one digit). |

### 2.5 Validations (in order)

1. `administratorId`, `identifier`, `fullName`, `email` not blank → `IllegalArgumentException`.
2. Password policy → `WeakPasswordException`.
3. The performer exists → `EntityNotFoundException`; is active → `IllegalStateException`.
4. The performer is not a seller → `SellerSelfRegistrationNotAllowedException` (a seller can never register sellers, including itself).
5. The performer is an administrator → `InvalidRoleAssignmentException`.
6. `identifier` and `email` not already used → `DuplicateEmailException`.

### 2.6 Domain Behavior

Creates `Seller(identifier, fullName, email, registeredBy = administrator, status = ACTIVE)`. The `Seller` constructor itself rejects a missing registering administrator.

### 2.7 Persistence

`SellerRepository.save(seller)`, then `UserAccountService.createAccount(seller, password)`.

### 2.8 Output

The registered `Seller`.

### 2.9 Exceptions

`IllegalArgumentException`, `WeakPasswordException`, `EntityNotFoundException`, `IllegalStateException`, `SellerSelfRegistrationNotAllowedException`, `InvalidRoleAssignmentException`, `DuplicateEmailException`.

### 2.10 Output Ports

`PersonRepository`, `SellerRepository`, `UserRepository`, `PasswordService`, `NotificationService`.

### 2.11 Processing Flow

```text
validate fields → validate password policy → load performer (exists, active)
→ reject seller performer → require ADMINISTRATOR → require unique identifier and email
→ create Seller (registeredBy = administrator, ACTIVE) → save → create access account → notify "Welcome"
```

---

## 3. Consult Seller

### 3.1 Description

Retrieves the information of a seller (identity, status, registering administrator, warehouses, products).

### 3.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any seller); `SELLER` (only itself).

### 3.3 Domain Service Method

```java
Seller consultSeller(String requesterId, String sellerId);
```

Role ports: `AdministratorPort.consultSeller`, `SupervisorPort.consultSeller`, `SellerPort.consultMyProfile`.

### 3.4 Validations and Authorization

1. Requester has `CONSULT_SELLER`.
2. `sellerId` not blank; the seller exists → `EntityNotFoundException`.
3. `requireSellerAccess`: a seller only accesses itself → `OwnershipAccessDeniedException`.

### 3.5 Output

The `Seller`. No state changes. Output port: `SellerRepository`.

---

## 4. Update Seller

### 4.1 Description

Updates the full name and email of a seller.

### 4.2 Actor

`ADMINISTRATOR` (any seller); `SELLER` (only itself).

### 4.3 Domain Service Method

```java
Seller updateSeller(String requesterId, String sellerId, String fullName, String email);
```

Role ports: `AdministratorPort.updateSeller`, `SellerPort.updateMyProfile`.

### 4.4 Validations and Authorization

1. Requester has `UPDATE_SELLER`; the seller exists; `requireSellerAccess`.
2. The new email is not used by another person → `DuplicateEmailException`.
3. `fullName` not blank and `email` valid → `IllegalArgumentException`.

### 4.5 Persistence and Notification

`SellerRepository.save(seller)`; notifies "Seller updated".

### 4.6 Output

The updated `Seller`.

---

## 5. Change Seller Status

### 5.1 Description

Changes the operational status of a seller (`ACTIVE`, `INACTIVE`, `BLOCKED`). A seller that is not active cannot publish products, register warehouses, or authenticate.

### 5.2 Actor

`ADMINISTRATOR`.

### 5.3 Domain Service Method

```java
Seller changeSellerStatus(String requesterId, String sellerId, UserStatus status);
```

Role port: `AdministratorPort.changeSellerStatus`. REST: `PATCH /api/v1/admin/sellers/{sellerId}/status`.

### 5.4 Validations and Authorization

1. `status` not null → `IllegalArgumentException`.
2. Requester has `CHANGE_SELLER_STATUS`; the seller exists.

### 5.5 Persistence and Notification

`Seller.changeStatus(status)`, `SellerRepository.save(seller)`; notifies "Seller status changed".

### 5.6 Output

The updated `Seller`.
