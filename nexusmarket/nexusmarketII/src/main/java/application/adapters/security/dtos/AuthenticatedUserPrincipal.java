package application.adapters.security.dtos;

import application.domain.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Principal stored in the Spring Security context for an authenticated
 * request. It wraps the current User domain model (loaded from the
 * repository, never rebuilt from token claims) so REST controllers can
 * pass it to the Role Input Ports. Spring Security types stay outside the
 * domain.
 */
public record AuthenticatedUserPrincipal(User user) {

    public static final String ROLE_PREFIX = "ROLE_";

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().getCode()));
    }

    @Override
    public String toString() {
        return "AuthenticatedUserPrincipal{" + user + "}";
    }
}
