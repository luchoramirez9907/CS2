# Domain Model

## Introduction

The Domain Model represents the core business entities of the NexusMarket Marketplace Management System. These entities encapsulate the business rules, data, relationships, and lifecycle concepts described in the system specification.

The model follows Object-Oriented Design and Domain-Driven Design (DDD) principles. Inheritance is used to represent genuine domain specialization, while explicit object relationships are preferred over generic identifier fields.

The model distinguishes between:

* **Persons**, which represent the identifiable participants of the Marketplace and their role within the system.
* **Warehouses**, which represent the physical locations where inventory is managed, either owned by the Marketplace or by a Seller.
* **Products**, which represent the physical or digital goods offered through the catalog.
* **Inventory**, which represents the distributed stock available for commercialization.
* **Shopping Carts and Orders**, which represent the commercial commitment lifecycle between a Buyer and the Marketplace.
* **Invoices, Shipments, Returns and Refunds**, which represent the billing, logistics, and post-sale processes derived from an Order.

Every significant business action performed over a Product, Order, Inventory record, or Shipment must be reflected in the status and lifecycle of the corresponding entity.

---

# Domain Class Hierarchy

```text
Person (Abstract)
├── Buyer
├── Seller
├── LogisticsOperator
├── Administrator
└── Supervisor

Warehouse (Abstract)
├── MarketplaceWarehouse
└── SellerWarehouse

Product (Abstract)
├── PhysicalProduct
└── DigitalProduct

Inventory
InventoryMovement

ShoppingCart
Order

Invoice
Shipment
Return
Refund
```

---

# Domain Relationships

```text
Person
   │
   ├── Buyer
   ├── Seller
   ├── LogisticsOperator
   ├── Administrator
   └── Supervisor

Administrator
   │
   ├── registers ──────────> Seller
   └── registers ──────────> MarketplaceWarehouse

Seller
   │
   ├── owns ───────────────> SellerWarehouse
   └── publishes ──────────> Product

Warehouse
   │
   └── stores ─────────────> Inventory

Product
   │
   └── stocked in ─────────> Inventory
                                │
                                └── tracked by ────────> InventoryMovement

Buyer
   │
   ├── owns ───────────────> ShoppingCart
   └── places ─────────────> Order

ShoppingCart
   └── converted into ─────> Order

Order
   ├── generates ──────────> Invoice
   ├── fulfilled through ──> Shipment
   └── may generate ───────> Return
                                │
                                └── may generate ──────> Refund

Shipment
   └── assigned to ────────> LogisticsOperator
```

---

# Entities

---

# Person (Abstract)

## Description

Represents any identifiable participant authorized to interact with the NexusMarket system.

This abstract class centralizes the common identity and contact information shared by all participants: Buyer, Seller, Logistics Operator, Administrator, and Supervisor.

The role assigned to a person represents what that person means within the system and determines the responsibilities or business capabilities associated with that person. Per business rule RG-02, each person holds exactly one role within the system.

This class cannot be instantiated directly.

## Attributes

| Attribute | Type       | Description                                                                 |
| --------- | ---------- | ---------------------------------------------------------------------------- |
| identifier | String    | Unique identifier of the person across the platform.                         |
| fullName  | String     | Official full name of the person.                                            |
| email     | String     | Primary email address, unique across the platform, used for access and communication. |
| role      | SystemRole | Business role that defines the person's responsibilities within the Marketplace. |
| status    | UserStatus | Current operational status of the person (e.g. Active, Blocked).             |

## Relationships

* A `Person` may be specialized as a `Buyer`, `Seller`, `LogisticsOperator`, `Administrator`, or `Supervisor`.
* The `role` belongs to `Person` because it represents the person's meaning and responsibilities within the Marketplace.

## Business Rules

* Each person has exactly one role within the system (RG-02).
* `identifier` and `email` must be unique across the platform.
* No participant may manage information outside the scope of its role (RG-03).

