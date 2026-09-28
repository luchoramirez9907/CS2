package Application.domain.ports.in;

import Application.domain.models.Person;

/**
 * Input port (use case): Manage User.
 *
 * Consults an existing system participant and changes the person's
 * operational status, according to the access permissions of the
 * requesting user.
 */
public interface ManageUserUseCase {

    /**
     * @param requesterId identifier of the user performing the consultation
     * @param userId      identifier of the user being consulted
     * @return the consulted Person
     */
    Person consultUser(String requesterId, String userId);

    /**
     * @param performerId identifier of the Administrator performing the change
     * @param userId      identifier of the user whose status changes
     * @param action      one of ACTIVE, INACTIVE or BLOCKED
     * @return the updated Person
     */
    Person changeUserStatus(String performerId, String userId, String action);
}
