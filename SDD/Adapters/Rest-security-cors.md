# REST Security and CORS Specification (Spring Security)

## 1. Purpose

This document defines the security boundary of the NexusMarket API: authentication, role-based route authorization, CORS for the browser frontend, and request correlation.

---

## 2. Authentication

- Stateless JWT authentication: no HTTP session, no cookies, CSRF protection disabled (tokens travel in the `Authorization` header).
- HTTP Basic, form login, and Spring Boot's default in-memory user are disabled.
- Tokens are signed with HS256 using `jwt.secret` (at least 32 bytes) and expire after `jwt.expiration-ms` (default 1 hour).
- On every request, `JwtAuthenticationFilter` loads the current `User`; blocked or inactive persons and obsolete token versions are rejected (`401`).
- Passwords are stored only as BCrypt hashes.

---

## 3. Route Authorization

**Every request requires a valid token** except the login (which issues the token) and the buyer registration (which creates the account); `OPTIONS` and `/error` are open only for technical reasons and return no business data.

| Route | Rule |
| ----- | ---- |
| `OPTIONS /**` | Permitted (CORS preflight: browsers never send the token on it). |
| `/error` | Permitted (internal error page used to write error responses). |
| `POST /api/v1/auth/login` | Permitted: it is the request that issues the token. |
| `POST /api/v1/public/buyers` | Permitted: buyer self-registration creates the account used to log in. |
| `/api/v1/public/**` (catalog search and product) | Any authenticated role. |
| `/actuator/**` (health) | Any authenticated role. |
| `/api/v1/auth/**` (logout, password) | Any authenticated role. |
| `/api/v1/buyer/**` | `ROLE_BUYER` |
| `/api/v1/seller/**` | `ROLE_SELLER` |
| `/api/v1/operator/**` | `ROLE_LOGISTICS_OPERATOR` |
| `/api/v1/admin/**` | `ROLE_ADMINISTRATOR` |
| `/api/v1/supervisor/**` | `ROLE_SUPERVISOR` |
| Anything else | Denied. |

Route authorization is the first boundary. The domain still enforces the permission matrix and the ownership rules (`ValidatePermissionsService`, `ValidateOwnershipAccessService`), e.g. a buyer cannot read another buyer's order even on a buyer route (`403`).

---

## 4. CORS Policy

- Only the configured origin is allowed: `nexusmarket.cors.allowed-origin` (environment variable `FRONTEND_ORIGIN`, development default `http://localhost:5173`).
- Methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.
- Allowed headers: `Authorization`, `Content-Type`, `Accept`, `X-Request-Id`. Exposed header: `X-Request-Id`.
- `allowCredentials = false`; wildcard origins are never used and arbitrary `Origin` values are not reflected.
- Preflight requests succeed without a token; a disallowed origin receives `403` without CORS headers.

CORS is configured once, through the `CorsConfigurationSource` bean used by the `SecurityFilterChain`.

---

## 5. Request Correlation

`RequestIdFilter` runs first in the chain: it accepts a valid `X-Request-Id` (1–64 characters `[A-Za-z0-9._-]`) or generates `req-<uuid>`, returns it in the response header, puts it in the logging MDC, and includes it as `requestId` in every error envelope.

---

## 6. Configuration

| Property | Environment variable | Default |
| -------- | -------------------- | ------- |
| `jwt.secret` | `JWT_SECRET` | Development-only value in `application.properties` |
| `jwt.expiration-ms` | `JWT_EXPIRATION_MS` | `3600000` |
| `nexusmarket.cors.allowed-origin` | `FRONTEND_ORIGIN` | `http://localhost:5173` |

Production requires HTTPS, an externally managed `JWT_SECRET`, and the real frontend origin.

---

## 7. Implementation

| Component (`adapters/security/`) | Responsibility |
| --------------------------------- | -------------- |
| `SecurityConfig` | `SecurityFilterChain`, route rules, CORS source, `PasswordEncoder`. |
| `JwtTokenServiceAdapter` | Implements the `TokenService` output port (JJWT, HS256). |
| `dtos/JwtClaimsDTO`, `mappers/JwtClaimsMapper` | Claims carried by the token (`sub`, `jti`, `ver`, `iat`, `exp`) and their mapping from the `User` and to `TokenService.TokenClaims`. |
| `JwtAuthenticationFilter` | Validates the token and asks `LoadAuthenticatedUserService` (domain) for the current `User`. |
| `dtos/AuthenticatedUserPrincipal` | Principal holding the `User` domain model and its role authority. |
| `BCryptPasswordServiceAdapter` | Implements the `PasswordService` output port. |
| `SecurityErrorHandler` | `401` / `403` with the standard error envelope. |
| `RequestIdFilter` | `X-Request-Id` correlation. |

---

## 8. Verification

- `RoleIsolationIntegrationTest`: logs in with the five roles and checks the full 5 x 5 matrix (`200` only on the role's own routes, `403` on the routes of the other four), eleven processes of one role attempted with another role (`403 FORBIDDEN`), and a buyer that cannot see the order of another buyer (`403`).
- `RestApiIntegrationTest`: `401` without token and with a tampered token, `401` for the catalog and health without token and `200` with it, login and buyer registration as the only business routes without token, `403` for another role's routes, logout invalidates the token, token claims contain no email or role, CORS preflight allowed only for the configured origin, `X-Request-Id` echo and replacement.
- `JwtTokenServiceAdapterTest`: claims, token version, tampered / foreign / expired tokens rejected, short secrets rejected at startup.
