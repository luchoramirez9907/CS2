package application.domain.services.authentication;

import application.domain.models.User;
import application.domain.ports.out.UserRepository;

import java.util.Optional;

/**
 * LoadAuthenticatedUserService
 *
 * Internal domain service that resolves the authenticated User of a request
 * from the identity carried by its access token (person identifier and
 * token version). The token never carries the role or the status: they are
 * always read from the current User, so a blocked account or a logout takes
 * effect immediately. The security adapter (JwtAuthenticationFilter) only
 * wraps the result in its principal.
 */
public class LoadAuthenticatedUserService {

    private final UserRepository userRepository;

    public LoadAuthenticatedUserService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("LoadAuthenticatedUserService requires its dependencies");
        }
        this.userRepository = userRepository;
    }

    /**
     * @param personId     identifier of the Person owning the account (claim sub)
     * @param tokenVersion authTokenVersion the token was issued with (claim ver)
     * @return the User when it exists, its person is active and the token
     *         version is still current; empty otherwise
     */
    public Optional<User> loadAuthenticatedUser(String personId, int tokenVersion) {
        if (personId == null || personId.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByPersonId(personId)
                .filter(user -> user.getPerson().isActive())
                .filter(user -> user.acceptsTokenVersion(tokenVersion));
    }
}
