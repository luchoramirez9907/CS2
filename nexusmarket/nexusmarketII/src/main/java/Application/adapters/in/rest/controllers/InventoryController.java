package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.InventorySnapshotDtoMapper;
import Application.adapters.in.rest.requests.InventoryMovementRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.InventoryResponse;
import Application.domain.models.Inventory;
import Application.domain.ports.in.ConsultInventoryUseCase;
import Application.domain.ports.in.RegisterInventoryMovementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Inventory Management (Register Inventory Movement /
 * Consult Inventory).
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
            @RequestBody InventoryMovementRequest request) {
        Inventory inventory;
        if ("ADJUSTMENT".equalsIgnoreCase(request.movementType())) {
            inventory = registerInventoryMovementUseCase.registerAdjustment(
                    request.performerId(), request.productId(),
                    request.warehouseId(), request.quantity());
        } else {
            inventory = registerInventoryMovementUseCase.registerStockIn(
                    request.performerId(), request.productId(),
                    request.warehouseId(), request.quantity());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory movement registered",
                        InventorySnapshotDtoMapper.toResponse(
                                new Application.domain.ports.in.ConsultInventoryUseCase.StockSnapshot(
                                        inventory.getIdentifier(),
                                        inventory.getProduct().getIdentifier(),
                                        inventory.getWarehouse().getIdentifier(),
                                        inventory.getAvailableQuantity(),
                                        inventory.getReservedQuantity()))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> consultStock(
            @RequestParam String requesterId,
            @RequestParam String productId,
            @RequestParam String warehouseId) {
        ConsultInventoryUseCase.StockSnapshot snapshot =
                consultInventoryUseCase.consultStock(requesterId, productId, warehouseId);
        return ResponseEntity.ok(ApiResponse.ok("Stock consulted",
                InventorySnapshotDtoMapper.toResponse(snapshot)));
    }
}
