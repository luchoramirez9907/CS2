package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.FilterParamMapper;
import application.adapters.in.rest.mappers.LogisticsDtoMapper;
import application.adapters.in.rest.mappers.OrderDtoMapper;
import application.adapters.in.rest.mappers.PersonDtoMapper;
import application.adapters.in.rest.mappers.ProductDtoMapper;
import application.adapters.in.rest.mappers.WarehouseDtoMapper;
import application.adapters.in.rest.requests.CreateShipmentRequest;
import application.adapters.in.rest.requests.DamagedStockRequest;
import application.adapters.in.rest.requests.InventoryMovementRequest;
import application.adapters.in.rest.requests.ShipmentReturnRequest;
import application.adapters.in.rest.requests.UpdatePersonRequest;
import application.adapters.in.rest.responses.ApiResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.TrackingEventResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.valueobjects.InventoryMovementType;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST adapter for the LOGISTICS_OPERATOR role (LogisticsOperatorPort). The
 * route prefix is restricted to that role by the security configuration.
 */
@RestController
@RequestMapping("/api/v1/operator")
public class LogisticsOperatorRestController {

    private final LogisticsOperatorPort operatorPort;

    public LogisticsOperatorRestController(LogisticsOperatorPort operatorPort) {
        this.operatorPort = operatorPort;
    }

    // ---------------------------------------------------------------- Profile

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> consultProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Profile found", PersonDtoMapper.toResponse(operatorPort.consultMyProfile(principal.user())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdatePersonRequest request) {
        return ok("Profile updated", PersonDtoMapper.toResponse(
                operatorPort.updateMyProfile(principal.user(), request.fullName(), request.email())));
    }

    // ---------------------------------------------------------------- Warehouses, catalog and inventory

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consultWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String warehouseId) {
        return ok("Warehouse found",
                WarehouseDtoMapper.toResponse(operatorPort.consultWarehouse(principal.user(), warehouseId)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consultProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId) {
        return ok("Product found",
                ProductDtoMapper.toResponse(operatorPort.consultProduct(principal.user(), productId)));
    }

    @PostMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerMovement(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody InventoryMovementRequest request) {
        return created("Inventory movement registered", WarehouseDtoMapper.toResponse(
                operatorPort.registerInventoryMovement(principal.user(), request.productId(), request.warehouseId(),
                        InventoryMovementType.fromCode(request.movementType()), request.quantity())));
    }

    @PostMapping("/inventory/damaged")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerDamagedStock(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody DamagedStockRequest request) {
        return created("Damaged stock registered", WarehouseDtoMapper.toResponse(operatorPort.registerDamagedStock(
                principal.user(), request.productId(), request.warehouseId(), request.quantity())));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<InventoryResponse>> consultInventory(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Inventory found", WarehouseDtoMapper.toResponse(
                operatorPort.consultInventory(principal.user(), productId, warehouseId)));
    }

    // ---------------------------------------------------------------- Logistics

    @PostMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId,
            @Valid @RequestBody CreateShipmentRequest request) {
        // The shipment is always assigned to the authenticated operator.
        return created("Shipment created", LogisticsDtoMapper.toResponse(
                operatorPort.createShipment(principal.user(), orderId, request.originWarehouseId())));
    }

    @PostMapping("/orders/{orderId}/shipment/dispatch")
    public ResponseEntity<ApiResponse<ShipmentResponse>> dispatchShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment dispatched",
                LogisticsDtoMapper.toResponse(operatorPort.dispatchShipment(principal.user(), orderId)));
    }

    @PostMapping("/orders/{orderId}/shipment/delivery")
    public ResponseEntity<ApiResponse<ShipmentResponse>> confirmDelivery(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Delivery confirmed",
                LogisticsDtoMapper.toResponse(operatorPort.confirmDelivery(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> consultShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment found",
                LogisticsDtoMapper.toResponse(operatorPort.consultShipment(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingEventResponse>>> consultTracking(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Tracking found", operatorPort.consultTracking(principal.user(), orderId).stream()
                .map(LogisticsDtoMapper::toResponse).toList());
    }

    // ---------------------------------------------------------------- Orders

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consultOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order found", OrderDtoMapper.toResponse(operatorPort.consultOrder(principal.user(), orderId)));
    }

    @PostMapping("/orders/{orderId}/finalization")
    public ResponseEntity<ApiResponse<OrderResponse>> finalizeOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order finalized", OrderDtoMapper.toResponse(operatorPort.finalizeOrder(principal.user(), orderId)));
    }

    // ---------------------------------------------------------------- Listings, history and failed deliveries

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> listWarehouses(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Warehouses found", operatorPort.listWarehouses(principal.user()).stream()
                .map(WarehouseDtoMapper::toResponse).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> consultAssignedOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Orders found", operatorPort.consultAssignedOrders(principal.user(),
                FilterParamMapper.orderStatus(status)).stream().map(OrderDtoMapper::toResponse).toList());
    }

    @PostMapping("/orders/{orderId}/shipment/return")
    public ResponseEntity<ApiResponse<ShipmentResponse>> registerShipmentReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId,
            @Valid @RequestBody ShipmentReturnRequest request) {
        return ok("Shipment returned to the warehouse", LogisticsDtoMapper.toResponse(
                operatorPort.registerShipmentReturn(principal.user(), orderId, request.reason())));
    }

    @GetMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> consultInventoryMovements(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Movement history found", operatorPort.consultInventoryMovements(principal.user(), productId, warehouseId)
                .stream().map(WarehouseDtoMapper::toResponse).toList());
    }

    // ----------------------------------------------------------------

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.ok(message, data));
    }

    private static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, data));
    }
}
