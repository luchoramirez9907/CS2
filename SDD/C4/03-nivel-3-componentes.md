# C4 Nivel 3 — Componentes

## Propósito

Abrir el contenedor `nexusmarket-app` y mostrar sus componentes y el orden
en que participan en una petición.

## Diagrama

```mermaid
C4Component
    title Componentes — nexusmarket-app

    Container_Boundary(app, "nexusmarket-app") {
        Component(sec, "Seguridad", "adapters/security", "RequestIdFilter, JwtAuthenticationFilter, SecurityConfig, SecurityErrorHandler")
        Component(rest, "Controladores REST", "adapters/in/rest", "7 controladores (uno por rol + auth), DTOs validados, mappers, GlobalExceptionHandler")
        Component(ports, "Puertos por rol", "domain/ports/in", "PublicAccessPort, BuyerPort, SellerPort, LogisticsOperatorPort, AdministratorPort, SupervisorPort")
        Component(uc, "Adaptadores de casos de uso", "adapters/useCases", "*UseCaseImpl: implementan un puerto y llaman a los servicios")
        Component(svc, "Servicios de dominio", "domain/services", "31 servicios del catálogo + servicios internos (autorización, reserva de stock, cuentas)")
        Component(model, "Modelo de dominio", "domain/models, valueobjects, enums", "Entidades que protegen sus reglas y estados")
        Component(out, "Puertos de salida", "domain/ports/out", "12 repositorios + NotificationService, PasswordService, TokenService")
        Component(pers, "Persistencia", "adapters/out/persistence", "MySQL (JPA), MongoDB, memoria")
        Component(cfg, "Configuración", "infrastructure", "DomainBeanConfig, DemoDataSeeder, DatabaseConnectionVerifier")
    }

    Rel(sec, rest, "Deja pasar la petición autenticada")
    Rel(rest, ports, "Llama con el User autenticado")
    Rel(uc, ports, "Implementa")
    Rel(uc, svc, "Delega")
    Rel(svc, model, "Usa")
    Rel(svc, out, "Usa")
    Rel(pers, out, "Implementa")
    Rel(sec, out, "Implementa TokenService y PasswordService")
    Rel(cfg, svc, "Crea los servicios como beans")
```

## Ciclo de vida de una petición

1. `RequestIdFilter` asigna o reutiliza `X-Request-Id`.
2. `JwtAuthenticationFilter` valida el token con `TokenService` y pide el `User` a
   `LoadAuthenticatedUserService` (dominio). Si no hay usuario válido, la petición sigue sin autenticar.
3. `SecurityConfig` exige el rol del prefijo de la ruta; si falla, `SecurityErrorHandler` responde `401`/`403`.
4. El controlador valida el DTO (`@Valid`), lo convierte con su mapper y llama al puerto de su rol.
5. El `*UseCaseImpl` pasa `user.getIdentifier()` como solicitante y llama al servicio de dominio.
6. El servicio revisa permisos (`ValidatePermissionsService`) y propiedad (`ValidateOwnershipAccessService`),
   aplica las reglas sobre el modelo y guarda por los puertos de salida.
7. El controlador convierte el resultado en DTO de respuesta. Si hubo una excepción,
   `GlobalExceptionHandler` la traduce al sobre de error y deja un `WARN` con el `requestId`.
