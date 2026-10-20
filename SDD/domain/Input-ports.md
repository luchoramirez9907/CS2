# Input Ports (Role-Based Use Case Interfaces)

## 1. Overview

Input Ports define the application entry contracts through which external delivery mechanisms (REST controllers) interact with the NexusMarket domain.

The input ports are the **Role Input Ports** (`*Port`): one interface per system role (`SystemRole`), plus a public port for onboarding, authentication and the catalog. Each exposes exactly the operations that role may perform. REST controllers depend **only** on these ports.

There are no per-operation input interfaces. Each Role Input Port is implemented by a Use Case Adapter that injects the Domain Services of the service catalog (`Services.md`) directly, one class per catalog service (for example `ConfirmOrderService` for "Confirm Order").

```text
REST Controller (adapters/in/rest)
      │  calls
      ▼
Role Input Port (domain/ports/in/*Port)
      ▲
      │  implemented by
Use Case Adapter (adapters/useCases/*UseCaseImpl)
      │  calls
      ▼
Domain Service (domain/services/<subdomain>/*Service)
```

---

# 2. General Principles

### 2.1 Role Isolation

Each system role interacts with the application only through its Role Input Port:

| Role Input Port | Role (`SystemRole`) | Authentication |
| --------------- | ------------------- | -------------- |
| `PublicAccessPort` | — | Not required only for `login` and `registerBuyer`; any authenticated role for the rest |
| `BuyerPort` | `BUYER` | Required |
| `SellerPort` | `SELLER` | Required |
| `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` | Required |
| `AdministratorPort` | `ADMINISTRATOR` | Required |
| `SupervisorPort` | `SUPERVISOR` | Required |

A role port never exposes an operation that the role is not allowed to perform according to the permission matrix (`Services.md`, *Permission Matrix*). For example, `SupervisorPort` only contains consultations, and only `AdministratorPort` can approve returns and refunds.

The authorization rules are still enforced by the domain (`ValidatePermissionsService`, `ValidateOwnershipAccessService`): the role ports are the first boundary, not the only one.

### 2.2 Authenticated User Parameter

Every method of an authenticated port receives the authenticated **`User`** domain model as its first parameter. The REST layer rebuilds it from the access token and passes it to the port; the port never receives identifiers of the requester from the client (such as a `buyerId` in the request body).

Operations over the requester's own information use the `My` prefix (`consultMyProfile`, `consultMyOrders`) and take no additional identifier: the owner is always the authenticated user.

### 2.3 Parameters

- **References** to existing entities are passed as identifiers (`String orderId`), because the entity is loaded and validated by the domain service.
- **Business values** are passed as Domain Models and Value Objects (`Address`, `ProductStatus`, `InventoryMovementType`, `Product`), never as DTOs or raw catalog strings.
- **Money** is passed as `BigDecimal`.

### 2.4 Mapping to the Evaluation Guide

The evaluation guide lists role ports named after the banking example (`NaturalCustomerPort`, `TellerEmployeePort`, …). NexusMarket applies the same rule — *input ports organized exclusively by role* — with the roles of its own domain (`SystemRole`: Buyer, Seller, Logistics Operator, Administrator, Supervisor).

---

# 3. Role Input Port Definitions

## 3.1 PublicAccessPort (onboarding and authentication)

```java
public interface PublicAccessPort {
    Buyer registerBuyer(String identifier, String fullName, String email, Address primaryAddress,
                        String password);
    Product consultCatalogProduct(String productId);
    AuthenticationResult login(String email, String password);
    void logout(User user);
    void changeMyPassword(User user, String currentPassword, String newPassword);
    List<Product> searchCatalog(String text, String sellerId);
}
```

| Method | Authentication | Delegates to |
| ------ | -------------- | ------------ |
| `registerBuyer` | Not required | `RegisterBuyerService.registerBuyer` (self-registration, no performer) |
| `consultCatalogProduct` | Any role | `ManageProductService.consultProduct` (anonymous: published products only) |
| `login` | Not required | `AuthenticateUserService.login` (returns the `User` and its access token) |
| `logout` | Any role | `AuthenticateUserService.logout` (invalidates previous tokens) |
| `changeMyPassword` | Any role | `AuthenticateUserService.changePassword` |
| `searchCatalog` | Any role | `ManageProductService.searchCatalog` (published products only) |

