package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.CartDtoMapper;
import Application.adapters.in.rest.requests.AddCartItemRequest;
import Application.adapters.in.rest.requests.UpdateCartItemRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.CartResponse;
import Application.domain.models.ShoppingCart;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for shopping cart management. Except for the original add
 * endpoint (buyerId in the body), the buyer is identified by the
 * X-User-Id header.
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final ManageCartItemsUseCase manageCartItemsUseCase;
    private final ConsultCartUseCase consultCartUseCase;

    public CartController(ManageCartItemsUseCase manageCartItemsUseCase,
                          ConsultCartUseCase consultCartUseCase) {
        this.manageCartItemsUseCase = manageCartItemsUseCase;
        this.consultCartUseCase = consultCartUseCase;
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@RequestBody AddCartItemRequest request) {
        ShoppingCart cart = manageCartItemsUseCase.addItemToCart(
                request.buyerId(), request.cartId(), request.productId(),
                request.quantity(), request.unitPrice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added to cart", CartDtoMapper.toResponse(cart)));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @RequestHeader("X-User-Id") String buyerId,
            @PathVariable String productId,
            @RequestBody UpdateCartItemRequest request) {
        ShoppingCart cart = manageCartItemsUseCase.updateItemQuantity(buyerId, productId, request.quantity());
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", CartDtoMapper.toResponse(cart)));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader("X-User-Id") String buyerId,
            @PathVariable String productId) {
        ShoppingCart cart = manageCartItemsUseCase.removeItem(buyerId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Cart item removed", CartDtoMapper.toResponse(cart)));
    }

    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> clear(@RequestHeader("X-User-Id") String buyerId) {
        ShoppingCart cart = manageCartItemsUseCase.clearCart(buyerId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", CartDtoMapper.toResponse(cart)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> consult(@RequestHeader("X-User-Id") String buyerId) {
        return consultCartUseCase.consultCart(buyerId)
                .map(cart -> ResponseEntity.ok(ApiResponse.ok("Active cart", CartDtoMapper.toResponse(cart))))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("Buyer has no active cart", null)));
    }
}
