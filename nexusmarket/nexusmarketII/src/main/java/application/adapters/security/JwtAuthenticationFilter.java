package application.adapters.security;

import application.domain.models.User;
import application.domain.ports.out.TokenService;
import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.domain.services.authentication.LoadAuthenticatedUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Authenticates each request carrying "Authorization: Bearer <token>".
 *
 * The token signature and expiration are verified by the TokenService; the
 * current User is then loaded by LoadAuthenticatedUserService (domain)
 * from the token subject and version. The request stays
 * unauthenticated (and protected routes answer 401) when the token is
 * invalid, the account no longer exists, the person is not active, or the
 * token version is obsolete (logout or password change).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final LoadAuthenticatedUserService loadAuthenticatedUserService;

    public JwtAuthenticationFilter(TokenService tokenService,
                                   LoadAuthenticatedUserService loadAuthenticatedUserService) {
        this.tokenService = tokenService;
        this.loadAuthenticatedUserService = loadAuthenticatedUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        resolveToken(request)
                .flatMap(tokenService::parseToken)
                .flatMap(claims -> loadAuthenticatedUserService.loadAuthenticatedUser(claims.subject(),
                        claims.tokenVersion()))
                .ifPresent(this::authenticate);
        filterChain.doFilter(request, response);
    }

    private void authenticate(User user) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.authorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static Optional<String> resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(BEARER_PREFIX.length()).trim());
    }
}
