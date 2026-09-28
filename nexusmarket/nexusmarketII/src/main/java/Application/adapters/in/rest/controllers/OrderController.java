package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.OrderDtoMapper;
import Application.adapters.in.rest.requests.ConfirmPaymentRequest;
import Application.adapters.in.rest.requests.CreateOrderRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.OrderResponse;
import Application.domain.models.Order;
import Application.domain.ports.in.ConsultOrderUseCase;
import Application.domain.ports.in.CreateOrderUseCase;
import Application.domain.ports.in.UpdateOrderStatusUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Order Management (Confirm Order / Update Order
 * Status / Consult Order).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final ConsultOrderUseCase consultOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase,
                           ConsultOrderUseCase consultOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.consultOrderUseCase = consultOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.checkout(request.buyerId(), request.cartId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order confirmed", OrderDtoMapper.toResponse(order)));
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @PathVariable String orderId,
            @RequestBody ConfirmPaymentRequest request) {
        Order order = updateOrderStatusUseCase.confirmPayment(request.performerId(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed", OrderDtoMapper.toResponse(order)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consult(
            @PathVariable String orderId,
            @RequestParam String requesterId) {
        Order order = consultOrderUseCase.consultOrder(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Order found", OrderDtoMapper.toResponse(order)));
    }
}
