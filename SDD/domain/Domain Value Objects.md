# Domain Value Objects

## Introduction

Value Objects represent immutable concepts within the NexusMarket domain.

Unlike Entities, Value Objects do not have their own identity. They are defined entirely by their values and are used to encapsulate controlled business concepts, improve domain expressiveness, and prevent the use of primitive values or scattered string literals throughout the application.

The Marketplace domain uses Value Objects for business catalogs such as roles, statuses, product lifecycle states, and inventory movement types.

All business catalogs inherit from `DomainCatalog`.

---

# Value Object Hierarchy

```text
DomainCatalog (Abstract)
├── SystemRole
├── UserStatus
├── BuyerCommercialStatus
├── ProductStatus
├── InventoryMovementType
└── OrderStatus
```

Additional non-catalog Value Objects:

```text
Address
```

---

# DomainCatalog (Abstract)

## Description

Represents a generic business catalog used throughout the NexusMarket domain.

`DomainCatalog` provides a consistent structure for controlled business values that require a code, a human-readable name, and a business description.

This class cannot be instantiated directly.

## Attributes

| Attribute   | Type   | Description                                           |
| ----------- | ------ | ------------------------------------------------------- |
| code        | String | Unique business identifier of the catalog value.        |
| name        | String | Human-readable name displayed within the application.   |
| description | String | Business definition of the catalog value.                |

## Characteristics

* Immutable.
* Equality is determined by value rather than object identity.
* Catalog values are controlled by the domain.
* Catalog values must not be represented by arbitrary strings throughout the application.
* Each catalog value must have a unique `code`.

---

# SystemRole

## Description

Represents the responsibilities and permissions assigned to a participant within the NexusMarket system.

The role is a characteristic of `Person` because it represents what the person means within the system and the single set of responsibilities associated with that person (RG-02).

## Inherits From

`DomainCatalog`

## Allowed Values

| Code               | Name               | Description                                                        |
| ------------------ | ------------------ | ---------------------------------------------------------------------- |
| BUYER               | Buyer               | Participant who acquires products published in the catalog.             |
| SELLER              | Seller              | Participant responsible for registering and managing products.          |
| LOGISTICS_OPERATOR  | Logistics Operator  | Participant responsible for the physical operation of warehouses and dispatches. |
| ADMINISTRATOR       | Administrator       | Participant responsible for administering sellers and warehouses.        |
| SUPERVISOR          | Supervisor          | Participant with read-only access for operational monitoring.            |

---

# UserStatus

## Description

Represents the current operational status of a person's participation in the NexusMarket system.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                             |
| -------- | -------- | ---------------------------------------------------------- |
| ACTIVE   | Active   | Person can access and operate within the system normally. |
| INACTIVE | Inactive | Person exists but is not currently active in the system.  |
| BLOCKED  | Blocked  | Person's access to the system has been suspended.          |

---

# BuyerCommercialStatus

## Description

Represents the commercial condition of a buyer for performing purchases within the Marketplace.

`BuyerCommercialStatus` is independent from `UserStatus`; it reflects the buyer's ability to transact rather than their general system access.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                              |
| ---------- | ---------- | ------------------------------------------------------------ |
| ACTIVE     | Active     | Buyer is authorized to place new orders.                        |
| RESTRICTED | Restricted | Buyer has limitations on purchasing activity.                    |
| BLOCKED    | Blocked    | Buyer is not authorized to place new orders.                     |

---

# ProductStatus

## Description

Represents the lifecycle state of a product within the catalog.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code          | Name          | Description                                             |
| ------------- | ------------- | ------------------------------------------------------------ |
| PUBLISHED     | Published     | Product is visible and available in the public catalog.        |
| SUSPENDED     | Suspended     | Product is temporarily hidden from the public catalog.          |
| DISCONTINUED  | Discontinued  | Product is permanently removed from commercialization.          |

---

# InventoryMovementType

## Description

Represents the type of movement recorded against an inventory record.

