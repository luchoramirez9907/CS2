package application.domain;

import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.services.authentication.LoadAuthenticatedUserService;
import application.domain.services.authentication.UserAccountService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure domain tests of LoadAuthenticatedUserService: the authenticated User
 * is only resolved when it exists, its person is active and the token
 * version is still current.
 */
class LoadAuthenticatedUserServiceTest {

    private static final Address ADDRESS = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");

    private DomainBusinessRulesTest.FakeUserRepository userRepository;
    private LoadAuthenticatedUserService loader;
    private User activeUser;

    @BeforeEach
    void setUp() {
        userRepository = new DomainBusinessRulesTest.FakeUserRepository();
        UserAccountService accounts = DomainBusinessRulesTest.newUserAccountService(userRepository);
        loader = new LoadAuthenticatedUserService(userRepository);

        activeUser = accounts.createAccount(new Buyer("buyer-1", "Bruno Buyer", "buyer@x.com", ADDRESS,
                BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE), DomainBusinessRulesTest.TEST_PASSWORD);
        accounts.createAccount(new Buyer("buyer-2", "Berta Buyer", "buyer2@x.com", ADDRESS,
                BuyerCommercialStatus.ACTIVE, UserStatus.INACTIVE), DomainBusinessRulesTest.TEST_PASSWORD);
    }

    @Test
    void loadsTheActiveUserWithTheCurrentTokenVersion() {
        User loaded = loader.loadAuthenticatedUser("buyer-1", activeUser.getAuthTokenVersion()).orElseThrow();
        assertEquals("buyer-1", loaded.getIdentifier());
    }

    @Test
    void rejectsAnObsoleteTokenVersionAfterLogout() {
        int issuedVersion = activeUser.getAuthTokenVersion();
        activeUser.invalidateSessions();
        userRepository.save(activeUser);
        assertTrue(loader.loadAuthenticatedUser("buyer-1", issuedVersion).isEmpty());
    }

    @Test
    void rejectsUnknownBlankOrInactiveUsers() {
        assertTrue(loader.loadAuthenticatedUser("ghost", 0).isEmpty());
        assertTrue(loader.loadAuthenticatedUser(" ", 0).isEmpty());
        assertTrue(loader.loadAuthenticatedUser("buyer-2", 0).isEmpty());
    }

    @Test
    void requiresItsRepository() {
        assertThrows(IllegalArgumentException.class, () -> new LoadAuthenticatedUserService(null));
    }
}
