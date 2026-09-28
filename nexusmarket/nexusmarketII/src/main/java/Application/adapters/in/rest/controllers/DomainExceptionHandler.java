package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.responses.ApiResponse;
import Application.domain.exceptions.DomainException;
import Application.domain.exceptions.OwnershipAccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain and input errors into standardized HTTP responses.
 * Business exceptions belong exclusively to the domain; this handler only
 * maps them to transport-level status codes.
 */
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(OwnershipAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(OwnershipAccessDeniedException ex) {
        return ResponseEntity.status(403).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity.status(400).body(ApiResponse.error(
                "Missing required header '" + ex.getHeaderName() + "' (requesting user identifier)"));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        return ResponseEntity.status(422).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(409).body(ApiResponse.error(ex.getMessage()));
    }
}
