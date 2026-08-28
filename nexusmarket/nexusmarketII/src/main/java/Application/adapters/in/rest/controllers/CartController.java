package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.CartDtoMapper;
import Application.adapters.in.rest.requests.AddCartItemRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.CartResponse;
import Application.domain.models.ShoppingCart;
import Application.domain.ports.in.AddItemToCartUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for shopping cart management.
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final AddItemToCartUseCase addItemToCartUseCase;

    public CartController(AddItemToCartUseCase addItemToCartUseCase) {
        this.addItemToCartUseCase = addItemToCartUseCase;
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@RequestBody AddCartItemRequest request) {
        ShoppingCart cart = addItemToCartUseCase.addItemToCart(
                request.buyerId(), request.cartId(), request.productId(),
                request.quantity(), request.unitPrice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added to cart", CartDtoMapper.toResponse(cart)));
    }
}
