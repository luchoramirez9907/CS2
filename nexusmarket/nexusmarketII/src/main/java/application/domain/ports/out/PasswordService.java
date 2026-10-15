package application.domain.ports.out;

/**
 * Output port: one-way hashing and verification of passwords. The domain
 * never stores or compares raw passwords itself.
 */
public interface PasswordService {

    /**
     * @return the hash of the raw password (salted, non-reversible)
     */
    String hash(String rawPassword);

    /**
     * @return whether the raw password corresponds to the stored hash
     */
    boolean matches(String rawPassword, String passwordHash);
}
