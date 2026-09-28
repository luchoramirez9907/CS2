package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.ShipmentDtoMapper;
import Application.adapters.in.rest.requests.CreateShipmentRequest;
import Application.adapters.in.rest.requests.ShipmentActionRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.ShipmentResponse;
import Application.domain.models.Shipment;
import Application.domain.ports.in.ConsultShipmentUseCase;
import Application.domain.ports.in.CreateShipmentUseCase;
import Application.domain.ports.in.UpdateShipmentStatusUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Logistics Management (Create Shipment / Update
 * Shipment Status / Consult Shipment).
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;
    private final UpdateShipmentStatusUseCase updateShipmentStatusUseCase;
    private final ConsultShipmentUseCase consultShipmentUseCase;

    public ShipmentController(CreateShipmentUseCase createShipmentUseCase,
                              UpdateShipmentStatusUseCase updateShipmentStatusUseCase,
                              ConsultShipmentUseCase consultShipmentUseCase) {
        this.createShipmentUseCase = createShipmentUseCase;
        this.updateShipmentStatusUseCase = updateShipmentStatusUseCase;
        this.consultShipmentUseCase = consultShipmentUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentResponse>> create(
            @RequestBody CreateShipmentRequest request) {
        Shipment shipment = createShipmentUseCase.createShipment(
                request.operatorId(), request.orderId(), request.originWarehouseId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Shipment created", ShipmentDtoMapper.toResponse(shipment)));
    }

    @PostMapping("/{orderId}/dispatch")
    public ResponseEntity<ApiResponse<ShipmentResponse>> dispatch(
            @PathVariable String orderId,
            @RequestBody ShipmentActionRequest request) {
        Shipment shipment = updateShipmentStatusUseCase.dispatchShipment(
                request.operatorId(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment dispatched",
                ShipmentDtoMapper.toResponse(shipment)));
    }

    @PostMapping("/{orderId}/delivery")
    public ResponseEntity<ApiResponse<ShipmentResponse>> confirmDelivery(
            @PathVariable String orderId,
            @RequestBody ShipmentActionRequest request) {
        Shipment shipment = updateShipmentStatusUseCase.confirmDelivery(
                request.operatorId(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment delivered",
                ShipmentDtoMapper.toResponse(shipment)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<ShipmentResponse>> consult(
            @PathVariable String orderId,
            @RequestParam String requesterId) {
        Shipment shipment = consultShipmentUseCase.consultShipment(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment found",
                ShipmentDtoMapper.toResponse(shipment)));
    }
}
