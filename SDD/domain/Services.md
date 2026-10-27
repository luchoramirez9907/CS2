# Services

## Introduction

This document provides a conceptual overview of the services that compose the NexusMarket Marketplace Management System.

The services described here define the main business capabilities exposed by the system. At this level, each service is described only in terms of its purpose and responsibility within the domain.

To keep the service catalog concise, related create/consult/update/status-change operations over the same entity have been consolidated into a single service where the underlying business intent is the same. Each consolidated service still covers every functional objective (OBJ-01 to OBJ-12) and restriction (RG-01, RG-02, RG-03) defined in the functional specification.

The detailed definition of each service—including inputs, outputs, business rules, validations, authorization requirements, domain interactions, exceptions, persistence considerations, and technical implementation—will be documented in separate files organized by **subdomain**.

The service documentation is therefore divided conceptually into the following subdomains:

- **User Management**
- **Seller Management**
- **Buyer Management**
- **Warehouse Management**
- **Catalog Management**
- **Inventory Management**
- **Shopping Cart Management**
- **Order Management**
- **Billing Management**
- **Logistics Management**
- **Returns and Refunds Management**
- **Administrative Reporting**
- **Authentication**
- **Authorization**

---

# User Management Services

## Register User

Creates a new system participant and establishes the person's initial identity information, assigned role, and operational status. It also creates the person's access account (`User`) with the initial password provided, which must satisfy the password policy.

## Manage User

Consults, updates, and changes the operational status of an existing system participant according to the access permissions of the requesting user. Administrators and supervisors can also list the participants, filtered by role.

---

# Seller Management Services

## Register Seller

Creates a new seller and associates it with the Administrator who incorporated it into the platform. A seller cannot self-register. It also creates the person's access account (`User`) with the initial password provided, which must satisfy the password policy.

## Manage Seller

Consults, updates, and changes the operational status of an existing seller according to the applicable business rules.

---

# Buyer Management Services

## Register Buyer

Creates a new buyer and establishes the buyer's initial delivery information and commercial status. It also creates the person's access account (`User`) with the initial password provided, which must satisfy the password policy.

## Manage Buyer

Consults, updates, and changes the commercial status of an existing buyer, and retrieves the buyer's associated orders, returns, and refunds according to the access permissions of the requesting user.

---

# Warehouse Management Services

## Register Warehouse

Creates a new warehouse, either owned directly by the Marketplace or owned by a seller, according to the specified ownership.

## Manage Warehouse

Consults and updates the information of an existing warehouse according to the applicable business rules. Also lists the warehouses the requester may access (a seller only its own).

---

# Catalog Management Services

## Publish Product

Creates a new product with its catalog price, registers its variants, associates it with the requesting seller, and makes it available in the public catalog.

## Manage Product

Consults and updates the information of an existing product (name, description, price, and variants) according to the applicable business rules. A price change does not affect items already in a shopping cart or order. The public catalog can be searched by text and seller (published products only), and every product of a seller can be listed by its owner and the administrative roles.

## Change Product Status

Suspends or permanently discontinues a product, removing or hiding it from the public catalog.

---

# Inventory Management Services

## Register Inventory Movement

Records any significant movement affecting an inventory record—initial stock, reservation, release of a reservation, manual adjustment, return, or damaged stock—ensuring that stock is never reserved or reduced below zero, and that stock marked as damaged cannot be reserved.

## Consult Inventory

Retrieves the current available and reserved stock of a product at a specific warehouse. Also retrieves the movement history of that inventory record.

---

# Shopping Cart Management Services

## Manage Cart Items

Adds, updates the quantity of, removes, or clears products within a buyer's shopping cart. The unit price of each item is taken from the catalog price of the product at selection time; the buyer never sets it.

## Consult Cart

Retrieves the current contents of a buyer's shopping cart.

---

# Order Management Services

## Confirm Order

Converts a buyer's shopping cart into a formal order, inheriting the inventory reservations made while the items were in the cart, issuing its invoice, and establishing the order's initial status (`PENDING_PAYMENT`).

## Update Order Status

Registers the financial confirmation of an order and, once the associated shipment has been delivered, finalizes the order (status `DELIVERED`). A finalized order cannot be modified afterward. Orders made exclusively of digital products are delivered immediately upon payment confirmation.

