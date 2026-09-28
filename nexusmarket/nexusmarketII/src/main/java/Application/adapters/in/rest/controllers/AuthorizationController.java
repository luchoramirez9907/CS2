package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.requests.ValidateOwnershipRequest;
import Application.adapters.in.rest.requests.ValidatePermissionRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.AuthorizationResponse;
import Application.domain.enums.BusinessOperation;
import Application.domain.ports.in.ValidateOwnershipAccessUseCase;
import Application.domain.ports.in.ValidatePermissionsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Authorization (Validate Permissions / Validate
 * Ownership Access).
 */
@RestController
@RequestMapping("/api/authorization")
public class AuthorizationController {

    private final ValidatePermissionsUseCase validatePermissionsUseCase;
    private final ValidateOwnershipAccessUseCase validateOwnershipAccessUseCase;

    public AuthorizationController(ValidatePermissionsUseCase validatePermissionsUseCase,
                                   ValidateOwnershipAccessUseCase validateOwnershipAccessUseCase) {
        this.validatePermissionsUseCase = validatePermissionsUseCase;
        this.validateOwnershipAccessUseCase = validateOwnershipAccessUseCase;
    }

    @PostMapping("/permissions")
    public ResponseEntity<ApiResponse<AuthorizationResponse>> validatePermission(
            @RequestBody ValidatePermissionRequest request) {
        boolean allowed;
        String detail;
        try {
            BusinessOperation operation = BusinessOperation.valueOf(request.operation());
            allowed = validatePermissionsUseCase.canPerform(request.userId(), operation);
            detail = operation.getDescription();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unknown operation: " + request.operation()));
        }
        return ResponseEntity.ok(ApiResponse.ok(allowed ? "Operation allowed" : "Operation denied",
                new AuthorizationResponse(request.userId(), allowed, detail)));
    }

    @PostMapping("/ownership")
    public ResponseEntity<ApiResponse<AuthorizationResponse>> validateOwnership(
            @RequestBody ValidateOwnershipRequest request) {
        boolean allowed = validateOwnershipAccessUseCase.canAccess(
                request.userId(), request.resourceType(), request.resourceId());
        return ResponseEntity.ok(ApiResponse.ok(allowed ? "Access allowed" : "Access denied",
                new AuthorizationResponse(request.userId(), allowed,
                        request.resourceType() + "/" + request.resourceId())));
    }
}
