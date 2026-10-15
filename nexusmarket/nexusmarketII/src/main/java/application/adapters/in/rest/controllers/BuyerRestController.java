package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.AfterSalesDtoMapper;
import application.adapters.in.rest.mappers.CartDtoMapper;
import application.adapters.in.rest.mappers.LogisticsDtoMapper;
import application.adapters.in.rest.mappers.OrderDtoMapper;
import application.adapters.in.rest.mappers.PersonDtoMapper;
import application.adapters.in.rest.mappers.ProductDtoMapper;
import application.adapters.in.rest.requests.AddCartItemRequest;
import application.adapters.in.rest.requests.RequestReturnRequest;
import application.adapters.in.rest.requests.UpdateBuyerRequest;
import application.adapters.in.rest.requests.UpdateCartItemRequest;
import application.adapters.in.rest.responses.ApiResponse;
import application.adapters.in.rest.responses.BuyerResponse;
import application.adapters.in.rest.responses.CartResponse;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.TrackingEventResponse;
import application.domain.ports.in.BuyerPort;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST adapter for the BUYER role (BuyerPort). The route prefix is
 * restricted to the BUYER role by the security configuration; every
 * operation acts on the authenticated buyer.
 */
@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerRestController {

    private final BuyerPort buyerPort;

    public BuyerRestController(BuyerPort buyerPort) {
        this.buyerPort = buyerPort;
    }

    // ---------------------------------------------------------------- Profile

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<BuyerResponse>> consultProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Profile found", PersonDtoMapper.toResponse(buyerPort.consultMyProfile(principal.user())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<BuyerResponse>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateBuyerRequest request) {
        return ok("Profile updated", PersonDtoMapper.toResponse(buyerPort.updateMyProfile(principal.user(),
                request.fullName(), request.email(), PersonDtoMapper.toAddress(request.primaryAddress()),
                PersonDtoMapper.toAddresses(request.additionalAddresses()))));
    }

    // ---------------------------------------------------------------- Catalog

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consultProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId) {
        return ok("Product found", ProductDtoMapper.toResponse(buyerPort.consultProduct(principal.user(), productId)));
    }

    // ---------------------------------------------------------------- Shopping cart

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<CartResponse>> consultCart(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return buyerPort.consultMyCart(principal.user())
                .map(cart -> ok("Cart found", CartDtoMapper.toResponse(cart)))
                .orElseGet(() -> ok("The buyer has no active cart", null));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody AddCartItemRequest request) {
        return created("Item added to cart", CartDtoMapper.toResponse(
                buyerPort.addItemToCart(principal.user(), request.productId(), request.quantity())));
    }

    @PutMapping("/cart/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ok("Cart item updated", CartDtoMapper.toResponse(
                buyerPort.updateCartItemQuantity(principal.user(), productId, request.quantity())));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId) {
        return ok("Cart item removed", CartDtoMapper.toResponse(buyerPort.removeCartItem(principal.user(), productId)));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Cart cleared", CartDtoMapper.toResponse(buyerPort.clearCart(principal.user())));
    }

    // ---------------------------------------------------------------- Orders, billing and logistics

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return created("Order confirmed", OrderDtoMapper.toResponse(buyerPort.checkout(principal.user())));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> consultMyOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Orders found", buyerPort.consultMyOrders(principal.user()).stream()
                .map(OrderDtoMapper::toResponse).toList());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consultOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order found", OrderDtoMapper.toResponse(buyerPort.consultOrder(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> consultInvoice(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Invoice found", LogisticsDtoMapper.toResponse(buyerPort.consultInvoice(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> consultShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment found",
                LogisticsDtoMapper.toResponse(buyerPort.consultShipment(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingEventResponse>>> consultTracking(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Tracking found", buyerPort.consultTracking(principal.user(), orderId).stream()
                .map(LogisticsDtoMapper::toResponse).toList());
    }

    // ---------------------------------------------------------------- Returns and refunds

    @PostMapping("/orders/{orderId}/returns")
    public ResponseEntity<ApiResponse<ReturnResponse>> requestReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId,
            @Valid @RequestBody RequestReturnRequest request) {
        return created("Return requested", AfterSalesDtoMapper.toResponse(
                buyerPort.requestReturn(principal.user(), orderId, request.reason())));
    }

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> consultMyReturns(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Returns found", buyerPort.consultMyReturns(principal.user()).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/returns/{returnId}")
    public ResponseEntity<ApiResponse<ReturnResponse>> consultReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return found", AfterSalesDtoMapper.toResponse(buyerPort.consultReturn(principal.user(), returnId)));
    }

    @GetMapping("/returns/{returnId}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> consultRefund(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Refund found", AfterSalesDtoMapper.toResponse(buyerPort.consultRefund(principal.user(), returnId)));
    }

    @GetMapping("/refunds")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> consultMyRefunds(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Refunds found", buyerPort.consultMyRefunds(principal.user()).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    // ----------------------------------------------------------------

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.ok(message, data));
    }

    private static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, data));
    }
}