## Consult Order

Retrieves the information of an order according to the access permissions of the requesting user. Also lists the accessible orders, optionally filtered by status.

---

# Billing Management Services

## Generate Invoice

Creates the billing record associated with a confirmed order, including the applicable total and tax amounts. Catalog prices already include VAT, so the invoiced total equals the order total and the tax amount is the VAT contained in it. The VAT rate is configurable (`nexusmarket.billing.tax-rate`, default 19%).

## Consult Invoice

Retrieves the invoice information associated with an order according to the access permissions of the requesting user.

---

# Logistics Management Services

## Create Shipment

Creates the shipment record associated with a paid order, establishes the originating warehouse and destination address, and assigns the responsible logistics operator.

## Update Shipment Status

Registers the physical dispatch of a shipment from the originating warehouse and its subsequent delivery confirmation to the buyer. A failed delivery returns the shipment to the warehouse, from where it can be dispatched again.

## Consult Shipment

Retrieves the current status and tracking information of a shipment according to the access permissions of the requesting user.

---

# Returns and Refunds Management Services

## Request Return

Creates a return request for a buyer over a delivered order, recording the reason provided by the buyer. An order holds at most one active return (a new request is allowed only after the previous one was rejected).

## Resolve Return

Approves or rejects a return request after applying the required business validations, completes an approved return by reinstating the returned products into stock, and retrieves or lists return requests according to the access permissions of the requesting user.

## Process Refund

Creates the monetary reimbursement associated with an approved return, or records the decision of rejecting it, and retrieves refund information according to the access permissions of the requesting user.

---

# Administrative Reporting Services

## Consult Commercial Report

Retrieves consolidated information about orders, invoices, revenue, and inventory levels across warehouses for administrative review.

## Consult Seller Performance Report

Retrieves consolidated information about products, orders, and returns associated with a seller.

---

# Authentication Services

## Authenticate User

Validates the credentials of a participant (email and password) and issues its access token; ends the session of the authenticated participant by invalidating every previously issued token; and changes its password after verifying the current one. A blocked or inactive participant cannot authenticate.

---

# Authorization Services

## Validate Permissions

Determines whether a user has permission to perform a specific business operation based on the user's role and status, ensuring no participant operates outside the boundaries of its assigned role.

## Validate Ownership Access

Determines whether a user is authorized to access or operate on information belonging to a specific buyer, seller, warehouse, or order that it owns or manages.

---

# Service Traceability

The following table maps every service of this catalog to the Domain Service class that implements it (`domain/services/`), its operations, and the Role Input Ports whose implementations (`adapters/useCases/*UseCaseImpl`) call it. There are no per-operation input interfaces: the Use Case adapters inject the Domain Services directly. Following the catalog, there is exactly one Domain Service class per catalog service (31), grouped in one subpackage per subdomain (`domain/services/user/`, `domain/services/order/`, ...). The Role Input Ports that group these operations by role are documented in `Input-ports.md`.

