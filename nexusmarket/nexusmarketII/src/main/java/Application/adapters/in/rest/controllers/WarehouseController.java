package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.PersonDtoMapper;
import Application.adapters.in.rest.mappers.WarehouseDtoMapper;
import Application.adapters.in.rest.requests.RegisterWarehouseRequest;
import Application.adapters.in.rest.requests.UpdateWarehouseRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.WarehouseResponse;
import Application.domain.models.Warehouse;
import Application.domain.ports.in.ManageWarehouseUseCase;
import Application.domain.ports.in.RegisterWarehouseUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for warehouse management. The requesting user is
 * identified by the X-User-Id header.
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
            @RequestHeader("X-User-Id") String requesterId,
            @RequestBody RegisterWarehouseRequest request) {
        Warehouse warehouse = registerWarehouseUseCase.registerWarehouse(requesterId, request.identifier(),
                request.name(), PersonDtoMapper.toAddress(request.address()), request.ownerSellerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Warehouse registered", WarehouseDtoMapper.toResponse(warehouse)));
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> consult(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String warehouseId) {
        Warehouse warehouse = manageWarehouseUseCase.consultWarehouse(requesterId, warehouseId);
        return ResponseEntity.ok(ApiResponse.ok("Warehouse found", WarehouseDtoMapper.toResponse(warehouse)));
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> update(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String warehouseId,
            @RequestBody UpdateWarehouseRequest request) {
        Warehouse warehouse = manageWarehouseUseCase.updateWarehouse(requesterId, warehouseId,
                request.name(), PersonDtoMapper.toAddress(request.address()));
        return ResponseEntity.ok(ApiResponse.ok("Warehouse updated", WarehouseDtoMapper.toResponse(warehouse)));
    }
}
