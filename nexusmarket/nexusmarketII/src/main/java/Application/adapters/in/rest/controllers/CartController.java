package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.CartDtoMapper;
import Application.adapters.in.rest.requests.AddCartItemRequest;
import Application.adapters.in.rest.requests.UpdateCartItemRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.CartResponse;
import Application.domain.models.ShoppingCart;
import Application.domain.ports.in.AddItemToCartUseCase;
import Application.domain.ports.in.ConsultCartUseCase;
import Application.domain.ports.in.ManageCartItemsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Shopping Cart Management (Manage Cart Items /
 * Consult Cart).
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final AddItemToCartUseCase addItemToCartUseCase;
    private final ManageCartItemsUseCase manageCartItemsUseCase;
    private final ConsultCartUseCase consultCartUseCase;

    public CartController(AddItemToCartUseCase addItemToCartUseCase,
                          ManageCartItemsUseCase manageCartItemsUseCase,
                          ConsultCartUseCase consultCartUseCase) {
        this.addItemToCartUseCase = addItemToCartUseCase;
        this.manageCartItemsUseCase = manageCartItemsUseCase;
        this.consultCartUseCase = consultCartUseCase;
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@RequestBody AddCartItemRequest request) {
        ShoppingCart cart = addItemToCartUseCase.addItemToCart(
                request.buyerId(), request.cartId(), request.productId(),
                request.quantity(), request.unitPrice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added to cart", CartDtoMapper.toResponse(cart)));
    }

    @PutMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @RequestBody UpdateCartItemRequest request) {
        ShoppingCart cart = manageCartItemsUseCase.updateItemQuantity(
                request.buyerId(), request.cartId(), request.productId(), request.quantity());
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", CartDtoMapper.toResponse(cart)));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable String productId,
            @RequestParam String buyerId,
            @RequestParam(required = false) String cartId) {
        ShoppingCart cart = manageCartItemsUseCase.removeItem(buyerId, cartId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed", CartDtoMapper.toResponse(cart)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @RequestParam String buyerId,
            @RequestParam(required = false) String cartId) {
        ShoppingCart cart = manageCartItemsUseCase.clearCart(buyerId, cartId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", CartDtoMapper.toResponse(cart)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> consultCart(
            @RequestParam String buyerId,
            @RequestParam(required = false) String cartId) {
        ShoppingCart cart = consultCartUseCase.consultCart(buyerId, cartId);
        return ResponseEntity.ok(ApiResponse.ok("Cart consulted", CartDtoMapper.toResponse(cart)));
    }
}
