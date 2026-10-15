# Use Cases Adapters Specification

## 1. Overview

Use Case Adapters reside in `application/adapters/useCases/`. They are the concrete implementations of the **Role Input Ports** defined in `application/domain/ports/in/` (see `domain/Input-ports.md`).

They bridge the incoming calls of a role (from REST controllers or other entry points) directly to the **Domain Services** (`application/domain/services/`), one class per catalog service.

---

## 2. Architectural Role and Injections

Every Use Case Adapter:

1. Implements exactly one Role Input Port (`domain/ports/in/*Port`).
2. Injects the **Domain Service classes** it needs (for example `ConfirmOrderService`). The Domain Services are framework-free and are declared as Spring beans in `infrastructure/config/DomainBeanConfig`.
3. Passes the authenticated `User` to the domain as the requester (`user.getIdentifier()`), and fixes the parameters that the role implies (e.g. the buyer of `checkout` or the owner of a seller warehouse is always the authenticated user).
4. Contains **no business rules**: validations, authorization, and state transitions remain in the Domain Services.

```text
REST Controller (adapters/in/rest)
      │  calls Role Input Port
      ▼
Role Input Port (domain/ports/in/*Port)
      ▲
      │  implemented by
Use Case Adapter (adapters/useCases/*UseCaseImpl)
      │  calls
      ▼
Domain Service (domain/services/<subdomain>/*Service) ──> Output Ports (domain/ports/out)
```

There are no per-operation input interfaces: the Role Input Port is the only input contract, and its implementation calls the Domain Services directly. This keeps one entry interface per role and avoids a second interface layer that would only repeat the methods of each service.

---

## 3. Structure and Naming Conventions

| Role Input Port | Use Case Adapter | Injected Domain Services |
| :--- | :--- | :--- |
| `PublicAccessPort` | `PublicAccessUseCaseImpl` | `RegisterBuyerService`, `ManageProductService`, `AuthenticateUserService` |
| `BuyerPort` | `BuyerUseCaseImpl` | `ManageBuyerService`, `ManageProductService`, `ManageCartItemsService`, `ConsultCartService`, `ConfirmOrderService`, `ConsultOrderService`, `ConsultInvoiceService`, `ConsultShipmentService`, `RequestReturnService`, `ResolveReturnService`, `ProcessRefundService` |
| `SellerPort` | `SellerUseCaseImpl` | `ManageSellerService`, `RegisterWarehouseService`, `ManageWarehouseService`, `PublishProductService`, `ManageProductService`, `ChangeProductStatusService`, `RegisterInventoryMovementService`, `ConsultInventoryService`, `ConsultOrderService`, `ConsultInvoiceService`, `ConsultShipmentService`, `ResolveReturnService`, `ConsultSellerPerformanceReportService` |
| `LogisticsOperatorPort` | `LogisticsOperatorUseCaseImpl` | `ManageUserService`, `ManageWarehouseService`, `ManageProductService`, `RegisterInventoryMovementService`, `ConsultInventoryService`, `CreateShipmentService`, `UpdateShipmentStatusService`, `ConsultShipmentService`, `ConsultOrderService`, `UpdateOrderStatusService` |
| `AdministratorPort` | `AdministratorUseCaseImpl` | `RegisterUserService`, `ManageUserService`, `RegisterSellerService`, `ManageSellerService`, `RegisterBuyerService`, `ManageBuyerService`, `RegisterWarehouseService`, `ManageWarehouseService`, `ManageProductService`, `ChangeProductStatusService`, `RegisterInventoryMovementService`, `ConsultInventoryService`, `ConsultOrderService`, `UpdateOrderStatusService`, `GenerateInvoiceService`, `ConsultInvoiceService`, `CreateShipmentService`, `UpdateShipmentStatusService`, `ConsultShipmentService`, `ResolveReturnService`, `ProcessRefundService`, `ConsultCommercialReportService`, `ConsultSellerPerformanceReportService` |
| `SupervisorPort` | `SupervisorUseCaseImpl` | Consultation operations only: `ManageUserService`, `ManageSellerService`, `ManageBuyerService`, `ManageWarehouseService`, `ManageProductService`, `ConsultInventoryService`, `ConsultOrderService`, `ConsultInvoiceService`, `ConsultShipmentService`, `ResolveReturnService`, `ProcessRefundService`, `ConsultCommercialReportService`, `ConsultSellerPerformanceReportService` |

The catalog service implemented by each Domain Service, and every Role Input Port that uses it, is listed in `domain/Services.md`, section *Service Traceability*.

### Parameters fixed by the role

| Adapter method | Fixed parameter |
| -------------- | --------------- |
| `PublicAccessUseCaseImpl.registerBuyer` | No performer (self-registration). |
| `PublicAccessUseCaseImpl.consultCatalogProduct` | No requester (only published products are visible). |
| `BuyerUseCaseImpl.*My*`, `addItemToCart`, `checkout` | Buyer = authenticated user; `cartId = null` resolves the buyer's active cart. |
| `SellerUseCaseImpl.registerWarehouse` | Owner seller = authenticated user. |
| `SellerUseCaseImpl.consultMyPerformanceReport` | Seller = authenticated user. |
| `LogisticsOperatorUseCaseImpl.createShipment` | Assigned operator = authenticated user. |
| `LogisticsOperatorUseCaseImpl.consultMyProfile` / `updateMyProfile` | Consulted person = authenticated user. |

---

## 4. Code Pattern Example (Java / Spring)

```java
package application.adapters.useCases;

@Service
public class LogisticsOperatorUseCaseImpl implements LogisticsOperatorPort {

    private final CreateShipmentService createShipmentService;
    private final UpdateOrderStatusService updateOrderStatusService;
    // ... other Domain Services

    @Override
    public Shipment createShipment(User user, String orderId, String originWarehouseId) {
        // The shipment is assigned to the authenticated operator.
        return createShipmentService.createShipment(user.getIdentifier(), orderId, originWarehouseId,
                user.getIdentifier());
    }

    @Override
    public Order finalizeOrder(User user, String orderId) {
        return updateOrderStatusService.finalizeOrder(user.getIdentifier(), orderId);
    }
}
```

---

## 5. Verification

`RoleUseCasesIntegrationTest` (`src/test/java/application/adapters/useCases/`) loads the Spring context with the `memory` and `demo` profiles and runs the complete lifecycle exclusively through the Role Input Ports: public catalog, onboarding, cart, checkout, payment, shipment, delivery, finalization, return, refund, and the read-only reports. It also verifies that a buyer cannot access another buyer's order.
