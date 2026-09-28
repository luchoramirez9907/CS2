package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.WarehouseDtoMapper;
import Application.adapters.in.rest.requests.RegisterWarehouseRequest;
import Application.adapters.in.rest.requests.UpdateWarehouseRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.WarehouseResponse;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ManageWarehouseUseCase;
import Application.domain.ports.in.RegisterWarehouseUseCase;
import Application.domain.valueobjects.Address;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Warehouse Management (Register / Manage Warehouse).
 */
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final RegisterWarehouseUseCase registerWarehouseUseCase;
    private final ManageWarehouseUseCase manageWarehouseUseCase;

    public WarehouseController(RegisterWarehouseUseCase registerWarehouseUseCase,
                               ManageWarehouseUseCase manageWarehouseUseCase) {
        this.registerWarehouseUseCase = registerWarehouseUseCase;
        this.manageWarehouseUseCase = manageWarehouseUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> register(
            @RequestBody RegisterWarehouseRequest request) {
        Address address = new Address(request.street(), request.city(), request.state(),
                request.country(), request.postalCode());
        Warehouse warehouse;
        if ("SELLER".equalsIgnoreCase(request.ownership())) {
            warehouse = registerWarehouseUseCase.registerSellerWarehouse(
                    request.performerId(), request.warehouseId(), request.name(), address);
        } else {
            warehouse = registerWarehouseUseCase.registerMarketplaceWarehouse(
                    request.performerId(), request.warehouseId(), request.name(), address);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Warehouse registered", WarehouseDtoMapper.toResponse(warehouse)));
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consult(
            @PathVariable String warehouseId,
            @RequestParam String requesterId) {
        Warehouse warehouse = manageWarehouseUseCase.consultWarehouse(requesterId, warehouseId);
        return ResponseEntity.ok(ApiResponse.ok("Warehouse found",
                WarehouseDtoMapper.toResponse(warehouse)));
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> update(
            @PathVariable String warehouseId,
            @RequestBody UpdateWarehouseRequest request) {
        Warehouse warehouse = manageWarehouseUseCase.updateWarehouse(
                request.performerId(), warehouseId, request.newName());
        return ResponseEntity.ok(ApiResponse.ok("Warehouse updated",
                WarehouseDtoMapper.toResponse(warehouse)));
    }
}
