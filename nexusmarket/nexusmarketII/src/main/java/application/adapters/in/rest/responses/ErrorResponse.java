package application.adapters.in.rest.responses;

import java.time.Instant;

/**
 * Standard error envelope returned by every REST endpoint and by the
 * security boundary (401/403). "code" is stable and machine-readable;
 * clients must not parse "message".
 *
 * @param timestamp ISO-8601 instant of the error
 * @param status    HTTP status code
 * @param code      stable error code (see constants)
 * @param message   client-safe description
 * @param path      request path (without query string)
 * @param requestId correlation identifier (same as the X-Request-Id header)
 * @param details   optional client-safe details (e.g. field validation errors)
 */
public record ErrorResponse(String timestamp, int status, String code, String message, String path,
                            String requestId, Object details) {

    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";
    public static final String INVALID_STATE_TRANSITION = "INVALID_STATE_TRANSITION";
    public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String DEPENDENCY_UNAVAILABLE = "DEPENDENCY_UNAVAILABLE";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    public static ErrorResponse of(int status, String code, String message, String path, String requestId,
                                   Object details) {
        return new ErrorResponse(Instant.now().toString(), status, code, message, path, requestId, details);
    }
}
