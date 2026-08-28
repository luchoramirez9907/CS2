package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.OrderDtoMapper;
import Application.adapters.in.rest.requests.CreateOrderRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.OrderResponse;
import Application.domain.models.Order;
import Application.domain.ports.in.CreateOrderUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for order creation (cart checkout).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.checkout(request.buyerId(), request.cartId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order confirmed", OrderDtoMapper.toResponse(order)));
    }
}
