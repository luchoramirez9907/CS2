package Application.domain.exceptions;

import Application.domain.valueobjects.SystemRole;

/**
 * Thrown when an operation is attempted by a person whose role does not
 * authorize it. No participant may manage information outside the scope
 * of its role (RG-03).
 */
public class InvalidRoleAssignmentException extends DomainException {

    private final SystemRole actualRole;
    private final SystemRole requiredRole;
    private final String operation;

    public InvalidRoleAssignmentException(String operation, SystemRole actualRole, SystemRole requiredRole) {
        super("Person with role '" + actualRole.getCode()
                + "' is not authorized to perform '" + operation + "'; required role: '"
                + requiredRole.getCode() + "'");
        this.actualRole = actualRole;
        this.requiredRole = requiredRole;
        this.operation = operation;
    }

    public InvalidRoleAssignmentException(String operation, SystemRole actualRole, String requiredRolesDescription) {
        super("Person with role '" + actualRole.getCode()
                + "' is not authorized to perform '" + operation + "'; required: "
                + requiredRolesDescription);
        this.actualRole = actualRole;
        this.requiredRole = null;
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public SystemRole getActualRole() {
        return actualRole;
    }

    public SystemRole getRequiredRole() {
        return requiredRole;
    }
}
