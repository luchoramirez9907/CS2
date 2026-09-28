package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.requests.ChangeStatusRequest;
import Application.adapters.in.rest.requests.RegisterSellerRequest;
import Application.adapters.in.rest.requests.UpdatePersonRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.SellerResponse;
import Application.adapters.in.rest.mappers.SellerDtoMapper;
import Application.domain.models.Seller;
import Application.domain.ports.in.ManageSellerUseCase;
import Application.domain.ports.in.RegisterSellerUseCase;
import Application.domain.valueobjects.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for seller registration and management.
 * Controllers never implement business rules; they delegate to input ports.
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
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String sellerId) {
        Seller seller = manageSellerUseCase.consultSeller(requesterId, sellerId);
        return ResponseEntity.ok(ApiResponse.ok("Seller found", SellerDtoMapper.toResponse(seller)));
    }

    @PutMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<SellerResponse>> update(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String sellerId,
            @RequestBody UpdatePersonRequest request) {
        Seller seller = manageSellerUseCase.updateSeller(requesterId, sellerId,
                request.fullName(), request.email());
        return ResponseEntity.ok(ApiResponse.ok("Seller updated", SellerDtoMapper.toResponse(seller)));
    }

    @PatchMapping("/{sellerId}/status")
    public ResponseEntity<ApiResponse<SellerResponse>> changeStatus(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String sellerId,
            @RequestBody ChangeStatusRequest request) {
        Seller seller = manageSellerUseCase.changeSellerStatus(requesterId, sellerId,
                request.status() == null ? null : UserStatus.fromCode(request.status()));
        return ResponseEntity.ok(ApiResponse.ok("Seller status changed", SellerDtoMapper.toResponse(seller)));
    }
}
