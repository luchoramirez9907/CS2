package application.domain;

import application.domain.exceptions.AccountDisabledException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.WeakPasswordException;
import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.TokenService;
import application.domain.services.authentication.AuthenticateUserService;
import application.domain.services.authentication.UserAccountService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure domain tests of AuthenticateUserService (login, logout, password
 * change) with in-memory fakes for the output ports.
 */
class AuthenticateUserServiceTest {

    private static final String PASSWORD = DomainBusinessRulesTest.TEST_PASSWORD;

    private DomainBusinessRulesTest.FakeUserRepository userRepository;
    private DomainBusinessRulesTest.FakePasswordService passwordService;
    private AuthenticateUserService authentication;
    private Buyer buyer;

    @BeforeEach
    void setUp() {
        userRepository = new DomainBusinessRulesTest.FakeUserRepository();
        passwordService = new DomainBusinessRulesTest.FakePasswordService();
        authentication = new AuthenticateUserService(userRepository, passwordService, new FakeTokenService());

        buyer = new Buyer("buyer-1", "Bruno Buyer", "buyer@x.com",
                new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111"),
                BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        new UserAccountService(userRepository, passwordService).createAccount(buyer, PASSWORD);
    }

    @Test
    void loginWithValidCredentialsIssuesATokenBoundToTheCurrentVersion() {
        AuthenticationResult result = authentication.login("buyer@x.com", PASSWORD);
        assertEquals("buyer-1", result.user().getIdentifier());
        assertEquals("token:buyer-1:v0", result.accessToken());
        assertEquals(3600, result.expiresInSeconds());
        assertFalse(result.toString().contains(result.accessToken()));
    }

    @Test
    void unknownEmailAndWrongPasswordFailWithTheSameException() {
        InvalidCredentialsException unknown = assertThrows(InvalidCredentialsException.class,
                () -> authentication.login("nobody@x.com", PASSWORD));
        InvalidCredentialsException wrong = assertThrows(InvalidCredentialsException.class,
                () -> authentication.login("buyer@x.com", "WrongPass1"));
        assertEquals(unknown.getMessage(), wrong.getMessage());
        assertThrows(InvalidCredentialsException.class, () -> authentication.login(null, null));
    }

    @Test
    void aBlockedPersonCannotLogIn() {
        buyer.block();
        assertThrows(AccountDisabledException.class, () -> authentication.login("buyer@x.com", PASSWORD));
    }

    @Test
    void logoutInvalidatesPreviousTokens() {
        User user = userRepository.findByPersonId("buyer-1").orElseThrow();
        authentication.logout(user);
        assertFalse(user.acceptsTokenVersion(0));
        assertEquals("token:buyer-1:v1", authentication.login("buyer@x.com", PASSWORD).accessToken());
    }

    @Test
    void changePasswordVerifiesTheCurrentPasswordAndThePolicy() {
        User user = userRepository.findByPersonId("buyer-1").orElseThrow();
        assertThrows(InvalidCredentialsException.class,
                () -> authentication.changePassword(user, "WrongPass1", "N3wPassword"));
        assertThrows(WeakPasswordException.class,
                () -> authentication.changePassword(user, PASSWORD, "short"));
        assertThrows(IllegalArgumentException.class,
                () -> authentication.changePassword(user, PASSWORD, PASSWORD));

        authentication.changePassword(user, PASSWORD, "N3wPassword");
        assertTrue(passwordService.matches("N3wPassword", user.getPasswordHash()));
        assertEquals(1, user.getAuthTokenVersion());
        assertThrows(InvalidCredentialsException.class, () -> authentication.login("buyer@x.com", PASSWORD));
        assertEquals("buyer-1", authentication.login("buyer@x.com", "N3wPassword").user().getIdentifier());
    }

    /** Deterministic, test-only stand-in for the JWT adapter. */
    static class FakeTokenService implements TokenService {
        public String issueToken(User user) {
            return "token:" + user.getIdentifier() + ":v" + user.getAuthTokenVersion();
        }

        public Optional<TokenClaims> parseToken(String token) {
            String[] parts = token.split(":");
            return Optional.of(new TokenClaims(parts[1], token, Integer.parseInt(parts[2].substring(1)),
                    Instant.now(), Instant.now().plusSeconds(3600)));
        }

        public long expirationSeconds() {
            return 3600;
        }
    }
}
