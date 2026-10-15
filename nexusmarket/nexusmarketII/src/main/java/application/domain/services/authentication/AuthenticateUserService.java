package application.domain.services.authentication;

import application.domain.exceptions.AccountDisabledException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.AuthenticationResult;
import application.domain.models.User;
import application.domain.ports.out.PasswordService;
import application.domain.ports.out.TokenService;
import application.domain.ports.out.UserRepository;

/**
 * AuthenticateUserService
 *
 * Catalog service "Authenticate User". Validates
 * credentials against the stored password hash, issues access tokens through
 * the TokenService output port, and invalidates previous tokens on logout and
 * password change (by incrementing User.authTokenVersion).
 */
public class AuthenticateUserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthenticateUserService(UserRepository userRepository, PasswordService passwordService,
                                   TokenService tokenService) {
        if (userRepository == null || passwordService == null || tokenService == null) {
            throw new IllegalArgumentException("AuthenticateUserService requires its dependencies");
        }
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    /**
     * Validates the credentials and issues an access token.
     *
     * @throws application.domain.exceptions.InvalidCredentialsException for an unknown email or wrong password
     * @throws application.domain.exceptions.AccountDisabledException    when the person is not active
     */
    public AuthenticationResult login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordService.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.getPerson().isActive()) {
            throw new AccountDisabledException(user.getIdentifier());
        }
        return new AuthenticationResult(user, tokenService.issueToken(user), tokenService.expirationSeconds());
    }

    /**
     * Invalidates every access token previously issued to the user.
     */
    public void logout(User user) {
        requireUser(user);
        user.invalidateSessions();
        userRepository.save(user);
    }

    /**
     * Changes the password of the user after verifying the current one, and
     * invalidates every previously issued access token.
     */
    public void changePassword(User user, String currentPassword, String newPassword) {
        requireUser(user);
        if (!passwordService.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        UserAccountService.requireValidPassword(newPassword);
        if (passwordService.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("The new password must be different from the current one");
        }
        user.changePasswordHash(passwordService.hash(newPassword));
        userRepository.save(user);
    }

    private static void requireUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("An authenticated User is required");
        }
    }
}
