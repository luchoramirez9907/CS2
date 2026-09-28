package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.SellerDtoMapper;
import Application.adapters.in.rest.requests.ChangeSellerStatusRequest;
import Application.adapters.in.rest.requests.RegisterSellerRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.SellerResponse;
import Application.domain.models.Seller;
import Application.domain.ports.in.ManageSellerUseCase;
import Application.domain.ports.in.RegisterSellerUseCase;
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
 * REST adapter for Seller Management (Register Seller / Manage Seller).
 */
@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final RegisterSellerUseCase registerSellerUseCase;
    private final ManageSellerUseCase manageSellerUseCase;

    public SellerController(RegisterSellerUseCase registerSellerUseCase,
                            ManageSellerUseCase manageSellerUseCase) {
        this.registerSellerUseCase = registerSellerUseCase;
        this.manageSellerUseCase = manageSellerUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SellerResponse>> register(@RequestBody RegisterSellerRequest request) {
        Seller seller = registerSellerUseCase.registerSeller(
                request.administratorId(), request.identifier(), request.fullName(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Seller registered", SellerDtoMapper.toResponse(seller)));
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<SellerResponse>> consult(
            @PathVariable String sellerId,
            @RequestParam String requesterId) {
        Seller seller = manageSellerUseCase.consultSeller(requesterId, sellerId);
        return ResponseEntity.ok(ApiResponse.ok("Seller found", SellerDtoMapper.toResponse(seller)));
    }

    @PostMapping("/{sellerId}/status")
    public ResponseEntity<ApiResponse<SellerResponse>> changeStatus(
            @PathVariable String sellerId,
            @RequestBody ChangeSellerStatusRequest request) {
        Seller seller = manageSellerUseCase.changeSellerStatus(
                request.performerId(), sellerId, request.action());
        return ResponseEntity.ok(ApiResponse.ok("Seller status updated",
                SellerDtoMapper.toResponse(seller)));
    }
}
