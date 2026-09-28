package Application.domain.ports.in;

import Application.domain.models.Person;
import Application.domain.valueobjects.UserStatus;

/**
 * Input port (use case): consults, updates and changes the operational
 * status of an existing system participant, according to the access
 * permissions of the requesting user.
 */
public interface ManageUserUseCase {

    Person consultUser(String requesterId, String userId);

    Person updateUser(String requesterId, String userId, String fullName, String email);

    Person changeUserStatus(String requesterId, String userId, UserStatus status);
}