---

# Buyer

## Description

Represents a user who acquires products published in the catalog.

A buyer never administers information belonging to other buyers, nor manages inventory or seller data.

## Inherits From

`Person`

## Attributes

| Attribute            | Type              | Description                                                     |
| --------------------- | ----------------- | ---------------------------------------------------------------- |
| primaryAddress        | Address           | Main delivery address for the buyer.                              |
| additionalAddresses   | List\<Address\>   | Secondary delivery addresses. Empty by default.                   |
| commercialStatus      | BuyerCommercialStatus | Condition of the buyer for performing purchases.               |
| cart                  | ShoppingCart?      | Active shopping cart owned by the buyer, when one exists.         |
| orders                | List\<Order\>      | Orders placed by the buyer. Empty by default.                     |

## Relationships

* A `Buyer` owns zero or one active `ShoppingCart`.
* A `Buyer` places zero or more `Order` instances.
* A `Buyer` may request zero or more `Return` instances over their own orders.

---

# Seller

## Description

Represents a business responsible for publishing and managing products in the catalog.

Sellers cannot self-register; they are incorporated into the platform by an `Administrator`.

## Inherits From

`Person`

## Attributes

| Attribute    | Type                  | Description                                                |
| ------------ | --------------------- | ------------------------------------------------------------ |
| warehouses   | List\<SellerWarehouse\> | Warehouses owned and operated by the seller. Empty by default. |
| products     | List\<Product\>        | Products published and managed by the seller. Empty by default. |
| registeredBy | Administrator          | Administrator who incorporated the seller into the platform. |

## Relationships

* A `Seller` owns zero or more `SellerWarehouse` instances.
* A `Seller` publishes zero or more `Product` instances.
* A `Seller` is registered by exactly one `Administrator`.

---

# LogisticsOperator

## Description

Represents the participant responsible for the physical operation of warehouses and the dispatch of orders.

## Inherits From

`Person`

## Attributes

| Attribute | Type              | Description                                              |
| --------- | ----------------- | ---------------------------------------------------------- |
| shipments | List\<Shipment\>  | Shipments handled by the logistics operator. Empty by default. |

## Relationships

* A `LogisticsOperator` is assigned to zero or more `Shipment` instances.

---

# Administrator

## Description

Represents the participant responsible for administering sellers and warehouses within the Marketplace.

## Inherits From

`Person`

## Attributes

*(No additional attributes beyond those inherited from `Person`.)*

## Relationships

* An `Administrator` registers zero or more `Seller` instances.
* An `Administrator` registers zero or more `MarketplaceWarehouse` instances.

---

# Supervisor

## Description

Represents a read-only participant responsible for operational consultation and monitoring. A supervisor does not create or modify business entities.

## Inherits From

`Person`

## Attributes

*(No additional attributes beyond those inherited from `Person`.)*

---

# Warehouse (Abstract)

## Description

Represents a physical location where inventory is stored and managed.

The system distinguishes between warehouses owned by the Marketplace and warehouses owned by a Seller.

This class cannot be instantiated directly.

## Attributes

| Attribute  | Type    | Description                              |
| ---------- | ------- | ------------------------------------------ |
| identifier | String  | Unique identifier of the warehouse.        |
| name       | String  | Display name of the warehouse.             |
| address    | Address | Physical location of the warehouse.        |

## Relationships

* A `Warehouse` stores zero or more `Inventory` records.

---

# MarketplaceWarehouse

## Description

Represents a warehouse owned and operated directly by the Marketplace.

## Inherits From

`Warehouse`

## Attributes

| Attribute     | Type          | Description                                       |
| ------------- | ------------- | --------------------------------------------------- |
| managedBy     | Administrator | Administrator responsible for the warehouse.         |

---

# SellerWarehouse

## Description

Represents a warehouse owned and operated by a Seller.

