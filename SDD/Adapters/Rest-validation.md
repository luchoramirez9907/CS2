# REST Validation Specification (Spring Boot)

## 1. Purpose

This document defines request validation at the REST boundary. It applies to every endpoint of `Api-rest-endpoints.md` and does not replace the domain validations of the Domain Services and entities.

Request DTOs use Jakarta Bean Validation (`jakarta.validation`, `spring-boot-starter-validation`); controllers apply `@Valid`. Validation annotations never enter the Domain layer.

---

## 2. Processing Order

1. **Security**: the filter chain authenticates the token and checks the role of the route before the controller runs (`401` / `403`).
2. **Transport**: JSON content type, well-formed JSON, path and query parameters (`400`).
3. **Shape and format**: required fields, sizes, email format, positive numbers (`400 INVALID_REQUEST` with field details).
4. **Catalog codes**: converted with `fromCode`; unknown codes are rejected (`400`).
5. **Domain**: authoritative rules — uniqueness, ownership, stock, state transitions, password policy (`403`, `404`, `409`, `422`).

A failure stops processing: an invalid request never reaches the Role Input Port.

---

## 3. Shared Field Rules

| Field | Boundary rule | Domain rule |
| ----- | ------------- | ----------- |
| `identifier` | Required; 1–64 characters; no whitespace. | Unique across the platform (`409`). |
| `fullName`, `name` | Required; not blank; at most 160 characters. | — |
| `email` | Required; valid email; at most 160 characters. | Unique across the platform (`409`). |
| `password` | Required; 8–100 characters; never returned or logged. | At least one letter and one digit (`422`). |
| `description` | Optional; at most 1000 characters. | — |
| address fields | All required and not blank; street ≤ 200, postal code ≤ 20. | — |
| `price` | Required on publication; decimal ≥ 0 with at most 13 integer digits and 2 decimals. | Not negative. |
| `quantity` (cart) | Positive integer, at most 1000. | Stock never negative (`422`). |
| `quantity` (damaged stock) | Positive integer, at most 100000. | Never above the available stock (`422`). |
| `quantity` (inventory movement) | Integer between -100000 and 100000. | Positive, except the signed `ADJUSTMENT`. |
| `amount` (refund) | Optional; positive decimal with at most 2 decimals. | Never above the order total (`400`). |
| `reason` (return) | Required; at most 500 characters. | — |
| `status`, `role`, `movementType` | Required; exact catalog code (case-sensitive), checked with `@Pattern` against the catalog: `status` ∈ {ACTIVE, INACTIVE, BLOCKED, RESTRICTED, PUBLISHED, SUSPENDED, DISCONTINUED}; `role` ∈ `SystemRole`; `movementType` ∈ `InventoryMovementType`. | Allowed transition (`409` / `422`). |
| referenced identifiers in bodies (`productId`, `warehouseId`, `originWarehouseId`, …) | Required where applicable; at most 64 characters. | Must exist (`404`). |
| path identifiers | Required path variables. | Must exist (`404`). |

---

## 4. Endpoint-Specific Rules

- **Login** requires non-blank `email` and `password`; an unknown email and a wrong password return the same `401 INVALID_CREDENTIALS`.
- **Registrations** (buyer, seller, user) require every identity field and an initial password; duplicates return `409 RESOURCE_ALREADY_EXISTS`.
- **Profile updates** only accept editable fields (name, email, addresses); identity, role, status and credentials are never editable through them.
- **Cart items** only receive the product and the quantity: the unit price is always the catalog price.
- **Refund**: the body is optional; without `amount` the order total is refunded.
- **Nested objects** (`primaryAddress`, `additionalAddresses`, `variants`) are validated in cascade with `@Valid`.

---

## 5. Implementation

- `@Valid` on every `@RequestBody`; constraints such as `@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Pattern`, `@Positive`, `@PositiveOrZero`, `@Min`, `@Max`, `@Digits`.
- `MethodArgumentNotValidException` → `400 INVALID_REQUEST` with `details = [{field, message}, …]`.
- `HttpMessageNotReadableException` (malformed JSON) → `400 INVALID_REQUEST`.
- Missing or mistyped query parameters → `400 INVALID_REQUEST`.

---

## 6. Verification

`RequestCatalogValidationTest` checks the catalog codes (`status`, `role`, `movementType`) and the ranges of quantities and prices directly on the request DTOs. `RestApiIntegrationTest` covers: invalid body with two field errors, malformed JSON, and proves that an invalid request does not reach the domain (no cart is created); plus `401`, `403`, `404`, and `409` responses.
