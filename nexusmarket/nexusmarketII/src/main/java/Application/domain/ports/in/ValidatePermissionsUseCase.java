package Application.domain.ports.in;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Person;

/**
 * Input port (use case): determines whether a user may perform a business
 * operation based on its role and status, so that no participant operates
 * outside the boundaries of its assigned role (RG-02 / RG-03).
 */
public interface ValidatePermissionsUseCase {

    boolean hasPermission(Person user, BusinessOperation operation);

    /**
     * Loads the user, and ensures it is active and its role allows the operation.
     *
     * @return the authorized user
     */
    Person requirePermission(String userId, BusinessOperation operation);
}