| Subdomain | Catalog Service | Domain Service | Operations | Used by Role Input Ports |
| --------- | --------------- | -------------- | ---------- | ------------------------ |
| User | Register User | `RegisterUserService` | `registerUser` | `AdministratorPort` |
| User | Manage User | `ManageUserService` | `consultUser`, `updateUser`, `changeUserStatus`, `listUsers` | `AdministratorPort`, `LogisticsOperatorPort`, `SupervisorPort` |
| Seller | Register Seller | `RegisterSellerService` | `registerSeller` | `AdministratorPort` |
| Seller | Manage Seller | `ManageSellerService` | `consultSeller`, `updateSeller`, `changeSellerStatus` | `AdministratorPort`, `SellerPort`, `SupervisorPort` |
| Buyer | Register Buyer | `RegisterBuyerService` | `registerBuyer` | `AdministratorPort`, `PublicAccessPort` |
| Buyer | Manage Buyer | `ManageBuyerService` | `consultBuyer`, `updateBuyer`, `changeCommercialStatus`, `consultBuyerOrders`, `consultBuyerReturns`, `consultBuyerRefunds` | `AdministratorPort`, `BuyerPort`, `SupervisorPort` |
| Warehouse | Register Warehouse | `RegisterWarehouseService` | `registerWarehouse` | `AdministratorPort`, `SellerPort` |
| Warehouse | Manage Warehouse | `ManageWarehouseService` | `consultWarehouse`, `updateWarehouse`, `listWarehouses` | `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` |
| Catalog | Publish Product | `PublishProductService` | `publishProduct` | `SellerPort` |
| Catalog | Manage Product | `ManageProductService` | `consultProduct`, `updateProduct`, `searchCatalog`, `consultSellerProducts` | All (catalog consultation) |
| Catalog | Change Product Status | `ChangeProductStatusService` | `changeProductStatus` | `AdministratorPort`, `SellerPort` |
| Inventory | Register Inventory Movement | `RegisterInventoryMovementService` | `registerMovement`, `registerDamagedStock` | `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort` |
| Inventory | Consult Inventory | `ConsultInventoryService` | `consultInventory`, `consultMovementHistory` | `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` |
| Shopping Cart | Manage Cart Items | `ManageCartItemsService` | `addItemToCart`, `updateItemQuantity`, `removeItem`, `clearCart` | `BuyerPort` |
| Shopping Cart | Consult Cart | `ConsultCartService` | `consultCart` | `BuyerPort` |
| Order | Confirm Order | `ConfirmOrderService` | `checkout` | `BuyerPort` |
| Order | Update Order Status | `UpdateOrderStatusService` | `confirmPayment`, `finalizeOrder` | `AdministratorPort`, `LogisticsOperatorPort` |
| Order | Consult Order | `ConsultOrderService` | `consultOrder`, `listOrders` | `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` |
| Billing | Generate Invoice | `GenerateInvoiceService` | `generateInvoice` | `AdministratorPort` |
| Billing | Consult Invoice | `ConsultInvoiceService` | `consultInvoice` | `AdministratorPort`, `BuyerPort`, `SellerPort`, `SupervisorPort` |
| Logistics | Create Shipment | `CreateShipmentService` | `createShipment` | `AdministratorPort`, `LogisticsOperatorPort` |
| Logistics | Update Shipment Status | `UpdateShipmentStatusService` | `dispatchShipment`, `confirmDelivery`, `registerShipmentReturn` | `AdministratorPort`, `LogisticsOperatorPort` |
| Logistics | Consult Shipment | `ConsultShipmentService` | `consultShipment`, `consultTracking` | `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` |
| Returns & Refunds | Request Return | `RequestReturnService` | `requestReturn` | `BuyerPort` |
| Returns & Refunds | Resolve Return | `ResolveReturnService` | `approveReturn`, `rejectReturn`, `completeReturn`, `consultReturn`, `listReturns` | `AdministratorPort`, `BuyerPort`, `SellerPort`, `SupervisorPort` |
| Returns & Refunds | Process Refund | `ProcessRefundService` | `processRefund`, `rejectRefund`, `consultRefund` | `AdministratorPort`, `BuyerPort`, `SupervisorPort` |
| Reporting | Consult Commercial Report | `ConsultCommercialReportService` | `consultCommercialReport` | `AdministratorPort`, `SupervisorPort` |
| Reporting | Consult Seller Performance Report | `ConsultSellerPerformanceReportService` | `consultSellerPerformanceReport` | `AdministratorPort`, `SellerPort`, `SupervisorPort` |
| Authentication | Authenticate User | `AuthenticateUserService` | `login`, `logout`, `changePassword` | `PublicAccessPort` |
| Authorization | Validate Permissions | `ValidatePermissionsService` | `hasPermission`, `requirePermission` | — (used by every other service) |
| Authorization | Validate Ownership Access | `ValidateOwnershipAccessService` | `canAccess*` / `require*Access` (person, buyer, seller, warehouse, product, order) | — (used by every other service) |

`InventoryReservationService` is an internal domain service (not called by any Role Input Port): it coordinates stock reservations, releases, sale exits, and returns across the distributed inventory of a product, and is used by `ManageCartItemsService`, `UpdateShipmentStatusService`, and `ResolveReturnService`. `ServiceValidations` holds the input validations shared by the domain services.

