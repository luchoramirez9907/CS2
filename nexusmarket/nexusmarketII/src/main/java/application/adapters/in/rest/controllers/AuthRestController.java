package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.AuthDtoMapper;
import application.adapters.in.rest.requests.ChangePasswordRequest;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.responses.ApiResponse;
import application.adapters.in.rest.responses.LoginResponse;
import application.domain.ports.in.PublicAccessPort;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for authentication (PublicAccessPort). Login is public;
 * logout and password change act on the authenticated user of any role.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;

    public AuthRestController(PublicAccessPort publicAccessPort) {
        this.publicAccessPort = publicAccessPort;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Authenticated",
                AuthDtoMapper.toResponse(publicAccessPort.login(request.email(), request.password()))));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        publicAccessPort.logout(principal.user());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        publicAccessPort.changeMyPassword(principal.user(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