## Inherits From

`Warehouse`

## Attributes

| Attribute | Type   | Description                     |
| --------- | ------ | ---------------------------------- |
| owner     | Seller | Seller who owns the warehouse.     |

---

# Product (Abstract)

## Description

Represents a good offered through the catalog, published and managed by a `Seller`.

The catalog distinguishes physical products, which require inventory and dispatch, from digital products, which are delivered immediately after payment.

This class cannot be instantiated directly.

## Attributes

| Attribute   | Type                 | Description                                                     |
| ----------- | -------------------- | ------------------------------------------------------------------ |
| identifier  | String               | Unique identifier of the product.                                  |
| name        | String               | Commercial name of the product.                                    |
| description | String               | Description of the product's characteristics.                     |
| variants    | List\<ProductVariant\> | Variations of the product, such as color, size, or model. Empty by default. |
| status      | ProductStatus        | Current lifecycle status of the product within the catalog.       |
| seller      | Seller               | Seller who publishes and manages the product.                     |

## Relationships

* A `Product` is published by one `Seller`.
* A `Product` may be stocked in zero or more `Inventory` records.

---

# PhysicalProduct

## Description

Represents a tangible good that requires inventory management and physical dispatch through a `Shipment`.

## Inherits From

`Product`

---

# DigitalProduct

## Description

Represents an intangible good delivered immediately upon payment confirmation, without requiring inventory or physical dispatch.

## Inherits From

`Product`

## Attributes

| Attribute             | Type   | Description                                              |
| ---------------------- | ------ | ------------------------------------------------------------ |
| digitalDeliveryDetails | String | Information required to deliver the digital good to the buyer. |

---

# ProductVariant

## Description

Represents a specific variation of a product, such as a difference in color, size, or model.

## Attributes

| Attribute | Type   | Description                                     |
| --------- | ------ | -------------------------------------------------- |
| name      | String | Name of the variant characteristic (e.g. Color).   |
| value     | String | Value of the variant characteristic (e.g. Red).    |

---

# Inventory

## Description

Represents the distributed stock of a product available for commercialization at a specific warehouse.

An inventory record must always be linked to exactly one product and one warehouse. Negative stock is never permitted.

## Attributes

| Attribute          | Type      | Description                                             |
| ------------------- | --------- | ----------------------------------------------------------- |
| identifier          | String    | Unique identifier of the inventory record.                  |
| product             | Product   | Product to which this inventory record belongs.             |
| warehouse           | Warehouse | Warehouse where the stock is physically located.             |
| availableQuantity   | Integer   | Quantity currently available for reservation or sale.        |
| reservedQuantity    | Integer   | Quantity currently reserved by active shopping carts or orders. |

## Relationships

* An `Inventory` record belongs to exactly one `Product`.
* An `Inventory` record belongs to exactly one `Warehouse`.
* An `Inventory` record generates zero or more `InventoryMovement` instances.

## Business Rules

* Negative stock is never permitted under any circumstance.
* Stock marked as damaged cannot be reserved.

---

# InventoryMovement

## Description

Represents a significant change in an inventory record, providing traceability of stock over time.

## Attributes

| Attribute      | Type                    | Description                                    |
| -------------- | ----------------------- | ------------------------------------------------- |
| movementId     | Integer                 | Unique identifier of the movement.                 |
| movementType   | InventoryMovementType   | Category of the inventory movement.                |
| quantity       | Integer                 | Quantity affected by the movement.                 |
| movementDate   | LocalDateTime           | Date and time when the movement occurred.          |
| inventory      | Inventory               | Inventory record affected by the movement.         |
| performedBy    | Person                  | Person who triggered the movement.                 |

## Relationships

* Each `InventoryMovement` affects exactly one `Inventory` record.
* Each `InventoryMovement` is performed by one `Person`.

---

# ShoppingCart

## Description

Represents the provisional selection of products made by a buyer before confirming an order.

