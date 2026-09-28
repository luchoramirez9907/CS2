package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.PersonDtoMapper;
import Application.adapters.in.rest.requests.ChangeStatusRequest;
import Application.adapters.in.rest.requests.RegisterUserRequest;
import Application.adapters.in.rest.requests.UpdatePersonRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.UserResponse;
import Application.domain.models.Person;
import Application.domain.ports.in.ManageUserUseCase;
import Application.domain.ports.in.RegisterUserUseCase;
import Application.domain.valueobjects.SystemRole;
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
 * REST adapter for user management (administrators, supervisors and
 * logistics operators). The requesting user is identified by the
 * X-User-Id header.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final ManageUserUseCase manageUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase, ManageUserUseCase manageUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.manageUserUseCase = manageUserUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @RequestHeader("X-User-Id") String requesterId,
            @RequestBody RegisterUserRequest request) {
        Person user = registerUserUseCase.registerUser(requesterId, request.identifier(),
                request.fullName(), request.email(),
                request.role() == null ? null : SystemRole.fromCode(request.role()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered", PersonDtoMapper.toResponse(user)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> consult(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String userId) {
        Person user = manageUserUseCase.consultUser(requesterId, userId);
        return ResponseEntity.ok(ApiResponse.ok("User found", PersonDtoMapper.toResponse(user)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String userId,
            @RequestBody UpdatePersonRequest request) {
        Person user = manageUserUseCase.updateUser(requesterId, userId, request.fullName(), request.email());
        return ResponseEntity.ok(ApiResponse.ok("User updated", PersonDtoMapper.toResponse(user)));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> changeStatus(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String userId,
            @RequestBody ChangeStatusRequest request) {
        Person user = manageUserUseCase.changeUserStatus(requesterId, userId,
                request.status() == null ? null : UserStatus.fromCode(request.status()));
        return ResponseEntity.ok(ApiResponse.ok("User status changed", PersonDtoMapper.toResponse(user)));
    }
}
