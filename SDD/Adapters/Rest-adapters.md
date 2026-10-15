# REST Adapters Specification

## 1. Overview

REST adapters reside in `application/adapters/in/rest/`. They handle HTTP transport concerns: they receive requests, validate the Request DTOs, map them to Domain Models and Value Objects, call the corresponding **Role Input Port** with the authenticated `User`, and map the Domain Models back to Response DTOs.

REST adapters never implement business rules and never call Domain Services directly: they only depend on the Role Input Ports.

---

## 2. Component Architecture

```text
HTTP Request (client)
      │  Authorization: Bearer <JWT>
      ▼
Spring Security filter chain (adapters/security)
      │  1. RequestIdFilter: X-Request-Id
      │  2. JwtAuthenticationFilter: validates the token; LoadAuthenticatedUserService loads the current User
      │  3. Route authorization: the route prefix requires the role of its port
      ▼
REST Controller (adapters/in/rest/controllers)
      │  1. @Valid Request DTO (Bean Validation)
      │  2. Mapper: Request DTO → Domain Model / Value Object
      │  3. Role Input Port call with the authenticated User
      │  4. Mapper: Domain Model → Response DTO
      ▼
Role Input Port (domain/ports/in/*Port)  ← implemented by adapters/useCases
```

---

## 3. Authentication, JWT and User Reconstruction

### 3.1 Login (`POST /api/v1/auth/login`)

1. The client sends `LoginRequest {email, password}`.
2. `AuthRestController` calls `PublicAccessPort.login(email, password)`.
3. `AuthenticateUserService` verifies the password hash (`PasswordService`), rejects inactive persons, and issues the token through the `TokenService` output port (`JwtTokenServiceAdapter`).
4. The response contains the token, its type (`Bearer`), its lifetime in seconds, and the authenticated participant.

**JWT claims:** `sub` (immutable person identifier), `jti` (token id), `ver` (`User.authTokenVersion`), `iat`, `exp`. The token never contains the role, email, password, or any authorization snapshot.

### 3.2 User reconstruction on every request

1. `JwtAuthenticationFilter` reads `Authorization: Bearer <token>` and asks `TokenService` to verify the signature and expiration.
2. It loads the current `User` by the `sub` claim through the `UserRepository` output port.
3. It rejects (leaves unauthenticated) missing accounts, persons that are not active, and tokens whose `ver` differs from the current `authTokenVersion` (logout or password change).
4. It stores an `AuthenticatedUserPrincipal` (wrapping the loaded `User` and its `ROLE_<code>` authority) in the Spring Security context.
5. Controllers obtain it with `@AuthenticationPrincipal AuthenticatedUserPrincipal principal` and pass `principal.user()` to the Role Input Port.

Role and status always come from the current domain model, never from token claims.

### 3.3 Logout and password change

`POST /api/v1/auth/logout` and `PUT /api/v1/auth/password` increment `authTokenVersion`, so every previously issued token is rejected by the filter (`401`).

---

## 4. REST Layer Structure

```text
adapters/in/rest/
├── controllers/   One controller per Role Input Port
│   ├── AuthRestController              /api/v1/auth
│   ├── PublicAccessRestController      /api/v1/public
│   ├── BuyerRestController             /api/v1/buyer
│   ├── SellerRestController            /api/v1/seller
│   ├── LogisticsOperatorRestController /api/v1/operator
│   ├── AdministratorRestController     /api/v1/admin
│   └── SupervisorRestController        /api/v1/supervisor
├── requests/      Request DTOs with Jakarta Bean Validation
├── responses/     Response DTOs, ApiResponse (success) and ErrorResponse (errors)
├── mappers/       Request DTO → Domain, Domain → Response DTO
└── exception/     GlobalExceptionHandler (@RestControllerAdvice)
```

---

## 5. Code Pattern Example

```java
@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerRestController {

    private final BuyerPort buyerPort;

    @PostMapping("/cart/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request) {
        ShoppingCart cart = buyerPort.addItemToCart(principal.user(), request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added to cart", CartDtoMapper.toResponse(cart)));
    }
}
```

---

## 6. Rules

1. Controllers depend only on Role Input Ports, never on Domain Services.
2. The requester is always the authenticated `User`; request bodies never carry "who I am" identifiers (`buyerId`, `approverId`, …).
3. Request DTOs and Spring annotations never enter the Domain layer; persistence entities are never returned by the API.
4. Catalog codes are converted with the Value Object `fromCode` methods; unknown codes return `400`.
5. Passwords and tokens are never logged: request and response DTOs that carry them override `toString()`.

See `Api-rest-endpoints.md`, `Rest-validation.md`, `Global-exception-handler.md`, and `Rest-security-cors.md`.
