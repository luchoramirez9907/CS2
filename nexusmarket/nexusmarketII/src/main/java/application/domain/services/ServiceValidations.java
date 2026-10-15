package application.domain.services;

import application.domain.exceptions.DuplicateEmailException;
import application.domain.ports.out.PersonRepository;

/**
 * Input validations shared by the domain services.
 */
public final class ServiceValidations {

    private ServiceValidations() {
    }

    public static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }

    /**
     * identifier and email must be unique across the platform.
     */
    public static void requireUniqueIdentity(PersonRepository personRepository, String identifier, String email) {
        if (personRepository.existsByIdentifier(identifier)) {
            throw new DuplicateEmailException(identifier);
        }
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    /**
     * Ensures the email is not used by a person other than the given one.
     */
    public static void requireEmailAvailableFor(PersonRepository personRepository, String email, String ownerId) {
        if (email == null) {
            return;
        }
        personRepository.findByEmail(email)
                .filter(existing -> !existing.getIdentifier().equals(ownerId))
                .ifPresent(existing -> {
                    throw new DuplicateEmailException(email);
                });
    }
}
