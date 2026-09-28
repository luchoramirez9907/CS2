package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.LogisticsDtoMapper;
import Application.adapters.in.rest.requests.CreateShipmentRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.ShipmentResponse;
import Application.adapters.in.rest.responses.TrackingEventResponse;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST adapter for the logistics process of an order (one shipment per
 * order). The requesting user is identified by the X-User-Id header.
 */
@RestController
@RequestMapping("/api/orders/{orderId}/shipment")
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
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId,
            @RequestBody CreateShipmentRequest request) {
        Shipment shipment = createShipmentUseCase.createShipment(requesterId, orderId,
                request.originWarehouseId(), request.logisticsOperatorId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Shipment created", LogisticsDtoMapper.toResponse(shipment)));
    }

    @PostMapping("/dispatch")
    public ResponseEntity<ApiResponse<ShipmentResponse>> dispatch(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Shipment shipment = updateShipmentStatusUseCase.dispatchShipment(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment dispatched", LogisticsDtoMapper.toResponse(shipment)));
    }

    @PostMapping("/delivery")
    public ResponseEntity<ApiResponse<ShipmentResponse>> confirmDelivery(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Shipment shipment = updateShipmentStatusUseCase.confirmDelivery(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment delivered", LogisticsDtoMapper.toResponse(shipment)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ShipmentResponse>> consult(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        Shipment shipment = consultShipmentUseCase.consultShipment(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment found", LogisticsDtoMapper.toResponse(shipment)));
    }

    @GetMapping("/tracking")
    public ResponseEntity<ApiResponse<List<TrackingEventResponse>>> tracking(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String orderId) {
        List<TrackingEventResponse> events = consultShipmentUseCase.consultTracking(requesterId, orderId)
                .stream()
                .map(LogisticsDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Shipment tracking", events));
    }
}
