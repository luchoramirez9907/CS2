package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.UserDtoMapper;
import Application.adapters.in.rest.requests.ChangeUserStatusRequest;
import Application.adapters.in.rest.requests.RegisterUserRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.UserResponse;
import Application.domain.models.Person;
import Application.domain.ports.in.ManageUserUseCase;
import Application.domain.ports.in.RegisterUserUseCase;
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
 * REST adapter for User Management (Register User / Manage User).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final ManageUserUseCase manageUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          ManageUserUseCase manageUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.manageUserUseCase = manageUserUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody RegisterUserRequest request) {
        Person person = registerUserUseCase.registerUser(
                request.performerId(), request.identifier(),
                request.fullName(), request.email(), request.roleCode());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered", UserDtoMapper.toResponse(person)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> consult(
            @PathVariable String userId,
            @RequestParam String requesterId) {
        Person person = manageUserUseCase.consultUser(requesterId, userId);
        return ResponseEntity.ok(ApiResponse.ok("User found", UserDtoMapper.toResponse(person)));
    }

    @PostMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatus(
            @PathVariable String userId,
            @RequestBody ChangeUserStatusRequest request) {
        Person person = manageUserUseCase.changeUserStatus(
                request.performerId(), userId, request.action());
        return ResponseEntity.ok(ApiResponse.ok("User status updated",
                UserDtoMapper.toResponse(person)));
    }
}