## Attributes

| Attribute   | Type              | Description                                    |
| ----------- | ----------------- | ------------------------------------------------- |
| cartId      | String            | Unique identifier of the shopping cart.            |
| buyer       | Buyer             | Buyer who owns the shopping cart.                  |
| items       | List\<CartItem\>  | Products currently selected in the cart. Empty by default. |
| createdDate | LocalDateTime     | Date and time when the cart was created.           |

## Relationships

* A `ShoppingCart` belongs to exactly one `Buyer`.
* A `ShoppingCart` is converted into one `Order` upon checkout confirmation.

---

# CartItem

## Description

Represents a product selection within a shopping cart, together with its quantity and reference price.

## Attributes

| Attribute  | Type       | Description                             |
| ---------- | ---------- | ------------------------------------------ |
| product    | Product    | Product selected by the buyer.              |
| quantity   | Integer    | Quantity of the product selected.           |
| unitPrice  | BigDecimal | Reference unit price at selection time.     |

---

# Order

## Description

Represents the formal commercial commitment between a buyer and the Marketplace. Its lifecycle is the central process of the system.

Every significant action in the order lifecycle changes the `orderStatus` and may generate an `Invoice`, a `Shipment`, or a `Return`.

## Attributes

| Attribute    | Type              | Description                                          |
| ------------- | ----------------- | ------------------------------------------------------- |
| orderId       | String            | Unique identifier of the order.                          |
| buyer         | Buyer             | Buyer who placed the order.                              |
| items         | List\<OrderItem\> | Products, quantities, and prices confirmed for the order. |
| orderStatus   | OrderStatus       | Current state of the order lifecycle.                    |
| creationDate  | LocalDateTime     | Date and time when the order was created.                |
| totalAmount   | BigDecimal        | Total amount confirmed for the order.                    |
| invoice       | Invoice?          | Invoice generated for the order, when applicable.        |
| shipment      | Shipment?         | Shipment associated with the order, when applicable.     |

## Relationships

* An `Order` is placed by one `Buyer`.
* An `Order` contains one or more `OrderItem` instances.
* An `Order` may generate one `Invoice`.
* An `Order` may be fulfilled through one `Shipment`.
* An `Order` may generate zero or more `Return` instances.

## Business Rules

* A finalized order cannot be modified under any circumstance.
* Every order must be linked to an authenticated buyer.

---

# OrderItem

## Description

Represents a confirmed product line within an order, including the quantity and price agreed at checkout.

## Attributes

| Attribute  | Type       | Description                              |
| ---------- | ---------- | -------------------------------------------- |
| product    | Product    | Product confirmed in the order.                |
| quantity   | Integer    | Quantity of the product confirmed.             |
| unitPrice  | BigDecimal | Unit price confirmed at checkout.              |
| subtotal   | BigDecimal | Result of quantity multiplied by unit price.   |

---

# Invoice

## Description

Represents the commercial billing information associated with a confirmed order.

## Attributes

| Attribute   | Type          | Description                             |
| ------------ | ------------- | ------------------------------------------ |
| invoiceId    | String        | Unique identifier of the invoice.            |
| order        | Order         | Order to which the invoice belongs.          |
| issueDate    | LocalDateTime | Date and time when the invoice was issued.   |
| totalAmount  | BigDecimal    | Total invoiced amount.                       |
| taxAmount    | BigDecimal    | Tax amount applied to the invoice.            |

## Relationships

* An `Invoice` belongs to exactly one `Order`.

---

# Shipment

## Description

Represents the logistics process of packing, dispatching, and delivering a physical order.

## Attributes

