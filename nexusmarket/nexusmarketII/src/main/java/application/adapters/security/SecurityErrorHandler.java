package application.adapters.security;

import application.adapters.in.rest.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Writes the standard error envelope for failures raised by the Spring
 * Security filter chain, which @RestControllerAdvice does not handle:
 * 401 when a protected route is called without a valid token, 403 when the
 * authenticated role is not allowed on the route. Both are logged as a warning
 * with the request identifier, like the rejections of GlobalExceptionHandler.
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandler.class);

    private final ObjectMapper objectMapper;

    public SecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        write(request, response, HttpStatus.UNAUTHORIZED, ErrorResponse.AUTHENTICATION_REQUIRED,
                "A valid access token is required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        write(request, response, HttpStatus.FORBIDDEN, ErrorResponse.FORBIDDEN,
                "The authenticated role is not allowed to perform this operation");
    }

    private void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                       String code, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        String requestId = RequestIdFilter.currentRequestId(request);
        log.warn("Request rejected [requestId={}] {} {} -> {} {}: {}", requestId, request.getMethod(),
                request.getRequestURI(), status.value(), code, message);
        ErrorResponse body = ErrorResponse.of(status.value(), code, message, request.getRequestURI(),
                requestId, null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
