package application.adapters.in.rest.exception;

import application.adapters.in.rest.responses.ErrorResponse;
import application.domain.exceptions.AccountDisabledException;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.DuplicateEmailException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidRoleAssignmentException;
import application.domain.exceptions.OrderAlreadyFinalizedException;
import application.domain.exceptions.OwnershipAccessDeniedException;
import application.domain.exceptions.SellerSelfRegistrationNotAllowedException;
import application.adapters.security.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

/**
 * Single error contract for every REST endpoint: translates validation,
 * domain and infrastructure exceptions into the standard ErrorResponse
 * envelope. Domain exceptions stay framework-independent; only this
 * adapter knows about HTTP status codes.
 *
 * Unexpected exceptions never expose internal details: they return a
 * generic 500 and are logged with the request identifier. Every rejected
 * request (4xx) is logged as a warning with its request identifier, method,
 * path, status and error code, so a failing call can be traced in the logs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------- 400: request shape and format

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::toDetail)
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.INVALID_REQUEST, "Request validation failed",
                request, fieldErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameters(HandlerMethodValidationException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.INVALID_REQUEST, "Request validation failed",
                request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.INVALID_REQUEST, "Malformed JSON request body",
                request, null);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadParameter(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.INVALID_REQUEST, "Missing or invalid request parameter",
                request, null);
    }

    /** Domain input validations (blank values, unknown catalog codes, illegal arguments). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorResponse.INVALID_REQUEST, ex.getMessage(), request, null);
    }

    // ---------------------------------------------------------------- 401 / 403

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ErrorResponse.INVALID_CREDENTIALS, ex.getMessage(), request, null);
    }

    @ExceptionHandler({AccountDisabledException.class, InvalidRoleAssignmentException.class,
            OwnershipAccessDeniedException.class, SellerSelfRegistrationNotAllowedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ErrorResponse.FORBIDDEN, ex.getMessage(), request, null);
    }

    // ---------------------------------------------------------------- 404 / 405 / 409

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorResponse.RESOURCE_NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleUnknownRoute(NoResourceFoundException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorResponse.RESOURCE_NOT_FOUND, "Requested resource was not found",
                request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorResponse.INVALID_REQUEST,
                "HTTP method not supported for this resource", request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorResponse.INVALID_REQUEST,
                "Content-Type must be application/json", request, null);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateEmailException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ErrorResponse.RESOURCE_ALREADY_EXISTS, ex.getMessage(), request, null);
    }

    /** Illegal state transitions: finalized orders, inactive participants. */
    @ExceptionHandler({OrderAlreadyFinalizedException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ErrorResponse.INVALID_STATE_TRANSITION, ex.getMessage(), request, null);
    }

    // ---------------------------------------------------------------- 422: business rules

    /**
     * Any other business rule violation: insufficient inventory, weak
     * password, return or refund not allowed, buyer not authorized to buy.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ErrorResponse.BUSINESS_RULE_VIOLATION, ex.getMessage(),
                request, null);
    }

    // ---------------------------------------------------------------- 503 / 500

    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    public ResponseEntity<ErrorResponse> handleUnavailable(Exception ex, HttpServletRequest request) {
        log.error("Dependency unavailable [requestId={}]", RequestIdFilter.currentRequestId(request), ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, ErrorResponse.DEPENDENCY_UNAVAILABLE,
                "A required service is temporarily unavailable", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error [requestId={}]", RequestIdFilter.currentRequestId(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponse.INTERNAL_ERROR, "An unexpected error occurred",
                request, null);
    }

    // ----------------------------------------------------------------

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                       HttpServletRequest request, Object details) {
        String requestId = RequestIdFilter.currentRequestId(request);
        if (status.is4xxClientError()) {
            // Rejected requests are traced with their request id; 5xx are logged with the stack trace above.
            log.warn("Request rejected [requestId={}] {} {} -> {} {}: {}", requestId, request.getMethod(),
                    request.getRequestURI(), status.value(), code, message);
        }
        ErrorResponse body = ErrorResponse.of(status.value(), code, message, request.getRequestURI(),
                requestId, details);
        return ResponseEntity.status(status).body(body);
    }

    private static Map<String, String> toDetail(FieldError error) {
        return Map.of("field", error.getField(),
                "message", error.getDefaultMessage() == null ? "invalid value" : error.getDefaultMessage());
    }
}
