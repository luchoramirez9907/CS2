package Application.domain.ports.in;

import Application.domain.models.Person;

/**
 * Input port (use case): Register User.
 *
 * Creates a new system participant (Administrator, LogisticsOperator or
 * Supervisor) and establishes the person's initial identity information,
 * assigned role and operational status. Seller and Buyer creation are
 * handled by their own subdomain services.
 */
public interface RegisterUserUseCase {

    /**
     * @param performerId identifier of the Administrator performing the registration
     * @param identifier  unique platform identifier for the new user
     * @param fullName    official full name of the new user
     * @param email       unique email of the new user
     * @param roleCode    role to assign (ADMINISTRATOR, LOGISTICS_OPERATOR or SUPERVISOR)
     * @return the registered Person
     */
    Person registerUser(String performerId, String identifier, String fullName, String email, String roleCode);
}
