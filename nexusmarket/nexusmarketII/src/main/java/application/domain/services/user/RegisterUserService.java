package application.domain.services.user;

import application.domain.enums.BusinessOperation;
import application.domain.models.Administrator;
import application.domain.models.LogisticsOperator;
import application.domain.models.Person;
import application.domain.models.Supervisor;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.PersonRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authentication.UserAccountService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

/**
 * RegisterUserService
 *
 * Catalog service "Register User". Only an
 * Administrator registers internal participants (administrators,
 * supervisors and logistics operators) together with their access account.
 * Buyers and Sellers are registered through their dedicated services (a
 * Seller can never self-register).
 */
public class RegisterUserService {

    private final PersonRepository personRepository;
    private final ValidatePermissionsService permissionsService;
    private final NotificationService notificationService;
    private final UserAccountService userAccountService;

    public RegisterUserService(PersonRepository personRepository,
                               ValidatePermissionsService permissionsService,
                               NotificationService notificationService,
                               UserAccountService userAccountService) {
        if (personRepository == null || permissionsService == null || notificationService == null
                || userAccountService == null) {
            throw new IllegalArgumentException("RegisterUserService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.permissionsService = permissionsService;
        this.notificationService = notificationService;
        this.userAccountService = userAccountService;
    }

    /**
     * @param administratorId identifier of the Administrator performing the registration
     * @param identifier      unique platform identifier of the new user
     * @param fullName        full name of the new user
     * @param email           unique email of the new user
     * @param role            ADMINISTRATOR, SUPERVISOR or LOGISTICS_OPERATOR
     * @param password        initial password of the access account (password policy applies)
     * @return the registered Person (status ACTIVE)
     */
    public Person registerUser(String administratorId, String identifier, String fullName,
                               String email, SystemRole role, String password) {
        ServiceValidations.requireText(identifier, "user identifier");
        ServiceValidations.requireText(fullName, "user full name");
        ServiceValidations.requireText(email, "user email");
        if (role == null) {
            throw new IllegalArgumentException("User role must not be null (RG-02)");
        }
        UserAccountService.requireValidPassword(password);
        permissionsService.requirePermission(administratorId, BusinessOperation.REGISTER_USER);
        if (role == SystemRole.BUYER || role == SystemRole.SELLER) {
            throw new IllegalArgumentException("Role " + role.getCode()
                    + " must be registered through its dedicated use case (Register Buyer / Register Seller)");
        }
        ServiceValidations.requireUniqueIdentity(personRepository, identifier, email);

        Person user;
        if (role == SystemRole.ADMINISTRATOR) {
            user = new Administrator(identifier, fullName, email, UserStatus.ACTIVE);
        } else if (role == SystemRole.SUPERVISOR) {
            user = new Supervisor(identifier, fullName, email, UserStatus.ACTIVE);
        } else {
            user = new LogisticsOperator(identifier, fullName, email, UserStatus.ACTIVE);
        }
        personRepository.save(user);
        userAccountService.createAccount(user, password);
        notificationService.notify(user, "Welcome to NexusMarket",
                "Your account was registered with role " + role.getName());
        return user;
    }
}
