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
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.DamagedStockRequest;
import application.adapters.in.rest.requests.InventoryMovementRequest;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.adapters.in.rest.requests.UpdatePersonRequest;
import application.adapters.in.rest.requests.UpdateProductRequest;
import application.adapters.in.rest.requests.UpdateWarehouseRequest;
import application.adapters.in.rest.responses.ApiResponse;
import application.adapters.in.rest.responses.InventoryMovementResponse;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.InvoiceResponse;
import application.adapters.in.rest.responses.OrderResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.adapters.in.rest.responses.ReturnResponse;
import application.adapters.in.rest.responses.SellerPerformanceReportResponse;
import application.adapters.in.rest.responses.SellerResponse;
import application.adapters.in.rest.responses.ShipmentResponse;
import application.adapters.in.rest.responses.TrackingEventResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.models.Seller;
import application.domain.ports.in.SellerPort;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.ProductStatus;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST adapter for the SELLER role (SellerPort). The route prefix is
 * restricted to the SELLER role by the security configuration; the seller
 * only operates on its own profile, warehouses, products and inventory.
 */
@RestController
@RequestMapping("/api/v1/seller")
public class SellerRestController {

    private final SellerPort sellerPort;

    public SellerRestController(SellerPort sellerPort) {
        this.sellerPort = sellerPort;
    }

    // ---------------------------------------------------------------- Profile

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerResponse>> consultProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Profile found", SellerDtoMapper.toResponse(sellerPort.consultMyProfile(principal.user())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<SellerResponse>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdatePersonRequest request) {
        return ok("Profile updated", SellerDtoMapper.toResponse(
                sellerPort.updateMyProfile(principal.user(), request.fullName(), request.email())));
    }

    // ---------------------------------------------------------------- Warehouses

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponse>> registerWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RegisterWarehouseRequest request) {
        return created("Warehouse registered", WarehouseDtoMapper.toResponse(sellerPort.registerWarehouse(
                principal.user(), request.identifier(), request.name(), PersonDtoMapper.toAddress(request.address()))));
    }

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consultWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String warehouseId) {
        return ok("Warehouse found",
                WarehouseDtoMapper.toResponse(sellerPort.consultWarehouse(principal.user(), warehouseId)));
    }

    @PutMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        return ok("Warehouse updated", WarehouseDtoMapper.toResponse(sellerPort.updateWarehouse(principal.user(),
                warehouseId, request.name(), PersonDtoMapper.toAddress(request.address()))));
    }

    // ---------------------------------------------------------------- Catalog

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> publishProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody PublishProductRequest request) {
        Seller seller = (Seller) principal.user().getPerson();
        return created("Product published", ProductDtoMapper.toResponse(
                sellerPort.publishProduct(principal.user(), ProductDtoMapper.toDomain(request, seller))));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consultProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId) {
        return ok("Product found", ProductDtoMapper.toResponse(sellerPort.consultProduct(principal.user(), productId)));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return ok("Product updated", ProductDtoMapper.toResponse(sellerPort.updateProduct(principal.user(),
                productId, request.name(), request.description(), request.price(),
                ProductDtoMapper.toVariants(request.variants()))));
    }

    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> changeProductStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ok("Product status changed", ProductDtoMapper.toResponse(sellerPort.changeProductStatus(
                principal.user(), productId, ProductStatus.fromCode(request.status()))));
    }

    // ---------------------------------------------------------------- Inventory

    @PostMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerMovement(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody InventoryMovementRequest request) {
        return created("Inventory movement registered", WarehouseDtoMapper.toResponse(
                sellerPort.registerInventoryMovement(principal.user(), request.productId(), request.warehouseId(),
                        InventoryMovementType.fromCode(request.movementType()), request.quantity())));
    }

    @PostMapping("/inventory/damaged")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerDamagedStock(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody DamagedStockRequest request) {
        return created("Damaged stock registered", WarehouseDtoMapper.toResponse(sellerPort.registerDamagedStock(
                principal.user(), request.productId(), request.warehouseId(), request.quantity())));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<InventoryResponse>> consultInventory(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Inventory found", WarehouseDtoMapper.toResponse(
                sellerPort.consultInventory(principal.user(), productId, warehouseId)));
    }

    // ---------------------------------------------------------------- Orders and returns

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consultOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order found", OrderDtoMapper.toResponse(sellerPort.consultOrder(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> consultInvoice(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Invoice found", LogisticsDtoMapper.toResponse(sellerPort.consultInvoice(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> consultShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment found",
                LogisticsDtoMapper.toResponse(sellerPort.consultShipment(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingEventResponse>>> consultTracking(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Tracking found", sellerPort.consultTracking(principal.user(), orderId).stream()
                .map(LogisticsDtoMapper::toResponse).toList());
    }

    @GetMapping("/returns/{returnId}")
    public ResponseEntity<ApiResponse<ReturnResponse>> consultReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return found", AfterSalesDtoMapper.toResponse(sellerPort.consultReturn(principal.user(), returnId)));
    }

    // ---------------------------------------------------------------- Reporting

    @GetMapping("/reports/performance")
    public ResponseEntity<ApiResponse<SellerPerformanceReportResponse>> consultPerformanceReport(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Report generated",
                ReportDtoMapper.toResponse(sellerPort.consultMyPerformanceReport(principal.user())));
    }

    // ---------------------------------------------------------------- Listings and history

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> consultMyProducts(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Products found", sellerPort.consultMyProducts(principal.user()).stream()
                .map(ProductDtoMapper::toResponse).toList());
    }

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> consultMyWarehouses(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Warehouses found", sellerPort.consultMyWarehouses(principal.user()).stream()
                .map(WarehouseDtoMapper::toResponse).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> consultMyOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Orders found", sellerPort.consultMyOrders(principal.user(), FilterParamMapper.orderStatus(status))
                .stream().map(OrderDtoMapper::toResponse).toList());
    }

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> consultMyReturns(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Returns found", sellerPort.consultMyReturns(principal.user(), FilterParamMapper.returnStatus(status))
                .stream().map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> consultInventoryMovements(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Movement history found", sellerPort.consultInventoryMovements(principal.user(), productId, warehouseId)
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
