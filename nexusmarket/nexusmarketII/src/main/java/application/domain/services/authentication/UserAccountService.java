package application.domain.services.authentication;

import application.domain.exceptions.WeakPasswordException;
import application.domain.models.Person;
import application.domain.models.User;
import application.domain.ports.out.PasswordService;
import application.domain.ports.out.UserRepository;

/**
 * UserAccountService
 *
 * Internal domain service (not exposed through an Input Port) that creates
 * the access account (User) of a Person when it is registered. It applies
 * the password policy and stores only the password hash.
 *
 * Password policy: 8 to 100 characters, with at least one letter and one
 * digit.
 */
public class UserAccountService {

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserAccountService(UserRepository userRepository, PasswordService passwordService) {
        if (userRepository == null || passwordService == null) {
            throw new IllegalArgumentException("UserAccountService requires its dependencies");
        }
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Creates and stores the access account of a newly registered Person.
     */
    public User createAccount(Person person, String rawPassword) {
        if (person == null) {
            throw new IllegalArgumentException("A User belongs to exactly one Person");
        }
        requireValidPassword(rawPassword);
        if (userRepository.findByPersonId(person.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("Person '" + person.getIdentifier()
                    + "' already has an access account");
        }
        User user = new User(person, passwordService.hash(rawPassword));
        userRepository.save(user);
        return user;
    }

    /**
     * Validates a raw password against the password policy.
     *
     * @throws WeakPasswordException when the password does not satisfy it
     */
    public static void requireValidPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException("it must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (rawPassword.length() > MAX_PASSWORD_LENGTH) {
            throw new WeakPasswordException("it must have at most " + MAX_PASSWORD_LENGTH + " characters");
        }
        if (rawPassword.chars().noneMatch(Character::isLetter)
                || rawPassword.chars().noneMatch(Character::isDigit)) {
            throw new WeakPasswordException("it must contain at least one letter and one digit");
        }
    }
}
