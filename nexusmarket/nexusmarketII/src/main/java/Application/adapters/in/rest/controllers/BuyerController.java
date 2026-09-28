package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.BuyerActivityDtoMapper;
import Application.adapters.in.rest.mappers.BuyerDtoMapper;
import Application.adapters.in.rest.requests.ChangeBuyerCommercialStatusRequest;
import Application.adapters.in.rest.requests.RegisterBuyerRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.BuyerActivityResponse;
import Application.adapters.in.rest.responses.BuyerResponse;
import Application.domain.models.Buyer;
import Application.domain.ports.in.ManageBuyerUseCase;
import Application.domain.ports.in.RegisterBuyerUseCase;
import Application.domain.valueobjects.Address;
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
 * REST adapter for Buyer Management (Register Buyer / Manage Buyer).
 */
@RestController
@RequestMapping("/api/buyers")
public class BuyerController {

    private final RegisterBuyerUseCase registerBuyerUseCase;
    private final ManageBuyerUseCase manageBuyerUseCase;

    public BuyerController(RegisterBuyerUseCase registerBuyerUseCase,
                           ManageBuyerUseCase manageBuyerUseCase) {
        this.registerBuyerUseCase = registerBuyerUseCase;
        this.manageBuyerUseCase = manageBuyerUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BuyerResponse>> register(@RequestBody RegisterBuyerRequest request) {
        Buyer buyer = registerBuyerUseCase.registerBuyer(
                request.identifier(), request.fullName(), request.email(),
                new Address(request.street(), request.city(), request.state(),
                        request.country(), request.postalCode()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Buyer registered", BuyerDtoMapper.toResponse(buyer)));
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> consult(
            @PathVariable String buyerId,
            @RequestParam String requesterId) {
        Buyer buyer = manageBuyerUseCase.consultBuyer(requesterId, buyerId);
        return ResponseEntity.ok(ApiResponse.ok("Buyer found", BuyerDtoMapper.toResponse(buyer)));
    }

    @GetMapping("/{buyerId}/activity")
    public ResponseEntity<ApiResponse<BuyerActivityResponse>> activity(
            @PathVariable String buyerId,
            @RequestParam String requesterId) {
        ManageBuyerUseCase.BuyerActivity activity =
                manageBuyerUseCase.consultBuyerActivity(requesterId, buyerId);
        return ResponseEntity.ok(ApiResponse.ok("Buyer activity",
                BuyerActivityDtoMapper.toResponse(activity)));
    }

    @PostMapping("/{buyerId}/commercial-status")
    public ResponseEntity<ApiResponse<BuyerResponse>> changeCommercialStatus(
            @PathVariable String buyerId,
            @RequestBody ChangeBuyerCommercialStatusRequest request) {
        Buyer buyer = manageBuyerUseCase.changeCommercialStatus(
                request.performerId(), buyerId, request.statusCode());
        return ResponseEntity.ok(ApiResponse.ok("Buyer commercial status updated",
                BuyerDtoMapper.toResponse(buyer)));
    }
}
