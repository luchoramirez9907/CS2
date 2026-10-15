# Shopping Cart Management Services

## 1. Purpose

This document specifies the management and consultation of the **shopping cart** of a buyer. General conventions are defined in `user-services.md`, section 2.

| Catalog service | Operations (Domain Service methods) | Domain Service |
| --------------- | ---------------------------------- | -------------- |
| Manage Cart Items | `addItemToCart`, `updateItemQuantity`, `removeItem`, `clearCart` | `ManageCartItemsService` |
| Consult Cart | `consultCart` | `ConsultCartService` |

**Rules shared by every operation:**

- The cart always belongs to the requesting buyer (the buyer identifier is the authenticated user; there is no permission-matrix lookup because only the buyer role reaches these operations).
- The buyer must exist (`EntityNotFoundException`) and be active (`IllegalStateException`).
- **Stock is reserved at selection time** for physical products: adding units reserves stock, removing units releases it. Digital products do not reserve stock.
- **The unit price always comes from the catalog** (`Product.price`), never from the client.

---

## 2. Add Item to Cart

### 2.1 Description

Adds a product selection to the buyer's active cart, creating the cart if the buyer has none.

### 2.2 Actor

`BUYER`.

### 2.3 Domain Service Method

```java
ShoppingCart addItemToCart(String buyerId, String cartId, String productId, int quantity);
```

`cartId` is optional: `null` resolves the buyer's active cart or creates a new one. Role port: `BuyerPort.addItemToCart`. REST: `POST /api/v1/buyer/cart/items`.

### 2.4 Validations (in order)

1. `buyerId` and `productId` not blank; `quantity` positive → `IllegalArgumentException`.
2. The buyer exists and is active.
3. The buyer's commercial status is `ACTIVE` → `BuyerNotAuthorizedException`.
4. The product exists → `EntityNotFoundException`; it is `PUBLISHED` → `IllegalArgumentException`.
5. When `cartId` is given: the cart exists → `EntityNotFoundException`; it belongs to the buyer → `IllegalArgumentException`.
6. Physical product: enough available stock in some warehouse → `InsufficientInventoryException`.

### 2.5 Domain Behavior

- `cart.addItem(product, quantity, product.getPrice())`. If the product is already in the cart, the quantity is increased and the line takes the current catalog price.
- Physical product: `InventoryReservationService.reserveStock(product, quantity, buyer)` (movement `RESERVATION`).
- `buyer.attachCart(cart)`.

### 2.6 Persistence and Notification

`ShoppingCartRepository.save(cart)` (and the inventory record and movement); notifies "Cart updated".

### 2.7 Output

The updated `ShoppingCart`.

### 2.8 Exceptions

`IllegalArgumentException`, `EntityNotFoundException`, `IllegalStateException`, `BuyerNotAuthorizedException`, `InsufficientInventoryException`.

### 2.9 Output Ports

`BuyerRepository`, `ProductRepository`, `ShoppingCartRepository`, `InventoryRepository`, `InventoryMovementRepository`, `NotificationService`.

### 2.10 Processing Flow

```text
validate input → load active buyer → require commercial ACTIVE → load PUBLISHED product
→ resolve or create cart → add item at catalog price → [physical] reserve stock
→ attach cart → save cart → notify
```

---

## 3. Update Cart Item Quantity

### 3.1 Description

Sets a new quantity for a product already in the cart, reserving or releasing the difference.

### 3.2 Domain Service Method

```java
ShoppingCart updateItemQuantity(String buyerId, String productId, int quantity);
```

Role port: `BuyerPort.updateCartItemQuantity`. REST: `PUT /api/v1/buyer/cart/items/{productId}`.

### 3.3 Validations

1. `productId` not blank; `quantity` positive (use *Remove Item* to delete) → `IllegalArgumentException`.
2. The buyer exists and is active; it has an active cart → `IllegalArgumentException`.
3. The product is in the cart → `IllegalArgumentException`.
4. When increasing, the product is still `PUBLISHED` → `IllegalArgumentException`.

### 3.4 Domain Behavior

`delta = newQuantity − currentQuantity`. Physical product: `delta > 0` reserves `delta` units (may raise `InsufficientInventoryException`); `delta < 0` releases them. Then `cart.updateItemQuantity`.

### 3.5 Persistence and Output

`ShoppingCartRepository.save(cart)`; returns the updated cart.

---

## 4. Remove Item

### 4.1 Description

Removes a product from the cart and releases its reserved stock.

### 4.2 Domain Service Method

```java
ShoppingCart removeItem(String buyerId, String productId);
```

Role port: `BuyerPort.removeCartItem`. REST: `DELETE /api/v1/buyer/cart/items/{productId}`.

### 4.3 Validations

The buyer exists and is active; it has an active cart; the product is in the cart → `IllegalArgumentException`.

### 4.4 Domain Behavior, Persistence and Output

Physical product: releases the reserved units (`RESERVATION_RELEASE`). `cart.removeItem`, `ShoppingCartRepository.save(cart)`; returns the updated cart.

---

## 5. Clear Cart

### 5.1 Description

Removes every product from the cart and releases all its reserved stock.

### 5.2 Domain Service Method

```java
ShoppingCart clearCart(String buyerId);
```

Role port: `BuyerPort.clearCart`. REST: `DELETE /api/v1/buyer/cart`.

### 5.3 Validations

The buyer exists and is active; it has an active cart → `IllegalArgumentException` (e.g. after a checkout, which consumes the cart).

### 5.4 Domain Behavior, Persistence and Output

Releases the reservation of every physical item, `cart.clear()`, `ShoppingCartRepository.save(cart)`; returns the empty cart.

---

## 6. Consult Cart

### 6.1 Description

Retrieves the buyer's active cart, if any.

### 6.2 Domain Service Method

```java
Optional<ShoppingCart> consultCart(String buyerId);
```

Role port: `BuyerPort.consultMyCart`. REST: `GET /api/v1/buyer/cart` (`data: null` when there is no active cart).

### 6.3 Validations and Output

`buyerId` not blank; the buyer exists and is active. Returns the active cart or empty. No state changes.
