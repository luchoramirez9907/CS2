package Application.domain.models;

import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

import java.util.Objects;

/**
 * Person (Abstract)
 *
 * Represents any identifiable participant authorized to interact with the
 * NexusMarket system. Centralizes the common identity and contact
 * information shared by all participants: Buyer, Seller, Logistics
 * Operator, Administrator and Supervisor.
 *
 * Business rules:
 * - Each person has exactly one role within the system (RG-02).
 * - identifier and email must be unique across the platform.
 * - No participant may manage information outside the scope of its role (RG-03).
 */
public abstract class Person {

    private final String identifier;
    private final String fullName;
    private final String email;
    private final SystemRole role;
    private UserStatus status;

    protected Person(String identifier, String fullName, String email, SystemRole role, UserStatus status) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Person identifier must not be null or blank");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Person full name must not be null or blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Person email must be a valid address");
        }
        if (role == null) {
            throw new IllegalArgumentException("Person role must not be null (RG-02)");
        }
        this.identifier = identifier;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status == null ? UserStatus.ACTIVE : status;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public SystemRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    /**
     * Ensures this person holds the expected role before executing an
     * operation. Enforces RG-02 / RG-03 at the domain level.
     */
    public void requireRole(String operation, SystemRole expectedRole) {
        if (this.role != expectedRole) {
            throw new InvalidRoleAssignmentException(operation, this.role, expectedRole);
        }
    }

    /**
     * Ensures this person is operational (active) before executing an operation.
     */
    public void requireActive() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Person '" + identifier + "' is not active (status: "
                    + status.getCode() + ")");
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
        Person person = (Person) o;
        return identifier.equals(person.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{identifier='" + identifier + "', fullName='" + fullName
                + "', role=" + role.getCode() + ", status=" + status.getCode() + "}";
    }
}
