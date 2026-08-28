package Application.domain.models;

import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

/**
 * Supervisor
 *
 * Represents a read-only participant responsible for operational
 * consultation and monitoring. A supervisor does not create or modify
 * business entities.
 */
public class Supervisor extends Person {

    public Supervisor(String identifier, String fullName, String email, UserStatus status) {
        super(identifier, fullName, email, SystemRole.SUPERVISOR, status);
    }
}