| Attribute        | Type                | Description                                       |
| ------------------ | ------------------- | ----------------------------------------------------- |
| shipmentId          | String              | Unique identifier of the shipment.                     |
| order               | Order               | Order fulfilled by this shipment.                      |
| logisticsOperator   | LogisticsOperator   | Operator responsible for the shipment.                  |
| originWarehouse     | Warehouse           | Warehouse from which the order is dispatched.           |
| shippingAddress     | Address             | Destination address for the delivery.                   |
| shipmentStatus      | ShipmentStatus      | Current status of the shipment.                         |
| dispatchDate        | LocalDateTime?      | Date and time when the shipment left the warehouse.      |
| deliveryDate        | LocalDateTime?      | Date and time when the shipment was delivered.           |

## Relationships

* A `Shipment` fulfills exactly one `Order`.
* A `Shipment` is assigned to one `LogisticsOperator`.
* A `Shipment` originates from one `Warehouse`.

---

# Return

## Description

Represents a buyer's request to return products from a delivered order.

## Attributes

| Attribute    | Type          | Description                                  |
| ------------- | ------------- | ------------------------------------------------ |
| returnId      | String        | Unique identifier of the return request.           |
| order         | Order         | Order associated with the return.                   |
| buyer         | Buyer         | Buyer who requested the return.                     |
| reason        | String        | Reason provided by the buyer for the return.         |
| returnStatus  | ReturnStatus  | Current status of the return process.                |
| requestDate   | LocalDateTime | Date and time when the return was requested.         |

## Relationships

* A `Return` belongs to exactly one `Order`.
* A `Return` is requested by one `Buyer`.
* A `Return` may generate one `Refund`.

---

# Refund

## Description

Represents the monetary reimbursement resulting from an approved return.

## Attributes

| Attribute    | Type          | Description                                     |
| ------------- | ------------- | --------------------------------------------------- |
| refundId      | String        | Unique identifier of the refund.                       |
| relatedReturn | Return        | Return that originated the refund.                     |
| amount        | BigDecimal    | Amount reimbursed to the buyer.                         |
| refundDate    | LocalDateTime | Date and time when the refund was processed.            |
| approvedBy    | Person        | Administrator or Supervisor who approved the refund.    |
| refundStatus  | RefundStatus  | Current status of the refund process.                   |

## Relationships

* A `Refund` originates from exactly one `Return`.
* A `Refund` is approved by one `Person`.

---

# Domain Design Rules

## Person and Specializations

* `Buyer`, `Seller`, `LogisticsOperator`, `Administrator`, and `Supervisor` inherit from `Person`.
* `role` and `status` are defined in `Person` and inherited by all specializations.
* Each person holds exactly one role within the system (RG-02).
* No participant may manage information or entities outside the scope of its role (RG-03).

## Warehouses

* `Warehouse` is abstract; `MarketplaceWarehouse` and `SellerWarehouse` represent genuine specializations, not a generic "type" field.
* A `SellerWarehouse` must always be linked to exactly one `Seller`.

## Products

* `Product` is abstract; `PhysicalProduct` and `DigitalProduct` represent genuine specializations rather than a generic type attribute.
* Only `PhysicalProduct` instances require inventory management and dispatch through a `Shipment`.
* `DigitalProduct` instances are delivered immediately after payment confirmation.

## Inventory

* Every `Inventory` record must be linked to exactly one `Product` and one `Warehouse`.
* Negative stock is never permitted under any circumstance.
* Every change to an inventory record must generate an `InventoryMovement`.

## Orders

* `ShoppingCart` represents a provisional, pre-commitment selection of products; `Order` represents the formal commercial commitment.
* `orderStatus` represents the current state of the order; each transition between statuses represents a significant business event.
* A finalized order cannot be modified under any circumstance.

## Post-Sale Processes

* `Invoice` and `Shipment` are generated as a direct consequence of an `Order`.
* `Return` can only be requested against a delivered `Order`.
* `Refund` can only be generated from an approved `Return`.

## General Constraints

* Every operation must be executed by an authenticated `Person`.
* `identifier` and `email` must be unique across the platform for every `Person`.
