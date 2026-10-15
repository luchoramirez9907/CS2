package application.domain.ports.out;

import application.domain.models.User;

import java.util.Optional;

/**
 * Output port: persistence contract for User access accounts.
 */
public interface UserRepository {

    void save(User user);

    /**
     * @param personIdentifier identifier of the Person owning the account
     */
    Optional<User> findByPersonId(String personIdentifier);

    Optional<User> findByEmail(String email);
}
