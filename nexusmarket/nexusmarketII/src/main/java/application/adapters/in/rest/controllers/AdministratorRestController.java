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
import application.adapters.in.rest.requests.CreateShipmentRequest;
import application.adapters.in.rest.requests.DamagedStockRequest;
import application.adapters.in.rest.requests.InventoryMovementRequest;
import application.adapters.in.rest.requests.ProcessRefundRequest;
import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.requests.RegisterSellerRequest;
import application.adapters.in.rest.requests.RegisterUserRequest;
import application.adapters.in.rest.requests.RegisterWarehouseRequest;
import application.adapters.in.rest.requests.ShipmentReturnRequest;
import application.adapters.in.rest.requests.UpdateBuyerRequest;
import application.adapters.in.rest.requests.UpdatePersonRequest;
import application.adapters.in.rest.requests.UpdateWarehouseRequest;
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
import application.domain.ports.in.AdministratorPort;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
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
 * REST adapter for the ADMINISTRATOR role (AdministratorPort). The route
 * prefix is restricted to that role by the security configuration.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdministratorRestController {

    private final AdministratorPort administratorPort;

    public AdministratorRestController(AdministratorPort administratorPort) {
        this.administratorPort = administratorPort;
    }

    // ---------------------------------------------------------------- Users

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RegisterUserRequest request) {
        return created("User registered", PersonDtoMapper.toResponse(administratorPort.registerUser(principal.user(),
                request.identifier(), request.fullName(), request.email(), SystemRole.fromCode(request.role()),
                request.password())));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> consultUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String userId) {
        return ok("User found", PersonDtoMapper.toResponse(administratorPort.consultUser(principal.user(), userId)));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String userId,
            @Valid @RequestBody UpdatePersonRequest request) {
        return ok("User updated", PersonDtoMapper.toResponse(
                administratorPort.updateUser(principal.user(), userId, request.fullName(), request.email())));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String userId,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ok("User status changed", PersonDtoMapper.toResponse(administratorPort.changeUserStatus(
                principal.user(), userId, UserStatus.fromCode(request.status()))));
    }

    // ---------------------------------------------------------------- Sellers

    @PostMapping("/sellers")
    public ResponseEntity<ApiResponse<SellerResponse>> registerSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RegisterSellerRequest request) {
        return created("Seller registered", SellerDtoMapper.toResponse(administratorPort.registerSeller(
                principal.user(), request.identifier(), request.fullName(), request.email(), request.password())));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<SellerResponse>> consultSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId) {
        return ok("Seller found",
                SellerDtoMapper.toResponse(administratorPort.consultSeller(principal.user(), sellerId)));
    }

    @PutMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<SellerResponse>> updateSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId,
            @Valid @RequestBody UpdatePersonRequest request) {
        return ok("Seller updated", SellerDtoMapper.toResponse(
                administratorPort.updateSeller(principal.user(), sellerId, request.fullName(), request.email())));
    }

    @PatchMapping("/sellers/{sellerId}/status")
    public ResponseEntity<ApiResponse<SellerResponse>> changeSellerStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ok("Seller status changed", SellerDtoMapper.toResponse(administratorPort.changeSellerStatus(
                principal.user(), sellerId, UserStatus.fromCode(request.status()))));
    }

    @GetMapping("/sellers/{sellerId}/performance-report")
    public ResponseEntity<ApiResponse<SellerPerformanceReportResponse>> consultSellerPerformanceReport(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId) {
        return ok("Report generated", ReportDtoMapper.toResponse(
                administratorPort.consultSellerPerformanceReport(principal.user(), sellerId)));
    }

    // ---------------------------------------------------------------- Buyers

    @PostMapping("/buyers")
    public ResponseEntity<ApiResponse<BuyerResponse>> registerBuyer(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RegisterBuyerRequest request) {
        return created("Buyer registered", PersonDtoMapper.toResponse(administratorPort.registerBuyer(
                principal.user(), request.identifier(), request.fullName(), request.email(),
                PersonDtoMapper.toAddress(request.primaryAddress()), request.password())));
    }

    @GetMapping("/buyers/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> consultBuyer(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Buyer found", PersonDtoMapper.toResponse(administratorPort.consultBuyer(principal.user(), buyerId)));
    }

    @PutMapping("/buyers/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> updateBuyer(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId,
            @Valid @RequestBody UpdateBuyerRequest request) {
        return ok("Buyer updated", PersonDtoMapper.toResponse(administratorPort.updateBuyer(principal.user(),
                buyerId, request.fullName(), request.email(), PersonDtoMapper.toAddress(request.primaryAddress()),
                PersonDtoMapper.toAddresses(request.additionalAddresses()))));
    }

    @PatchMapping("/buyers/{buyerId}/commercial-status")
    public ResponseEntity<ApiResponse<BuyerResponse>> changeBuyerCommercialStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ok("Buyer commercial status changed", PersonDtoMapper.toResponse(
                administratorPort.changeBuyerCommercialStatus(principal.user(), buyerId,
                        BuyerCommercialStatus.fromCode(request.status()))));
    }

    @GetMapping("/buyers/{buyerId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> consultBuyerOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Orders found", administratorPort.consultBuyerOrders(principal.user(), buyerId).stream()
                .map(OrderDtoMapper::toResponse).toList());
    }

    @GetMapping("/buyers/{buyerId}/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> consultBuyerReturns(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Returns found", administratorPort.consultBuyerReturns(principal.user(), buyerId).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/buyers/{buyerId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> consultBuyerRefunds(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String buyerId) {
        return ok("Refunds found", administratorPort.consultBuyerRefunds(principal.user(), buyerId).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    // ---------------------------------------------------------------- Warehouses

    @PostMapping("/warehouses")
    public ResponseEntity<ApiResponse<WarehouseResponse>> registerWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RegisterWarehouseRequest request) {
        return created("Warehouse registered", WarehouseDtoMapper.toResponse(administratorPort.registerWarehouse(
                principal.user(), request.identifier(), request.name(), PersonDtoMapper.toAddress(request.address()),
                request.ownerSellerId())));
    }

    @GetMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consultWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String warehouseId) {
        return ok("Warehouse found",
                WarehouseDtoMapper.toResponse(administratorPort.consultWarehouse(principal.user(), warehouseId)));
    }

    @PutMapping("/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        return ok("Warehouse updated", WarehouseDtoMapper.toResponse(administratorPort.updateWarehouse(
                principal.user(), warehouseId, request.name(), PersonDtoMapper.toAddress(request.address()))));
    }

    // ---------------------------------------------------------------- Catalog and inventory

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consultProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId) {
        return ok("Product found",
                ProductDtoMapper.toResponse(administratorPort.consultProduct(principal.user(), productId)));
    }

    @PatchMapping("/products/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> changeProductStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String productId,
            @Valid @RequestBody ChangeStatusRequest request) {
        return ok("Product status changed", ProductDtoMapper.toResponse(administratorPort.changeProductStatus(
                principal.user(), productId, ProductStatus.fromCode(request.status()))));
    }

    @PostMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerMovement(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody InventoryMovementRequest request) {
        return created("Inventory movement registered", WarehouseDtoMapper.toResponse(
                administratorPort.registerInventoryMovement(principal.user(), request.productId(),
                        request.warehouseId(), InventoryMovementType.fromCode(request.movementType()),
                        request.quantity())));
    }

    @PostMapping("/inventory/damaged")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerDamagedStock(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody DamagedStockRequest request) {
        return created("Damaged stock registered", WarehouseDtoMapper.toResponse(
                administratorPort.registerDamagedStock(principal.user(), request.productId(), request.warehouseId(),
                        request.quantity())));
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<InventoryResponse>> consultInventory(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Inventory found", WarehouseDtoMapper.toResponse(
                administratorPort.consultInventory(principal.user(), productId, warehouseId)));
    }

    // ---------------------------------------------------------------- Orders and billing

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> consultOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order found", OrderDtoMapper.toResponse(administratorPort.consultOrder(principal.user(), orderId)));
    }

    @PostMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Payment confirmed",
                OrderDtoMapper.toResponse(administratorPort.confirmOrderPayment(principal.user(), orderId)));
    }

    @PostMapping("/orders/{orderId}/finalization")
    public ResponseEntity<ApiResponse<OrderResponse>> finalizeOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Order finalized",
                OrderDtoMapper.toResponse(administratorPort.finalizeOrder(principal.user(), orderId)));
    }

    @PostMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return created("Invoice generated",
                LogisticsDtoMapper.toResponse(administratorPort.generateInvoice(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> consultInvoice(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Invoice found",
                LogisticsDtoMapper.toResponse(administratorPort.consultInvoice(principal.user(), orderId)));
    }

    // ---------------------------------------------------------------- Logistics

    @PostMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId,
            @Valid @RequestBody CreateShipmentRequest request) {
        return created("Shipment created", LogisticsDtoMapper.toResponse(administratorPort.createShipment(
                principal.user(), orderId, request.originWarehouseId(), request.logisticsOperatorId())));
    }

    @PostMapping("/orders/{orderId}/shipment/dispatch")
    public ResponseEntity<ApiResponse<ShipmentResponse>> dispatchShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment dispatched",
                LogisticsDtoMapper.toResponse(administratorPort.dispatchShipment(principal.user(), orderId)));
    }

    @PostMapping("/orders/{orderId}/shipment/delivery")
    public ResponseEntity<ApiResponse<ShipmentResponse>> confirmDelivery(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Delivery confirmed",
                LogisticsDtoMapper.toResponse(administratorPort.confirmDelivery(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> consultShipment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Shipment found",
                LogisticsDtoMapper.toResponse(administratorPort.consultShipment(principal.user(), orderId)));
    }

    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingEventResponse>>> consultTracking(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId) {
        return ok("Tracking found", administratorPort.consultTracking(principal.user(), orderId).stream()
                .map(LogisticsDtoMapper::toResponse).toList());
    }

    // ---------------------------------------------------------------- Returns and refunds

    @PostMapping("/returns/{returnId}/approval")
    public ResponseEntity<ApiResponse<ReturnResponse>> approveReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return approved",
                AfterSalesDtoMapper.toResponse(administratorPort.approveReturn(principal.user(), returnId)));
    }

    @PostMapping("/returns/{returnId}/rejection")
    public ResponseEntity<ApiResponse<ReturnResponse>> rejectReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return rejected",
                AfterSalesDtoMapper.toResponse(administratorPort.rejectReturn(principal.user(), returnId)));
    }

    @PostMapping("/returns/{returnId}/completion")
    public ResponseEntity<ApiResponse<ReturnResponse>> completeReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return completed; stock reinstated",
                AfterSalesDtoMapper.toResponse(administratorPort.completeReturn(principal.user(), returnId)));
    }

    @GetMapping("/returns/{returnId}")
    public ResponseEntity<ApiResponse<ReturnResponse>> consultReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Return found",
                AfterSalesDtoMapper.toResponse(administratorPort.consultReturn(principal.user(), returnId)));
    }

    @PostMapping("/returns/{returnId}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId,
            @Valid @RequestBody(required = false) ProcessRefundRequest request) {
        return created("Refund processed", AfterSalesDtoMapper.toResponse(administratorPort.processRefund(
                principal.user(), returnId, request == null ? null : request.amount())));
    }

    @GetMapping("/returns/{returnId}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> consultRefund(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Refund found",
                AfterSalesDtoMapper.toResponse(administratorPort.consultRefund(principal.user(), returnId)));
    }

    // ---------------------------------------------------------------- Reporting

    @GetMapping("/reports/commercial")
    public ResponseEntity<ApiResponse<CommercialReportResponse>> consultCommercialReport(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Report generated",
                ReportDtoMapper.toResponse(administratorPort.consultCommercialReport(principal.user())));
    }

    // ---------------------------------------------------------------- Listings and history

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String role) {
        return ok("Users found", administratorPort.listUsers(principal.user(), FilterParamMapper.role(role)).stream()
                .map(PersonDtoMapper::toResponse).toList());
    }

    @GetMapping("/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> listWarehouses(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ok("Warehouses found", administratorPort.listWarehouses(principal.user()).stream()
                .map(WarehouseDtoMapper::toResponse).toList());
    }

    @GetMapping("/sellers/{sellerId}/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> consultSellerProducts(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String sellerId) {
        return ok("Products found", administratorPort.consultSellerProducts(principal.user(), sellerId).stream()
                .map(ProductDtoMapper::toResponse).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Orders found", administratorPort.listOrders(principal.user(), FilterParamMapper.orderStatus(status)).stream()
                .map(OrderDtoMapper::toResponse).toList());
    }

    @GetMapping("/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> listReturns(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(required = false) String status) {
        return ok("Returns found", administratorPort.listReturns(principal.user(), FilterParamMapper.returnStatus(status)).stream()
                .map(AfterSalesDtoMapper::toResponse).toList());
    }

    @GetMapping("/inventory/movements")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponse>>> consultInventoryMovements(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam String productId, @RequestParam String warehouseId) {
        return ok("Movement history found", administratorPort.consultInventoryMovements(principal.user(), productId, warehouseId)
                .stream().map(WarehouseDtoMapper::toResponse).toList());
    }

    // ---------------------------------------------------------------- Failed deliveries and refund decisions

    @PostMapping("/orders/{orderId}/shipment/return")
    public ResponseEntity<ApiResponse<ShipmentResponse>> registerShipmentReturn(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String orderId,
            @Valid @RequestBody ShipmentReturnRequest request) {
        return ok("Shipment returned to the warehouse", LogisticsDtoMapper.toResponse(
                administratorPort.registerShipmentReturn(principal.user(), orderId, request.reason())));
    }

    @PostMapping("/returns/{returnId}/refund/rejection")
    public ResponseEntity<ApiResponse<RefundResponse>> rejectRefund(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable String returnId) {
        return ok("Refund rejected",
                AfterSalesDtoMapper.toResponse(administratorPort.rejectRefund(principal.user(), returnId)));
    }

    // ----------------------------------------------------------------

    private static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.ok(message, data));
    }

    private static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, data));
    }
}