---

## 3.2 BuyerPort (`BUYER`)

```java
public interface BuyerPort {
    Buyer consultMyProfile(User user);
    Buyer updateMyProfile(User user, String fullName, String email, Address primaryAddress,
                          List<Address> additionalAddresses);
    Product consultProduct(User user, String productId);
    ShoppingCart addItemToCart(User user, String productId, int quantity);
    ShoppingCart updateCartItemQuantity(User user, String productId, int quantity);
    ShoppingCart removeCartItem(User user, String productId);
    ShoppingCart clearCart(User user);
    Optional<ShoppingCart> consultMyCart(User user);
    Order checkout(User user);
    Order consultOrder(User user, String orderId);
    List<Order> consultMyOrders(User user);
    Invoice consultInvoice(User user, String orderId);
    Shipment consultShipment(User user, String orderId);
    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);
    Return requestReturn(User user, String orderId, String reason);
    Return consultReturn(User user, String returnId);
    List<Return> consultMyReturns(User user);
    Refund consultRefund(User user, String returnId);
    List<Refund> consultMyRefunds(User user);
}
```

| Method | Delegates to |
| ------ | ------------ |
| `consultMyProfile` / `updateMyProfile` | `ManageBuyerService.consultBuyer` / `updateBuyer` (buyer = user) |
| `consultProduct` | `ManageProductService.consultProduct` |
| `addItemToCart` | `ManageCartItemsService.addItemToCart` (active cart resolved by the domain) |
| `updateCartItemQuantity` / `removeCartItem` / `clearCart` | `ManageCartItemsService` |
| `consultMyCart` | `ConsultCartService.consultCart` |
| `checkout` | `ConfirmOrderService.checkout` (active cart) |
| `consultOrder` | `ConsultOrderService.consultOrder` |
| `consultMyOrders` / `consultMyReturns` / `consultMyRefunds` | `ManageBuyerService.consultBuyerOrders` / `consultBuyerReturns` / `consultBuyerRefunds` |
| `consultInvoice` | `ConsultInvoiceService.consultInvoice` |
| `consultShipment` / `consultTracking` | `ConsultShipmentService` |
| `requestReturn` | `RequestReturnService.requestReturn` |
| `consultReturn` | `ResolveReturnService.consultReturn` |
| `consultRefund` | `ProcessRefundService.consultRefund` |

---

## 3.3 SellerPort (`SELLER`)

```java
public interface SellerPort {
    Seller consultMyProfile(User user);
    Seller updateMyProfile(User user, String fullName, String email);
    Warehouse registerWarehouse(User user, String identifier, String name, Address address);
    Warehouse consultWarehouse(User user, String warehouseId);
    Warehouse updateWarehouse(User user, String warehouseId, String name, Address address);
    Product publishProduct(User user, Product product);
    Product consultProduct(User user, String productId);
    Product updateProduct(User user, String productId, String name, String description,
                          BigDecimal price, List<ProductVariant> variants);
    Product changeProductStatus(User user, String productId, ProductStatus status);
    Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                        InventoryMovementType type, int quantity);
    Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity);
    Inventory consultInventory(User user, String productId, String warehouseId);
    Order consultOrder(User user, String orderId);
    Invoice consultInvoice(User user, String orderId);
    Shipment consultShipment(User user, String orderId);
    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);
    Return consultReturn(User user, String returnId);
    SellerPerformanceReport consultMyPerformanceReport(User user);
    List<Product> consultMyProducts(User user);
    List<Warehouse> consultMyWarehouses(User user);
    List<Order> consultMyOrders(User user, OrderStatus status);
    List<Return> consultMyReturns(User user, ReturnStatus status);
    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
}
```

