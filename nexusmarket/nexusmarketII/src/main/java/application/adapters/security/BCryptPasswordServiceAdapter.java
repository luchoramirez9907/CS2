package application.adapters.security;

import application.domain.ports.out.PasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing the PasswordService output port with the BCrypt
 * PasswordEncoder configured in SecurityConfig.
 */
@Component
public class BCryptPasswordServiceAdapter implements PasswordService {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordServiceAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return rawPassword != null && passwordHash != null
                && passwordEncoder.matches(rawPassword, passwordHash);
    }
}
