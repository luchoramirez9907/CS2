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
- **Authorization**

---

# User Management Services

## Register User

Creates a new system participant and establishes the person's initial identity information, assigned role, and operational status.

## Manage User

Consults, updates, and changes the operational status of an existing system participant according to the access permissions of the requesting user.

---

# Seller Management Services

## Register Seller

Creates a new seller and associates it with the Administrator who incorporated it into the platform. A seller cannot self-register.

## Manage Seller

Consults, updates, and changes the operational status of an existing seller according to the applicable business rules.

---

# Buyer Management Services

## Register Buyer

Creates a new buyer and establishes the buyer's initial delivery information and commercial status.

## Manage Buyer

Consults, updates, and changes the commercial status of an existing buyer, and retrieves the buyer's associated orders, returns, and refunds according to the access permissions of the requesting user.

---

# Warehouse Management Services

## Register Warehouse

Creates a new warehouse, either owned directly by the Marketplace or owned by a seller, according to the specified ownership.

## Manage Warehouse

Consults and updates the information of an existing warehouse according to the applicable business rules.

---

# Catalog Management Services

## Publish Product

Creates a new product, registers its variants, associates it with the requesting seller, and makes it available in the public catalog.

## Manage Product

Consults and updates the information of an existing product according to the applicable business rules.

## Change Product Status

Suspends or permanently discontinues a product, removing or hiding it from the public catalog.

---

# Inventory Management Services

## Register Inventory Movement

Records any significant movement affecting an inventory record—initial stock, reservation, release of a reservation, manual adjustment, or return—ensuring that stock is never reserved or reduced below zero, and that stock marked as damaged cannot be reserved.

## Consult Inventory

Retrieves the current available and reserved stock of a product at a specific warehouse.

---

# Shopping Cart Management Services

## Manage Cart Items

Adds, updates the quantity of, removes, or clears products within a buyer's shopping cart.

## Consult Cart

Retrieves the current contents of a buyer's shopping cart.

---

# Order Management Services

## Confirm Order

Converts a buyer's shopping cart into a formal order, reserving the corresponding inventory and establishing the order's initial status.

## Update Order Status

Registers the financial confirmation of an order and, once the associated shipment has been delivered, finalizes the order. A finalized order cannot be modified afterward.

## Consult Order

Retrieves the information of an order according to the access permissions of the requesting user.

---

# Billing Management Services

## Generate Invoice

Creates the billing record associated with a confirmed order, including the applicable total and tax amounts.

## Consult Invoice

Retrieves the invoice information associated with an order according to the access permissions of the requesting user.

---

# Logistics Management Services

## Create Shipment

Creates the shipment record associated with a paid order, establishes the originating warehouse and destination address, and assigns the responsible logistics operator.

## Update Shipment Status

Registers the physical dispatch of a shipment from the originating warehouse and its subsequent delivery confirmation to the buyer.

## Consult Shipment

Retrieves the current status and tracking information of a shipment according to the access permissions of the requesting user.

---

# Returns and Refunds Management Services

## Request Return

Creates a return request for a buyer over a delivered order, recording the reason provided by the buyer.

## Resolve Return

Approves or rejects a return request after applying the required business validations, and retrieves the information of a return request according to the access permissions of the requesting user.

## Process Refund

Creates the monetary reimbursement associated with an approved return and retrieves refund information according to the access permissions of the requesting user.

---

# Administrative Reporting Services

## Consult Commercial Report

Retrieves consolidated information about orders, invoices, revenue, and inventory levels across warehouses for administrative review.

## Consult Seller Performance Report

Retrieves consolidated information about products, orders, and returns associated with a seller.

---

# Authorization Services

## Validate Permissions

Determines whether a user has permission to perform a specific business operation based on the user's role and status, ensuring no participant operates outside the boundaries of its assigned role.

## Validate Ownership Access

Determines whether a user is authorized to access or operate on information belonging to a specific buyer, seller, warehouse, or order that it owns or manages.

---

# Service Organization

The services described in this document provide the **high-level service catalog** of the system.

They intentionally do not describe implementation details or complete business workflows.

Detailed specifications will be maintained in separate Markdown files organized by subdomain. For example:

```text
services/
│   └── user-services.md
│
│   └── seller-services.md
│
│   └── buyer-services.md
│
│   └── warehouse-services.md
│
│   └── catalog-services.md
│
│   └── inventory-services.md
│
│   └── shopping-cart-services.md
│
│   └── order-services.md
│
│   └── billing-services.md
│
│   └── logistics-services.md
│
│   └── returns-refunds-services.md
│
│   └── administrative-reporting-services.md
│
    └── authorization-services.md
```
