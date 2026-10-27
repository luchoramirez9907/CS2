# Global Exception Handler Specification (Spring Boot)

## 1. Purpose

This document defines the single error contract of every REST endpoint and of the Spring Security boundary. Domain exceptions remain framework-independent; only the REST and infrastructure layers map them to HTTP responses.

---

## 2. Standard Error Response

Every handled error uses this envelope (`adapters/in/rest/responses/ErrorResponse`):

```json
{
  "timestamp": "2026-09-29T12:00:00.000Z",
  "status": 404,
  "code": "RESOURCE_NOT_FOUND",
  "message": "Order 'x' does not exist",
  "path": "/api/v1/admin/orders/x",
  "requestId": "req-3f2c...",
  "details": null
}
```

- `code` is stable and machine-readable; clients must not parse `message`.
- `path` excludes the query string.
- `requestId` equals the `X-Request-Id` response header.
- `details` carries field validation errors (`[{field, message}]`) or `null`.
- Unexpected errors return a generic message and never expose stack traces, SQL, credentials, or tokens.

---

## 3. Exception Mapping

| Failure | Exception | HTTP | Code |
| ------- | --------- | ---: | ---- |
| Invalid DTO fields | `MethodArgumentNotValidException`, `HandlerMethodValidationException` | 400 | `INVALID_REQUEST` (+ `details`) |
| Malformed JSON | `HttpMessageNotReadableException` | 400 | `INVALID_REQUEST` |
| Missing / mistyped parameter | `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` | 400 | `INVALID_REQUEST` |
| Domain input validation, unknown catalog code | `IllegalArgumentException` | 400 | `INVALID_REQUEST` |
| No or invalid token (filter chain) | `AuthenticationException` → `SecurityErrorHandler` | 401 | `AUTHENTICATION_REQUIRED` |
| Wrong login credentials | `InvalidCredentialsException` | 401 | `INVALID_CREDENTIALS` |
| Role not allowed on the route (filter chain) | `AccessDeniedException` → `SecurityErrorHandler` | 403 | `FORBIDDEN` |
| Role / ownership / disabled account (domain) | `InvalidRoleAssignmentException`, `OwnershipAccessDeniedException`, `SellerSelfRegistrationNotAllowedException`, `AccountDisabledException` | 403 | `FORBIDDEN` |
| Entity does not exist | `EntityNotFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| Unknown route | `NoResourceFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| Unsupported HTTP method | `HttpRequestMethodNotSupportedException` | 405 | `INVALID_REQUEST` |
| Unsupported Content-Type (body not sent as JSON) | `HttpMediaTypeNotSupportedException` | 415 | `INVALID_REQUEST` |
| Duplicate person (identifier or email), product or warehouse identifier | `DuplicateEmailException`, `DuplicateResourceException` | 409 | `RESOURCE_ALREADY_EXISTS` |
| Illegal state transition, inactive participant | `OrderAlreadyFinalizedException`, `IllegalStateException` | 409 | `INVALID_STATE_TRANSITION` |
| Other business rule | `InsufficientInventoryException`, `WeakPasswordException`, `ReturnNotAllowedException`, `RefundNotAllowedException`, `BuyerNotAuthorizedException`, any other `DomainException` | 422 | `BUSINESS_RULE_VIOLATION` |
| Database unavailable | `DataAccessResourceFailureException`, `CannotCreateTransactionException` | 503 | `DEPENDENCY_UNAVAILABLE` |
| Unexpected error | `Exception` | 500 | `INTERNAL_ERROR` |

Unknown exceptions never fall back to `400`.

---

## 4. Implementation

- `GlobalExceptionHandler` (`adapters/in/rest/exception/`, `@RestControllerAdvice`) handles controller, validation and domain exceptions.
- `SecurityErrorHandler` (`adapters/security/`) implements `AuthenticationEntryPoint` and `AccessDeniedHandler`, so `401` and `403` raised by the filter chain use the same envelope (`@RestControllerAdvice` does not see filter-chain failures).
- `RequestIdFilter` reuses a valid incoming `X-Request-Id` or generates one, returns it in the response, stores it in the logging MDC (`requestId`), and exposes it to both handlers.
- Unexpected (`500`) and dependency (`503`) failures are logged with the request id; the response only contains the generic message.
- Every rejected request (`4xx`) is logged as a warning with its request id, method, path, status and error code (never the request body), so a failing call can be traced in the logs. `SecurityErrorHandler` writes the same warning for the `401` and `403` raised by the filter chain.

---

## 5. Verification

`RestApiIntegrationTest` verifies the envelope and the status/code mapping for missing token (`401`), invalid credentials (`401`), wrong role (`403`), validation and malformed JSON (`400`), not found (`404`), duplicate (`409`), and the `X-Request-Id` correlation between header and body.
