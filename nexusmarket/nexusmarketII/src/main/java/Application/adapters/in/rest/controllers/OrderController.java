package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.LogisticsDtoMapper;
import Application.adapters.in.rest.mappers.OrderDtoMapper;
import Application.adapters.in.rest.requests.CreateOrderRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.InvoiceResponse;
import Application.adapters.in.rest.responses.OrderResponse;
import Application.domain.models.Invoice;
import Application.domain.models.Order;
import Application.domain.ports.in.ConsultInvoiceUseCase;
import Application.domain.ports.in.ConsultOrderUseCase;
import Application.domain.ports.in.CreateOrderUseCase;
import Application.domain.ports.in.GenerateInvoiceUseCase;
import Application.domain.ports.in.UpdateOrderStatusUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for orders (checkout, status lifecycle, consultation) and
 * their invoice. Except for checkout (buyerId in the body), the requesting
 * user is identified by the X-User-Id header.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final ConsultOrderUseCase consultOrderUseCase;
    private final GenerateInvoiceUseCase generateInvoiceUseCase;
    private final ConsultInvoiceUseCase consultInvoiceUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           UpdateOrderStatusUseCase updateOrderStatusUseCase,
                           ConsultOrderUseCase consultOrderUseCase,
                           GenerateInvoiceUseCase generateInvoiceUseCase,
                           ConsultInvoiceUseCase consultInvoiceUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.consultOrderUseCase = consultOrderUseCase;
        this.generateInvoiceUseCase = generateInvoiceUseCase;
        this.consultInvoiceUseCase = consultInvoiceUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.checkout(request.buyerId(), request.cartId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order confirmed", OrderDtoMapper.toResponse(order)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consult(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Order order = consultOrderUseCase.consultOrder(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Order found", OrderDtoMapper.toResponse(order)));
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.confirmPayment(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed", OrderDtoMapper.toResponse(order)));
    }

    @PostMapping("/{orderId}/finalization")
    public ResponseEntity<ApiResponse<OrderResponse>> finalizeOrder(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Order order = updateOrderStatusUseCase.finalizeOrder(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Order finalized", OrderDtoMapper.toResponse(order)));
    }

    @PostMapping("/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Invoice invoice = generateInvoiceUseCase.generateInvoice(requesterId, orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Invoice generated", LogisticsDtoMapper.toResponse(invoice)));
    }

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> consultInvoice(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Invoice invoice = consultInvoiceUseCase.consultInvoice(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Invoice found", LogisticsDtoMapper.toResponse(invoice)));
    }
}