| Method | Delegates to |
| ------ | ------------ |
| `consultMyProfile` / `updateMyProfile` | `ManageSellerService.consultSeller` / `updateSeller` (seller = user) |
| `registerWarehouse` | `RegisterWarehouseService.registerWarehouse` (owner = user) |
| `consultWarehouse` / `updateWarehouse` | `ManageWarehouseService` |
| `publishProduct` | `PublishProductService.publishProduct` (seller = user) |
| `consultProduct` / `updateProduct` | `ManageProductService` |
| `changeProductStatus` | `ChangeProductStatusService.changeProductStatus` |
| `registerInventoryMovement` / `registerDamagedStock` | `RegisterInventoryMovementService` |
| `consultInventory` | `ConsultInventoryService.consultInventory` |
| `consultOrder` | `ConsultOrderService.consultOrder` (orders containing the seller's products) |
| `consultInvoice` | `ConsultInvoiceService.consultInvoice` |
| `consultShipment` / `consultTracking` | `ConsultShipmentService` |
| `consultReturn` | `ResolveReturnService.consultReturn` |
| `consultMyPerformanceReport` | `ConsultSellerPerformanceReportService` (seller = user) |
| `consultMyProducts` | `ManageProductService.consultSellerProducts` (seller = user) |
| `consultMyWarehouses` | `ManageWarehouseService.listWarehouses` (own warehouses) |
| `consultMyOrders` | `ConsultOrderService.listOrders` (orders containing the seller's products) |
| `consultMyReturns` | `ResolveReturnService.listReturns` |
| `consultInventoryMovements` | `ConsultInventoryService.consultMovementHistory` |

---

## 3.4 LogisticsOperatorPort (`LOGISTICS_OPERATOR`)

```java
public interface LogisticsOperatorPort {
    Person consultMyProfile(User user);
    Person updateMyProfile(User user, String fullName, String email);
    Warehouse consultWarehouse(User user, String warehouseId);
    Product consultProduct(User user, String productId);
    Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                        InventoryMovementType type, int quantity);
    Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity);
    Inventory consultInventory(User user, String productId, String warehouseId);
    Shipment createShipment(User user, String orderId, String originWarehouseId);
    Shipment dispatchShipment(User user, String orderId);
    Shipment confirmDelivery(User user, String orderId);
    Shipment consultShipment(User user, String orderId);
    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);
    Order consultOrder(User user, String orderId);
    Order finalizeOrder(User user, String orderId);
    List<Warehouse> listWarehouses(User user);
    List<Order> consultAssignedOrders(User user, OrderStatus status);
    Shipment registerShipmentReturn(User user, String orderId, String reason);
    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
}
```

| Method | Delegates to |
| ------ | ------------ |
| `consultMyProfile` / `updateMyProfile` | `ManageUserService.consultUser` / `updateUser` (user = self) |
| `consultWarehouse` | `ManageWarehouseService.consultWarehouse` |
| `consultProduct` | `ManageProductService.consultProduct` |
| `registerInventoryMovement` / `registerDamagedStock` | `RegisterInventoryMovementService` |
| `consultInventory` | `ConsultInventoryService.consultInventory` |
| `createShipment` | `CreateShipmentService.createShipment` (operator = user) |
| `dispatchShipment` / `confirmDelivery` | `UpdateShipmentStatusService` |
| `consultShipment` / `consultTracking` | `ConsultShipmentService` |
| `consultOrder` | `ConsultOrderService.consultOrder` |
| `finalizeOrder` | `UpdateOrderStatusService.finalizeOrder` |
| `listWarehouses` | `ManageWarehouseService.listWarehouses` |
| `consultAssignedOrders` | `ConsultOrderService.listOrders` (orders whose shipment is assigned to the operator) |
| `registerShipmentReturn` | `UpdateShipmentStatusService.registerShipmentReturn` |
| `consultInventoryMovements` | `ConsultInventoryService.consultMovementHistory` |

---

## 3.5 AdministratorPort (`ADMINISTRATOR`)

```java
public interface AdministratorPort {
    Person registerUser(User user, String identifier, String fullName, String email, SystemRole role,
                        String password);
    Person consultUser(User user, String userId);
    Person updateUser(User user, String userId, String fullName, String email);
    Person changeUserStatus(User user, String userId, UserStatus status);
    Seller registerSeller(User user, String identifier, String fullName, String email, String password);
    Seller consultSeller(User user, String sellerId);
    Seller updateSeller(User user, String sellerId, String fullName, String email);
    Seller changeSellerStatus(User user, String sellerId, UserStatus status);
    Buyer registerBuyer(User user, String identifier, String fullName, String email, Address primaryAddress,
                        String password);
    Buyer consultBuyer(User user, String buyerId);
    Buyer updateBuyer(User user, String buyerId, String fullName, String email, Address primaryAddress,
                      List<Address> additionalAddresses);
    Buyer changeBuyerCommercialStatus(User user, String buyerId, BuyerCommercialStatus status);
    List<Order> consultBuyerOrders(User user, String buyerId);
    List<Return> consultBuyerReturns(User user, String buyerId);
    List<Refund> consultBuyerRefunds(User user, String buyerId);
    Warehouse registerWarehouse(User user, String identifier, String name, Address address,
                                String ownerSellerId);
    Warehouse consultWarehouse(User user, String warehouseId);
    Warehouse updateWarehouse(User user, String warehouseId, String name, Address address);
    Product consultProduct(User user, String productId);
    Product changeProductStatus(User user, String productId, ProductStatus status);
    Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                        InventoryMovementType type, int quantity);
    Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity);
    Inventory consultInventory(User user, String productId, String warehouseId);
    Order consultOrder(User user, String orderId);
    Order confirmOrderPayment(User user, String orderId);
    Order finalizeOrder(User user, String orderId);
    Invoice generateInvoice(User user, String orderId);
    Invoice consultInvoice(User user, String orderId);
    Shipment createShipment(User user, String orderId, String originWarehouseId, String logisticsOperatorId);
    Shipment dispatchShipment(User user, String orderId);
    Shipment confirmDelivery(User user, String orderId);
    Shipment consultShipment(User user, String orderId);
    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);
    Return approveReturn(User user, String returnId);
    Return rejectReturn(User user, String returnId);
    Return completeReturn(User user, String returnId);
    Return consultReturn(User user, String returnId);
    Refund processRefund(User user, String returnId, BigDecimal amount);
    Refund consultRefund(User user, String returnId);
    CommercialReport consultCommercialReport(User user);
    SellerPerformanceReport consultSellerPerformanceReport(User user, String sellerId);
    List<Person> listUsers(User user, SystemRole role);
    List<Warehouse> listWarehouses(User user);
    List<Product> consultSellerProducts(User user, String sellerId);
    List<Order> listOrders(User user, OrderStatus status);
    List<Return> listReturns(User user, ReturnStatus status);
    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
    Shipment registerShipmentReturn(User user, String orderId, String reason);
    Refund rejectRefund(User user, String returnId);
}
```

| Method | Delegates to |
| ------ | ------------ |
| `registerUser` | `RegisterUserService.registerUser` |
| `consultUser` / `updateUser` / `changeUserStatus` | `ManageUserService` |
| `registerSeller` | `RegisterSellerService.registerSeller` |
| `consultSeller` / `updateSeller` / `changeSellerStatus` | `ManageSellerService` |
| `registerBuyer` | `RegisterBuyerService.registerBuyer` (performer = user) |
| `consultBuyer` / `updateBuyer` / `changeBuyerCommercialStatus` / `consultBuyerOrders` / `consultBuyerReturns` / `consultBuyerRefunds` | `ManageBuyerService` |
| `registerWarehouse` | `RegisterWarehouseService.registerWarehouse` |
| `consultWarehouse` / `updateWarehouse` | `ManageWarehouseService` |
| `consultProduct` | `ManageProductService.consultProduct` |
| `changeProductStatus` | `ChangeProductStatusService.changeProductStatus` |
| `registerInventoryMovement` / `registerDamagedStock` | `RegisterInventoryMovementService` |
| `consultInventory` | `ConsultInventoryService.consultInventory` |
| `consultOrder` | `ConsultOrderService.consultOrder` |
| `confirmOrderPayment` / `finalizeOrder` | `UpdateOrderStatusService.confirmPayment` / `finalizeOrder` |
| `generateInvoice` | `GenerateInvoiceService.generateInvoice` |
| `consultInvoice` | `ConsultInvoiceService.consultInvoice` |
| `createShipment` | `CreateShipmentService.createShipment` |
| `dispatchShipment` / `confirmDelivery` | `UpdateShipmentStatusService` |
| `consultShipment` / `consultTracking` | `ConsultShipmentService` |
| `approveReturn` | `ResolveReturnService.approveReturn` |
| `rejectReturn` / `completeReturn` / `consultReturn` | `ResolveReturnService` |
| `processRefund` / `consultRefund` | `ProcessRefundService` |
| `consultCommercialReport` | `ConsultCommercialReportService` |
| `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` |
| `listUsers` | `ManageUserService.listUsers` |
| `listWarehouses` | `ManageWarehouseService.listWarehouses` |
| `consultSellerProducts` | `ManageProductService.consultSellerProducts` |
| `listOrders` | `ConsultOrderService.listOrders` |
| `listReturns` | `ResolveReturnService.listReturns` |
| `consultInventoryMovements` | `ConsultInventoryService.consultMovementHistory` |
| `registerShipmentReturn` | `UpdateShipmentStatusService.registerShipmentReturn` |
| `rejectRefund` | `ProcessRefundService.rejectRefund` |

---

## 3.6 SupervisorPort (`SUPERVISOR`, read-only)

```java
public interface SupervisorPort {
    Person consultUser(User user, String userId);
    Seller consultSeller(User user, String sellerId);
    Buyer consultBuyer(User user, String buyerId);
    List<Order> consultBuyerOrders(User user, String buyerId);
    List<Return> consultBuyerReturns(User user, String buyerId);
    List<Refund> consultBuyerRefunds(User user, String buyerId);
    Warehouse consultWarehouse(User user, String warehouseId);
    Product consultProduct(User user, String productId);
    Inventory consultInventory(User user, String productId, String warehouseId);
    Order consultOrder(User user, String orderId);
    Invoice consultInvoice(User user, String orderId);
    Shipment consultShipment(User user, String orderId);
    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);
    Return consultReturn(User user, String returnId);
    Refund consultRefund(User user, String returnId);
    CommercialReport consultCommercialReport(User user);
    SellerPerformanceReport consultSellerPerformanceReport(User user, String sellerId);
    List<Person> listUsers(User user, SystemRole role);
    List<Warehouse> listWarehouses(User user);
    List<Product> consultSellerProducts(User user, String sellerId);
    List<Order> listOrders(User user, OrderStatus status);
    List<Return> listReturns(User user, ReturnStatus status);
    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
}
```

Every method delegates to the consultation operation of the same name in the corresponding Domain Service (`ManageUserService.consultUser`, `ManageSellerService.consultSeller`, `ManageBuyerService.consultBuyer`, …). The port contains no create, update, approve, or status-change operation.

---

# 4. Domain Services behind the ports

The Role Input Ports do not hide per-operation interfaces: their implementations call the Domain Services directly. The complete list of services, their operations and the Role Input Ports that use each one are documented in `Services.md`, section *Service Traceability*.

`ValidatePermissionsService` and `ValidateOwnershipAccessService` are internal authorization services used by the other Domain Services; they are not called by any Role Input Port. `LoadAuthenticatedUserService` is used by the security adapter to resolve the authenticated `User` of each request.

---

# 5. Implementation

Each Role Input Port is implemented by one class in `adapters/useCases/` (`PublicAccessUseCaseImpl`, `BuyerUseCaseImpl`, `SellerUseCaseImpl`, `LogisticsOperatorUseCaseImpl`, `AdministratorUseCaseImpl`, `SupervisorUseCaseImpl`). These classes inject the Domain Services directly, pass `user.getIdentifier()` as the requester, and contain no business rules. See `Adapters/Use-cases-adapters.md`.

The REST controllers depend only on the Role Input Ports and receive the authenticated `User` resolved from the JWT by `LoadAuthenticatedUserService` (see `Adapters/Rest-adapters.md`). The complete order lifecycle is verified through the ports (`RoleUseCasesIntegrationTest`) and over HTTP (`RestApiIntegrationTest`).
