package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.requests.RegisterSellerRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.SellerResponse;
import Application.adapters.in.rest.mappers.SellerDtoMapper;
import Application.domain.models.Seller;
import Application.domain.ports.in.RegisterSellerUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for seller registration.
 * Controllers never implement business rules; they delegate to input ports.
 */
@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final RegisterSellerUseCase registerSellerUseCase;

    public SellerController(RegisterSellerUseCase registerSellerUseCase) {
        this.registerSellerUseCase = registerSellerUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SellerResponse>> register(@RequestBody RegisterSellerRequest request) {
        Seller seller = registerSellerUseCase.registerSeller(
                request.administratorId(), request.identifier(), request.fullName(), request.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Seller registered", SellerDtoMapper.toResponse(seller)));
    }
}
