package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.AfterSalesDtoMapper;
import application.adapters.in.rest.mappers.FilterParamMapper;
import application.adapters.in.rest.mappers.LogisticsDtoMapper;
import application.adapters.in.rest.mappers.OrderDtoMapper;
import application.adapters.in.rest.mappers.PersonDtoMapper;
import application.adapters.in.rest.mappers.ProductDtoMapper;
import application.adapters.in.rest.mappers.ReportDtoMapper;
import application.adapters.in.rest.mappers.SellerDtoMapper;
import application.adapters.in.rest.mappers.WarehouseDtoMapper;
import application.adapters.in.rest.responses.ApiResponse;
import application.adapters.in.rest.responses.BuyerResponse;
import application.adapters.in.rest.responses.CommercialReportResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.RefundResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.SellerPerformanceReportResponse;
import application.adapters.in.rest.responses.SellerResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.TrackingEventResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.ports.in.SupervisorPort;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST adapter for the SUPERVISOR role (SupervisorPort). Read-only: it only
 * exposes GET endpoints. The route prefix is restricted to that role by the
 * security configuration.
 */
@RestController
@RequestMapping("/api/v1/supervisor")
public class SupervisorRestController {

    private final SupervisorPort supervisorPort;

    public SupervisorRestController(SupervisorPort supervisorPort) {
        this.supervisorPort = supervisorPort;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> consultUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String userId) {
        return ok("User found", PersonDtoMapper.toResponse(supervisorPort.consultUser(principal.user(), userId)));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<SellerResponse>> consultSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId) {
        return ok("Seller found", SellerDtoMapper.toResponse(supervisorPort.consultSeller(principal.user(), sellerId)));
    }

    @GetMapping("/sellers/{sellerId}/performance-report")
    public ResponseEntity<ApiResponse<SellerPerformanceReportResponse>> consultSellerPerformanceReport(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId) {
        return ok("Report generated", ReportDtoMapper.toResponse(
                supervisorPort.consultSellerPerformanceReport(principal.user(), sellerId)));
    }

    @GetMapping("/buyers/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> consultBuyer(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Buyer found", PersonDtoMapper.toResponse(supervisorPort.consultBuyer(principal.user(), buyerId)));
    }

    @GetMapping("/buyers/{buyerId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> consultBuyerOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Orders found", supervisorPort.consultBuyerOrders(principal.user(), buyerId).stream()
                .map(OrderDtoMapper::toResponse).toList());
    }

    @GetMapping("/buyers/{buyerId}/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> consultBuyerReturns(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Returns found", supervisorPort.consultBuyerReturns(principal.user(), buyerId).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/buyers/{buyerId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> consultBuyerRefunds(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Refunds found", supervisorPort.consultBuyerRefunds(principal.user(), buyerId).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consultWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String warehouseId) {
        return ok("Warehouse found",
                WarehouseDtoMapper.toResponse(supervisorPort.consultWarehouse(principal.user(), warehouseId)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consultProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId) {
        return ok("Product found",
                ProductDtoMapper.toResponse(supervisorPort.consultProduct(principal.user(), productId)));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<InventoryResponse>> consultInventory(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Inventory found", WarehouseDtoMapper.toResponse(
                supervisorPort.consultInventory(principal.user(), productId, warehouseId)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consultOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order found", OrderDtoMapper.toResponse(supervisorPort.consultOrder(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> consultInvoice(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Invoice found",
                LogisticsDtoMapper.toResponse(supervisorPort.consultInvoice(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> consultShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment found",
                LogisticsDtoMapper.toResponse(supervisorPort.consultShipment(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingEventResponse>>> consultTracking(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Tracking found", supervisorPort.consultTracking(principal.user(), orderId).stream()
                .map(LogisticsDtoMapper::toResponse).toList());
    }

    @GetMapping("/returns/{returnId}")
    public ResponseEntity<ApiResponse<ReturnResponse>> consultReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return found",
                AfterSalesDtoMapper.toResponse(supervisorPort.consultReturn(principal.user(), returnId)));
    }

    @GetMapping("/returns/{returnId}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> consultRefund(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Refund found",
                AfterSalesDtoMapper.toResponse(supervisorPort.consultRefund(principal.user(), returnId)));
    }

    @GetMapping("/reports/commercial")
    public ResponseEntity<ApiResponse<CommercialReportResponse>> consultCommercialReport(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Report generated",
                ReportDtoMapper.toResponse(supervisorPort.consultCommercialReport(principal.user())));
    }

    // ---------------------------------------------------------------- Listings and history

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String role) {
        return ok("Users found", supervisorPort.listUsers(principal.user(), FilterParamMapper.role(role)).stream()
                .map(PersonDtoMapper::toResponse).toList());
    }

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> listWarehouses(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Warehouses found", supervisorPort.listWarehouses(principal.user()).stream()
                .map(WarehouseDtoMapper::toResponse).toList());
    }

    @GetMapping("/sellers/{sellerId}/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> consultSellerProducts(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId) {
        return ok("Products found", supervisorPort.consultSellerProducts(principal.user(), sellerId).stream()
                .map(ProductDtoMapper::toResponse).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Orders found", supervisorPort.listOrders(principal.user(), FilterParamMapper.orderStatus(status)).stream()
                .map(OrderDtoMapper::toResponse).toList());
    }

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> listReturns(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Returns found", supervisorPort.listReturns(principal.user(), FilterParamMapper.returnStatus(status)).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> consultInventoryMovements(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Movement history found", supervisorPort.consultInventoryMovements(principal.user(), productId, warehouseId)
                .stream().map(WarehouseDtoMapper::toResponse).toList());
    }

    // ----------------------------------------------------------------

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.ok(message, data));
    }
}
