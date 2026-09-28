package Application.domain.ports.in;

import Application.domain.enums.BusinessOperation;

/**
 * Input port (use case): Validate Permissions.
 *
 * Determines whether a user has permission to perform a specific
 * business operation based on the user's role and status, ensuring no
 * participant operates outside the boundaries of its assigned role.
 */
public interface ValidatePermissionsUseCase {

    boolean canPerform(String userId, BusinessOperation operation);

    /** @throws Application.domain.exceptions.InvalidRoleAssignmentException when not allowed */
    void requirePermission(String userId, BusinessOperation operation);
}
