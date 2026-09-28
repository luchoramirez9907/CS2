package Application.domain.ports.in;

import Application.domain.models.Person;
import Application.domain.valueobjects.SystemRole;

/**
 * Input port (use case): creates a new system participant with its
 * initial identity information, assigned role and operational status.
 * Buyers and Sellers are registered through their own use cases.
 */
public interface RegisterUserUseCase {

    /**
     * @param administratorId identifier of the Administrator performing the registration
     * @param identifier      unique platform identifier of the new user
     * @param fullName        full name of the new user
     * @param email           unique email of the new user
     * @param role            ADMINISTRATOR, SUPERVISOR or LOGISTICS_OPERATOR
     * @return the registered Person (status ACTIVE)
     */
    Person registerUser(String administratorId, String identifier, String fullName,
                        String email, SystemRole role);
}
