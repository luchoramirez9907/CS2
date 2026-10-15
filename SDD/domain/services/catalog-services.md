# Catalog Management Services

## 1. Purpose

This document specifies the publication, consultation, update, and status changes of **products** (`PhysicalProduct`, `DigitalProduct`). General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Publish Product | `publishProduct` | `PublishProductService` |
| Manage Product | `consultProduct` / `updateProduct` / `searchCatalog` / `consultSellerProducts` | `ManageProductService` |
| Change Product Status | `changeProductStatus` | `ChangeProductStatusService` |

Product lifecycle (`ProductStatus`):

```text
PUBLISHED ⇄ SUSPENDED
    │           │
    └─────┬─────┘
          ▼
    DISCONTINUED   (permanent: no further status change, no update)
```

Only `PUBLISHED` products are visible in the public catalog and can be added to a cart.

---

## 2. Publish Product

### 2.1 Description

Creates a new product with its catalog price and variants, links it to the publishing seller, and makes it available in the public catalog.

### 2.2 Actor

`SELLER`.

### 2.3 Domain Service Method

```java
Product publishProduct(String sellerId, Product product);
```

Role port: `SellerPort.publishProduct` (seller = the authenticated seller). REST: `POST /api/v1/seller/products`. The REST mapper builds a `PhysicalProduct`, or a `DigitalProduct` when `digital = true`.

### 2.4 Input (Product)

| Field | Rule |
| ----- | ---- |
| `identifier` | Required, not blank, unique among products. |
| `name` | Required, not blank. |
| `description` | Optional. |
| `price` | Required, not negative (taxes included). |
| `status` | Must be `PUBLISHED`. |
| `seller` | Must be the publishing seller. |
| `variants` | Optional list of (name, value), both not blank. |
| `digitalDeliveryDetails` | Required for `DigitalProduct`. |

### 2.5 Validations (in order)

1. `sellerId` not blank; `product` not null → `IllegalArgumentException`.
2. The seller exists → `EntityNotFoundException`; is active → `IllegalStateException`.
3. The product belongs to that seller → `IllegalArgumentException`.
4. The product enters the catalog as `PUBLISHED` → `IllegalArgumentException`.
5. No product with the same identifier → `IllegalArgumentException`.

Entity invariants (identifier, name, price, status, seller, variants, digital delivery details) are enforced by the `Product` constructors.

### 2.6 Domain Behavior

`seller.addProduct(product)`.

### 2.7 Persistence and Notification

`ProductRepository.save(product)`; notifies the seller "Product published".

### 2.8 Output

The published `Product`.

### 2.9 Output Ports

`SellerRepository`, `ProductRepository`, `NotificationService`.

### 2.10 Processing Flow

```text
validate input → load seller (exists, active) → product belongs to seller → status PUBLISHED
→ unique identifier → seller.addProduct → save → notify
```

---

## 3. Consult Product

### 3.1 Description

Retrieves a product. Published products are public; suspended or discontinued products are only visible to their seller, administrators, and supervisors.

### 3.2 Actor

Anyone (published products); `SELLER` owner, `ADMINISTRATOR`, `SUPERVISOR` (hidden products).

### 3.3 Domain Service Method

```java
Product consultProduct(String requesterId, String productId);
```

`requesterId` is `null` for anonymous access. Role ports: `PublicAccessPort.consultCatalogProduct` (anonymous) and `consultProduct` in every authenticated role port.

### 3.4 Validations and Authorization

1. The product exists → `EntityNotFoundException`.
2. If it is `PUBLISHED`, it is returned.
3. Otherwise: anonymous requesters, requesters without `CONSULT_PRODUCT`, and requesters that cannot access the product (not its seller nor an administrative role) receive "not available in the catalog" → `IllegalArgumentException`. The response does not reveal whether a hidden product exists.

### 3.5 Output

The `Product`. Output port: `ProductRepository`.

---

## 4. Update Product

### 4.1 Description

Updates the name, description, catalog price, and variants of a product.

### 4.2 Actor

`SELLER` (only its own products).

### 4.3 Domain Service Method

```java
Product updateProduct(String requesterId, String productId, String name, String description,
                      BigDecimal price, List<ProductVariant> variants);
```

`price` and `variants` are optional: `null` keeps the current value. Role port: `SellerPort.updateProduct`.

### 4.4 Validations and Authorization

1. Requester has `UPDATE_PRODUCT`; the product exists; `requireProductAccess` (only its seller) → `OwnershipAccessDeniedException`.
2. A discontinued product cannot be modified → `IllegalArgumentException`.
3. `name` not blank; `price` not negative; variant name and value not blank → `IllegalArgumentException`.

### 4.5 Domain Behavior

`updateInformation(name, description)`, `changePrice(price)` when given, `replaceVariants(variants)` when given. A price change does not affect items already in a cart or order: they keep the price captured at selection time.

### 4.6 Persistence

`ProductRepository.save(product)`.

### 4.7 Output

The updated `Product`.

---

## 5. Change Product Status

### 5.1 Description

Suspends, re-publishes, or permanently discontinues a product.

### 5.2 Actor

`SELLER` (only its own products); `ADMINISTRATOR` (any product).

### 5.3 Domain Service Method

```java
Product changeProductStatus(String requesterId, String productId, ProductStatus status);
```

Role ports: `SellerPort.changeProductStatus`, `AdministratorPort.changeProductStatus`.

### 5.4 Validations and Authorization

1. `status` not null → `IllegalArgumentException`.
2. Requester has `CHANGE_PRODUCT_STATUS`; the product exists; `requireProductAccess`.
3. The new status differs from the current one → `IllegalArgumentException`.
4. A `DISCONTINUED` product cannot change its status → `IllegalArgumentException`.

### 5.5 Persistence and Notification

`Product.changeStatus(status)`, `ProductRepository.save(product)`; notifies the seller "Product status changed".

### 5.6 Output

The updated `Product`.

---

## 6. Search Catalog

### 6.1 Description

Searches the public catalog. Only `PUBLISHED` products are returned, sorted by name.

### 6.2 Actor

Anyone (no authentication).

### 6.3 Domain Service Method

```java
List<Product> searchCatalog(String text, String sellerId);
```

Role port: `PublicAccessPort.searchCatalog`. REST: `GET /api/v1/public/products?q=&sellerId=`.

### 6.4 Rules

- `text` (optional): case-insensitive match against the name or the description.
- `sellerId` (optional): only products of that seller.
- Suspended and discontinued products never appear.

### 6.5 Output

The list of products (possibly empty). Output port: `ProductRepository` (`findAll`, `findBySellerId`).

---

## 7. Consult Seller Products

### 7.1 Description

Lists every product of a seller, in any status (published, suspended, discontinued), sorted by name.

### 7.2 Actor

`ADMINISTRATOR`, `SUPERVISOR` (any seller); `SELLER` (only its own products).

### 7.3 Domain Service Method

```java
List<Product> consultSellerProducts(String requesterId, String sellerId);
```

Role ports: `SellerPort.consultMyProducts`, `AdministratorPort.consultSellerProducts`, `SupervisorPort.consultSellerProducts`. REST: `GET /api/v1/seller/products`, `GET .../sellers/{sellerId}/products`.

### 7.4 Validations and Authorization

1. `sellerId` not blank → `IllegalArgumentException`.
2. Requester has `CONSULT_SELLER`.
3. A seller can only list its own products → `OwnershipAccessDeniedException`.

An unknown seller returns an empty list.

### 7.5 Output

The list of products. Output port: `ProductRepository.findBySellerId`.
