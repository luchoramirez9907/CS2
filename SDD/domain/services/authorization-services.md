# Authorization and Authentication Services

## 1. Purpose

This document specifies the services that decide **who may do what** (authorization) and **who is the requester** (authentication). General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Validate Permissions | `hasPermission` / `requirePermission` | `ValidatePermissionsService` |
| Validate Ownership Access | `canAccess*`, `require*Access` | `ValidateOwnershipAccessService` |
| Authenticate User | `login` / `logout` / `changePassword` | `AuthenticateUserService` |
| — (internal) | — | `UserAccountService` |

Authorization is applied in two layers: the REST security configuration only lets each role reach its own route prefix, and the domain services apply the permission matrix and the ownership rules below. The domain layer is authoritative.

---

## 2. Validate Permissions

### 2.1 Description

Decides whether a person may perform a business operation according to its role and status (RG-02 / RG-03). The operation → roles matrix is documented in `Services.md`, section *Permission Matrix* (enum `BusinessOperation`).

### 2.2 Consumers

Every Domain Service, as the first step of each operation.

### 2.3 Domain Service Method

```java
boolean hasPermission(Person user, BusinessOperation operation);
Person requirePermission(String userId, BusinessOperation operation);
```

### 2.4 Rules

`hasPermission`: true only when the person exists, is `ACTIVE`, and its role is allowed for the operation.

`requirePermission`, in order:

1. `userId` not blank; `operation` not null → `IllegalArgumentException`.
2. The person exists → `EntityNotFoundException`.
3. The person is `ACTIVE` → `IllegalStateException`.
4. Its role is allowed for the operation → `InvalidRoleAssignmentException` (the message lists the allowed roles).

Returns the loaded `Person`, used by the caller as the performer of the operation.

### 2.5 Output Ports

`PersonRepository`.

---

## 3. Validate Ownership Access

### 3.1 Description

Decides whether a person may access information that belongs to a specific buyer, seller, warehouse, product, or order.

### 3.2 Domain Service Method

```java
boolean canAccessPerson(Person user, Person target);     void requirePersonAccess(Person user, Person target);
boolean canAccessBuyer(Person user, Buyer buyer);         void requireBuyerAccess(Person user, Buyer buyer);
boolean canAccessSeller(Person user, Seller seller);      void requireSellerAccess(Person user, Seller seller);
boolean canAccessWarehouse(Person user, Warehouse w);     void requireWarehouseAccess(Person user, Warehouse w);
boolean canAccessProduct(Person user, Product product);   void requireProductAccess(Person user, Product product);
boolean canAccessOrder(Person user, Order order);         void requireOrderAccess(Person user, Order order);
```

`require*` methods throw `OwnershipAccessDeniedException` when the corresponding `canAccess*` is false.

### 3.3 Rules

| Resource | Administrator / Supervisor | Buyer | Seller | Logistics Operator |
| -------- | -------------------------- | ----- | ------ | ------------------ |
| Person / Buyer / Seller | All | Only itself | Only itself | Only itself |
| Warehouse | All | — | Only its `SellerWarehouse` | All |
| Product | All | — | Only its own products | — |
| Order | All | Only its own orders | Orders containing its products | Orders whose shipment is assigned to it |

Supervisors can access everything for **consultation only**: the permission matrix never grants them a write operation.

---

## 4. Authenticate User — Login

### 4.1 Description

Validates the credentials of a participant and issues its access token.

### 4.2 Actor

Any participant (no prior authentication).

### 4.3 Domain Service Method

```java
AuthenticationResult login(String email, String password);
```

Role port: `PublicAccessPort.login`. REST: `POST /api/v1/auth/login`.

### 4.4 Validations (in order)

1. `email` and `password` not blank → `InvalidCredentialsException`.
2. An account exists for the email → `InvalidCredentialsException`.
3. The password matches the stored hash → `InvalidCredentialsException` (same message as step 2, so the response never reveals which accounts exist).
4. The person is `ACTIVE` → `AccountDisabledException`.

### 4.5 Domain Behavior

`TokenService.issueToken(user)` signs a token bound to the current `authTokenVersion`.

### 4.6 Output

`AuthenticationResult(user, accessToken, expiresInSeconds)`.

### 4.7 Output Ports

`UserRepository`, `PasswordService`, `TokenService`.

---

## 5. Authenticate User — Logout

### 5.1 Description

Ends every session of the authenticated participant.

### 5.2 Domain Service Method

```java
void logout(User user);
```

Role port: `PublicAccessPort.logout`. REST: `POST /api/v1/auth/logout` (any authenticated role).

### 5.3 Domain Behavior and Persistence

`user.invalidateSessions()` (increments `authTokenVersion`), `UserRepository.save(user)`. Every previously issued token is rejected from then on.

---

## 6. Authenticate User — Change Password

### 6.1 Description

Changes the password of the authenticated participant.

### 6.2 Domain Service Method

```java
void changePassword(User user, String currentPassword, String newPassword);
```

Role port: `PublicAccessPort.changeMyPassword`. REST: `PUT /api/v1/auth/password`.

### 6.3 Validations (in order)

1. The current password matches → `InvalidCredentialsException`.
2. The new password satisfies the policy → `WeakPasswordException`.
3. The new password differs from the current one → `IllegalArgumentException`.

### 6.4 Domain Behavior and Persistence

`user.changePasswordHash(passwordService.hash(newPassword))` (also invalidates every previous token), `UserRepository.save(user)`.

---

## 7. User Account Service (internal)

`UserAccountService` creates the access account of a person when it is registered (`user-services.md`, `seller-services.md`, `buyer-services.md`):

1. **Password policy** (`requireValidPassword`): 8 to 100 characters, at least one letter and one digit → `WeakPasswordException`. The registration services call it **before** storing the person.
2. The person has no account yet → `IllegalArgumentException`.
3. `new User(person, passwordService.hash(password))` with `authTokenVersion = 0`, stored through `UserRepository`.

---

## 8. Request Authentication (infrastructure)

On every protected request, `JwtAuthenticationFilter` (see `Adapters/Rest-security-cors.md`) validates the token with `TokenService.parseToken`, loads the `User` by the token subject, and accepts it only if the person is `ACTIVE` and the token version equals the current `authTokenVersion`. Role and status are never read from the token.
