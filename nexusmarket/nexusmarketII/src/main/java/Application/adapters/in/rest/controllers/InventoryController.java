package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.WarehouseDtoMapper;
import Application.adapters.in.rest.requests.DamagedStockRequest;
import Application.adapters.in.rest.requests.InventoryMovementRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.InventoryResponse;
import Application.domain.models.Inventory;
import Application.domain.ports.in.ConsultInventoryUseCase;
import Application.domain.ports.in.RegisterInventoryMovementUseCase;
import Application.domain.valueobjects.InventoryMovementType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for inventory management. The requesting user is
 * identified by the X-User-Id header.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final RegisterInventoryMovementUseCase registerInventoryMovementUseCase;
    private final ConsultInventoryUseCase consultInventoryUseCase;

    public InventoryController(RegisterInventoryMovementUseCase registerInventoryMovementUseCase,
                               ConsultInventoryUseCase consultInventoryUseCase) {
        this.registerInventoryMovementUseCase = registerInventoryMovementUseCase;
        this.consultInventoryUseCase = consultInventoryUseCase;
    }

    @PostMapping("/movements")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerMovement(
            @RequestHeader("X-User-Id") String requesterId,
            @RequestBody InventoryMovementRequest request) {
        Inventory inventory = registerInventoryMovementUseCase.registerMovement(requesterId,
                request.productId(), request.warehouseId(),
                request.movementType() == null ? null : InventoryMovementType.fromCode(request.movementType()),
                request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory movement registered", WarehouseDtoMapper.toResponse(inventory)));
    }

    @PostMapping("/damaged")
    public ResponseEntity<ApiResponse<InventoryResponse>> registerDamaged(
            @RequestHeader("X-User-Id") String requesterId,
            @RequestBody DamagedStockRequest request) {
        Inventory inventory = registerInventoryMovementUseCase.registerDamagedStock(requesterId,
                request.productId(), request.warehouseId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Damaged stock registered", WarehouseDtoMapper.toResponse(inventory)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> consult(
            @RequestHeader("X-User-Id") String requesterId,
            @RequestParam String productId,
            @RequestParam String warehouseId) {
        Inventory inventory = consultInventoryUseCase.consultInventory(requesterId, productId, warehouseId);
        return ResponseEntity.ok(ApiResponse.ok("Inventory found", WarehouseDtoMapper.toResponse(inventory)));
    }
}
