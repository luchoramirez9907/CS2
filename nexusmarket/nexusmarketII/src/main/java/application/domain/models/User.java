package application.domain.models;

import application.domain.valueobjects.SystemRole;

/**
 * User
 *
 * Represents the access account of a Person: the credentials used to
 * authenticate into NexusMarket. Every Person has at most one User, and the
 * User shares the identity, email, role and status of its Person.
 *
 * Business rules:
 * - Only the password hash is stored, never the raw password.
 * - authTokenVersion invalidates every previously issued access token
 *   whenever it changes (password change or logout). A blocked or inactive
 *   Person is rejected on every request regardless of its tokens.
 * - Only an active Person may authenticate.
 */
public class User {

    private final Person person;
    private String passwordHash;
    private int authTokenVersion;

    public User(Person person, String passwordHash) {
        this(person, passwordHash, 0);
    }

    private User(Person person, String passwordHash, int authTokenVersion) {
        if (person == null) {
            throw new IllegalArgumentException("A User belongs to exactly one Person");
        }
        requirePasswordHash(passwordHash);
        if (authTokenVersion < 0) {
            throw new IllegalArgumentException("Auth token version must not be negative");
        }
        this.person = person;
        this.passwordHash = passwordHash;
        this.authTokenVersion = authTokenVersion;
    }

    public Person getPerson() {
        return person;
    }

    public String getIdentifier() {
        return person.getIdentifier();
    }

    public String getEmail() {
        return person.getEmail();
    }

    public SystemRole getRole() {
        return person.getRole();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getAuthTokenVersion() {
        return authTokenVersion;
    }

    /**
     * Ensures the account may authenticate: its Person must be active.
     */
    public void requireCanAuthenticate() {
        person.requireActive();
    }

    /**
     * Replaces the password hash and invalidates every previously issued
     * access token.
     */
    public void changePasswordHash(String newPasswordHash) {
        requirePasswordHash(newPasswordHash);
        this.passwordHash = newPasswordHash;
        invalidateSessions();
    }

    /**
     * Invalidates every previously issued access token (e.g. on logout).
     */
    public void invalidateSessions() {
        authTokenVersion++;
    }

    /**
     * @return whether a token issued with the given version is still valid
     */
    public boolean acceptsTokenVersion(int tokenVersion) {
        return tokenVersion == authTokenVersion;
    }

    /**
     * Rebuilds a persisted account. Used exclusively by persistence mappers.
     */
    public static User reconstruct(Person person, String passwordHash, int authTokenVersion) {
        return new User(person, passwordHash, authTokenVersion);
    }

    private static void requirePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be null or blank");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return person.equals(((User) o).person);
    }

    @Override
    public int hashCode() {
        return person.hashCode();
    }

    @Override
    public String toString() {
        return "User{identifier='" + getIdentifier() + "', role=" + getRole().getCode() + "}";
    }
}
