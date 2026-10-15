# User Management Services

## 1. Purpose

This document specifies the services that register and manage the **internal participants** of NexusMarket: administrators, supervisors, and logistics operators. Buyers and sellers are registered through their own services (`buyer-services.md`, `seller-services.md`).

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Register User | `registerUser` | `RegisterUserService` |
| Manage User | `consultUser` / `updateUser` / `changeUserStatus` / `listUsers` | `ManageUserService` |

---

## 2. Conventions (all service documents)

- **Requester.** Every operation receives the identifier of the requesting person (`requesterId`, `administratorId`, …). The Role Input Port implementations always pass the authenticated `User`'s identifier, so it can never be chosen by the client.
- **Authorization.** `ValidatePermissionsService.requirePermission(requesterId, operation)` loads the requester, rejects it when it does not exist (`EntityNotFoundException`) or is not active (`IllegalStateException`), and checks the permission matrix of `Services.md` (`InvalidRoleAssignmentException`). Ownership checks (`require*Access`) then reject access to information of other participants (`OwnershipAccessDeniedException`).
- **HTTP mapping.** Exceptions are translated by the REST layer as defined in `Adapters/Global-exception-handler.md`: `IllegalArgumentException` → 400, `InvalidRoleAssignmentException` / `OwnershipAccessDeniedException` → 403, `EntityNotFoundException` → 404, `DuplicateEmailException` → 409, `IllegalStateException` → 409, other `DomainException` → 422.
- **Notifications** are sent through the `NotificationService` output port after the change is persisted.

---

## 3. Register User

### 3.1 Description

Creates a new internal participant (administrator, supervisor, or logistics operator) with its identity, role, `ACTIVE` status, and access account.

### 3.2 Actor

`ADMINISTRATOR`.

### 3.3 Domain Service Method

```java
Person registerUser(String administratorId, String identifier, String fullName, String email,
                    SystemRole role, String password);
```

Role port: `AdministratorPort.registerUser`. REST: `POST /api/v1/admin/users`.

### 3.4 Input

| Field | Rule |
| ----- | ---- |
| `identifier` | Required, not blank, unique across the platform. |
| `fullName` | Required, not blank. |
| `email` | Required, valid (contains `@`), unique across the platform. |
| `role` | Required: `ADMINISTRATOR`, `SUPERVISOR`, or `LOGISTICS_OPERATOR`. |
| `password` | Password policy: 8–100 characters, at least one letter and one digit. |

### 3.5 Validations (in order)

1. `identifier`, `fullName`, `email` not blank; `role` not null → `IllegalArgumentException`.
2. Password policy → `WeakPasswordException`.
3. Requester has `REGISTER_USER` (administrator, active).
4. `role` is not `BUYER` nor `SELLER` (they have dedicated services) → `IllegalArgumentException`.
5. `identifier` and `email` not already used by any person → `DuplicateEmailException`.

The password is validated **before** anything is stored, so a rejected password never leaves a person without an account.

### 3.6 Domain Behavior

Creates the specialization that corresponds to the role (`Administrator`, `Supervisor`, or `LogisticsOperator`) with status `ACTIVE`. Each person holds exactly one role (RG-02).

### 3.7 Persistence

`PersonRepository.save(person)`, then `UserAccountService.createAccount(person, password)` (stores the BCrypt hash through `UserRepository`).

### 3.8 Output

The registered `Person`.

### 3.9 Exceptions

`IllegalArgumentException`, `WeakPasswordException`, `InvalidRoleAssignmentException`, `EntityNotFoundException` (unknown requester), `IllegalStateException` (inactive requester), `DuplicateEmailException` (identifier or email already used).

### 3.10 Output Ports

`PersonRepository`, `UserRepository`, `PasswordService`, `NotificationService`.

### 3.11 Processing Flow

```text
validate fields → validate password policy → require REGISTER_USER
→ reject BUYER/SELLER roles → require unique identifier and email
→ create specialization (ACTIVE) → save person → create access account → notify "Welcome"
```

---

## 4. Consult User

### 4.1 Description

Retrieves the identity information of a participant.

### 4.2 Actor

`ADMINISTRATOR` and `SUPERVISOR` (any participant); `BUYER`, `SELLER`, `LOGISTICS_OPERATOR` (only themselves).

### 4.3 Domain Service Method

```java
Person consultUser(String requesterId, String userId);
```

Role ports: `AdministratorPort.consultUser`, `SupervisorPort.consultUser`, `LogisticsOperatorPort.consultMyProfile`.

### 4.4 Validations and Authorization

1. Requester has `CONSULT_USER`.
2. `userId` not blank; the person exists → `EntityNotFoundException`.
3. `requirePersonAccess`: administrative roles access every person; other roles only themselves → `OwnershipAccessDeniedException`.

### 4.5 Output

The `Person`. No state changes.

### 4.6 Output Ports

`PersonRepository`.

---

## 5. Update User

### 5.1 Description

Updates the full name and email of a participant. The identifier, role, status, and credentials never change through this operation.

### 5.2 Actor

`ADMINISTRATOR` (any participant); `BUYER`, `SELLER`, `LOGISTICS_OPERATOR` (only themselves). The supervisor is read-only.

### 5.3 Domain Service Method

```java
Person updateUser(String requesterId, String userId, String fullName, String email);
```

Role ports: `AdministratorPort.updateUser`, `LogisticsOperatorPort.updateMyProfile`.

### 5.4 Validations and Authorization

1. Requester has `UPDATE_USER`; the target exists; `requirePersonAccess`.
2. The new `email` is not used by another person → `DuplicateEmailException`.
3. `Person.updateContactInformation`: `fullName` not blank, `email` contains `@` → `IllegalArgumentException`.

### 5.5 Persistence and Notification

`PersonRepository.save(person)`; notifies "Profile updated".

### 5.6 Output

The updated `Person`.

---

## 6. Change User Status

### 6.1 Description

Changes the operational status of a participant (`ACTIVE`, `INACTIVE`, `BLOCKED`). A participant that is not `ACTIVE` cannot authenticate nor perform any operation.

### 6.2 Actor

`ADMINISTRATOR`.

### 6.3 Domain Service Method

```java
Person changeUserStatus(String requesterId, String userId, UserStatus status);
```

Role port: `AdministratorPort.changeUserStatus`. REST: `PATCH /api/v1/admin/users/{userId}/status`.

### 6.4 Validations and Authorization

1. `status` not null → `IllegalArgumentException`.
2. Requester has `CHANGE_USER_STATUS`; the target exists.
3. An administrator cannot change its own status → `IllegalArgumentException`.

### 6.5 Domain Behavior

`Person.changeStatus(status)`. The JWT filter reads the current status on every request, so a blocked participant is rejected immediately, even with a previously issued token.

### 6.6 Persistence and Notification

`PersonRepository.save(person)`; notifies "Account status changed".

### 6.7 Output

The updated `Person`.

---

## 7. List Users

### 7.1 Description

Lists the participants the requester may access, sorted by identifier, optionally filtered by role.

### 7.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (every participant).

### 7.3 Domain Service Method

```java
List<Person> listUsers(String requesterId, SystemRole role);
```

`role` is optional. Role ports: `AdministratorPort.listUsers`, `SupervisorPort.listUsers`. REST: `GET .../users?role=`.

### 7.4 Authorization and Output

Requester has `CONSULT_USER`; every person is filtered with `canAccessPerson` (administrative roles see everyone) and by role. Output port: `PersonRepository.findAll`.