`UserAccountService` is an internal domain service used by `RegisterUserService`, `RegisterSellerService`, and `RegisterBuyerService`: it validates the password policy, hashes the password through the `PasswordService` output port, and stores the new `User` through the `UserRepository` output port.

`LoadAuthenticatedUserService` is an internal domain service used by the security adapter (`JwtAuthenticationFilter`): it resolves the authenticated `User` of each request from the identity carried by the access token (person identifier and token version), and only returns it when the person is active and the token version is still current.

---

# Permission Matrix

`ValidatePermissionsService` validates every operation against the following matrix (enum `BusinessOperation`). A role not listed for an operation is rejected. Ownership rules are applied on top of this matrix (`ValidateOwnershipAccessService`): buyers and sellers only access their own information; sellers access the orders containing their products; logistics operators access the orders whose shipment is assigned to them; administrators and supervisors may consult every entity.

| Operation | Administrator | Supervisor | Buyer | Seller | Logistics Operator |
| --------- | :-----------: | :--------: | :---: | :----: | :----------------: |
| Register user | ✔ | | | | |
| Consult user | ✔ | ✔ | ✔ | ✔ | ✔ |
| Update user | ✔ | | ✔ | ✔ | ✔ |
| Change user status | ✔ | | | | |
| Register seller | ✔ | | | | |
| Consult seller | ✔ | ✔ | | ✔ | |
| Update seller | ✔ | | | ✔ | |
| Change seller status | ✔ | | | | |
| Register buyer | ✔ | | | | |
| Consult buyer | ✔ | ✔ | ✔ | | |
| Update buyer | ✔ | | ✔ | | |
| Change buyer commercial status | ✔ | | | | |
| Register warehouse | ✔ | | | ✔ | |
| Consult warehouse | ✔ | ✔ | | ✔ | ✔ |
| Update warehouse | ✔ | | | ✔ | |
| Publish product | | | | ✔ | |
| Consult product | ✔ | ✔ | ✔ | ✔ | ✔ |
| Update product | | | | ✔ | |
| Change product status | ✔ | | | ✔ | |
| Register inventory movement | ✔ | | | ✔ | ✔ |
| Consult inventory | ✔ | ✔ | | ✔ | ✔ |
| Manage / consult cart | | | ✔ | | |
| Confirm order | | | ✔ | | |
| Confirm order payment | ✔ | | | | |
| Finalize order | ✔ | | | | ✔ |
| Consult order | ✔ | ✔ | ✔ | ✔ | ✔ |
| Generate invoice | ✔ | | | | |
| Consult invoice | ✔ | ✔ | ✔ | ✔ | |
| Create shipment / update shipment status | ✔ | | | | ✔ |
| Consult shipment | ✔ | ✔ | ✔ | ✔ | ✔ |
| Request return | | | ✔ | | |
| Resolve return | ✔ | | | | |
| Consult return | ✔ | ✔ | ✔ | ✔ | |
| Process refund | ✔ | | | | |
| Consult refund | ✔ | ✔ | ✔ | | |
| Consult commercial report | ✔ | ✔ | | | |
| Consult seller performance report | ✔ | ✔ | | ✔ | |

---

# Service Organization

The services described in this document provide the **high-level service catalog** of the system.

The detailed specification of each operation (actor, domain service method, input, validations, authorization, domain behavior, persistence, exceptions, output ports, and processing flow) is maintained in one file per subdomain, in `domain/services/`:

| File | Subdomain |
| ---- | --------- |
| `services/user-services.md` | User Management (also defines the conventions shared by every file) |
| `services/seller-services.md` | Seller Management |
| `services/buyer-services.md` | Buyer Management |
| `services/warehouse-services.md` | Warehouse Management |
| `services/catalog-services.md` | Catalog Management |
| `services/inventory-services.md` | Inventory Management (and the internal reservation service) |
| `services/shopping-cart-services.md` | Shopping Cart Management |
| `services/order-services.md` | Order Management |
| `services/billing-services.md` | Billing Management |
| `services/logistics-services.md` | Logistics Management |
| `services/returns-refunds-services.md` | Returns and Refunds Management |
| `services/administrative-reporting-services.md` | Administrative Reporting |
| `services/authorization-services.md` | Authorization and Authentication |