Movements provide traceability of stock changes independently from the current available and reserved quantities of the inventory record.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code           | Name           | Description                                                  |
| -------------- | -------------- | ------------------------------------------------------------------ |
| STOCK_IN        | Stock In        | Incoming stock registered into a warehouse.                          |
| RESERVATION     | Reservation     | Stock reserved as a result of an active shopping cart or order.       |
| SALE_EXIT       | Sale Exit       | Stock removed from inventory as a result of a completed sale.         |
| ADJUSTMENT      | Adjustment      | Manual correction of the recorded stock quantity.                     |
| RETURN          | Return          | Stock reinstated as a result of a product return.                     |

---

# OrderStatus

## Description

Represents the current state of an order as it progresses through its commercial and logistics lifecycle.

The status describes the current state of the order, while each transition between statuses represents a significant business event in the order's lifecycle.

## Inherits From

`DomainCatalog`

## Allowed Values

| Code             | Name             | Description                                                |
| ----------------- | ----------------- | ---------------------------------------------------------------- |
| CART               | Cart               | Products provisionally selected, order not yet confirmed.          |
| PENDING_PAYMENT    | Pending Payment    | Order confirmed and awaiting financial confirmation.                |
| PAID               | Paid               | Payment confirmed; preparation process may begin.                   |
| SHIPPED            | Shipped            | Order has physically left the warehouse.                            |
| DELIVERED          | Delivered          | Order has been successfully delivered to the buyer.                  |

## Lifecycle

```text
CART
   │
   ▼
PENDING_PAYMENT
   │
   ▼
PAID
   │
   ▼
SHIPPED
   │
   ▼
DELIVERED
```

---

# Address

## Description

Represents a physical location used for buyer deliveries and warehouse placement.

`Address` is a Value Object rather than a `DomainCatalog`, since it does not represent a controlled catalog of business values but a structured, immutable description of a physical location.

## Attributes

| Attribute    | Type   | Description                          |
| ------------ | ------ | --------------------------------------- |
| street       | String | Street name and number.                  |
| city         | String | City of the address.                     |
| state        | String | State, department, or province.          |
| country      | String | Country of the address.                   |
| postalCode   | String | Postal or ZIP code.                        |

---

# Primitive Enumerations

The following concepts are represented as primitive enumerations because they contain fixed technical values and do not require business catalog metadata such as `code`, `name`, or `description`.

---

# ShipmentStatus

## Description

Represents the technical execution state of a shipment.

## Values

```text
PENDING
IN_TRANSIT
DELIVERED
RETURNED
```

---

# ReturnStatus

## Description

Represents the technical processing state of a return request.

## Values

```text
REQUESTED
APPROVED
REJECTED
COMPLETED
```

---

# RefundStatus

## Description

Represents the technical processing state of a refund.

## Values

```text
PENDING
PROCESSED
REJECTED
```

---

# Value Object Design Rules

## Immutability

All Value Objects must be immutable after creation.

Their values cannot be modified after the object has been instantiated.

## Equality

Value Objects are compared according to their values rather than object identity.

Two instances containing the same business values represent the same Value Object.

## Controlled Values

Business catalogs must use controlled values defined by the domain.

The application must avoid replacing these concepts with arbitrary strings such as:

```text
"ACTIVE"
"BLOCKED"
"PUBLISHED"
```

throughout the codebase.

Instead, the corresponding Value Object must be used:

```text
UserStatus
BuyerCommercialStatus
ProductStatus
OrderStatus
```

## Business Versus Technical Enumerations

A business concept should be modeled as a `DomainCatalog` Value Object when it requires:

* a business code;
* a display name;
* a business description;
* controlled domain evolution.

A simple enumeration should be used when the concept represents a fixed technical value without additional business metadata.

## Relationship With Entities

Entities reference Value Objects rather than primitive strings whenever the referenced value represents a controlled business concept.

Examples:

```text
Person.role : SystemRole

Person.status : UserStatus

Buyer.commercialStatus : BuyerCommercialStatus

Product.status : ProductStatus

InventoryMovement.movementType : InventoryMovementType

Order.orderStatus : OrderStatus

Buyer.primaryAddress : Address
```

This approach improves type safety, domain expressiveness, maintainability, and consistency with Domain-Driven Design principles.
