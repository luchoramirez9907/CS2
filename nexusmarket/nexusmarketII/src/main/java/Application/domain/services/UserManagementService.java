package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Administrator;
import Application.domain.models.LogisticsOperator;
import Application.domain.models.Person;
import Application.domain.models.Supervisor;
import Application.domain.ports.in.ManageUserUseCase;
import Application.domain.ports.in.RegisterUserUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

/**
 * UserManagementService
 *
 * Implements RegisterUserUseCase and ManageUserUseCase. Only an
 * Administrator registers users or changes their operational status;
 * every participant may consult and update its own identity information.
 * Buyers and Sellers are registered through their dedicated use cases
 * (a Seller can never self-register).
 */
public class UserManagementService implements RegisterUserUseCase, ManageUserUseCase {

    private final PersonRepository personRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public UserManagementService(PersonRepository personRepository,
                                 AuthorizationService authorizationService,
                                 NotificationService notificationService) {
        if (personRepository == null || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("UserManagementService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Override
    public Person registerUser(String administratorId, String identifier, String fullName,
                               String email, SystemRole role) {
        ServiceValidations.requireText(identifier, "user identifier");
        ServiceValidations.requireText(fullName, "user full name");
        ServiceValidations.requireText(email, "user email");
        if (role == null) {
            throw new IllegalArgumentException("User role must not be null (RG-02)");
        }
        authorizationService.requirePermission(administratorId, BusinessOperation.REGISTER_USER);
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
        notificationService.notify(user, "Welcome to NexusMarket",
                "Your account was registered with role " + role.getName());
        return user;
    }

    @Override
    public Person consultUser(String requesterId, String userId) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_USER);
        Person user = findUser(userId);
        authorizationService.requirePersonAccess(requester, user);
        return user;
    }

    @Override
    public Person updateUser(String requesterId, String userId, String fullName, String email) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.UPDATE_USER);
        Person user = findUser(userId);
        authorizationService.requirePersonAccess(requester, user);

        ServiceValidations.requireEmailAvailableFor(personRepository, email, user.getIdentifier());
        user.updateContactInformation(fullName, email);
        personRepository.save(user);
        notificationService.notify(user, "Profile updated", "Your identity information was updated");
        return user;
    }

    @Override
    public Person changeUserStatus(String requesterId, String userId, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("User status must not be null");
        }
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.CHANGE_USER_STATUS);
        Person user = findUser(userId);
        if (requester.getIdentifier().equals(user.getIdentifier())) {
            throw new IllegalArgumentException("An administrator cannot change its own status");
        }
        user.changeStatus(status);
        personRepository.save(user);
        notificationService.notify(user, "Account status changed",
                "Your account status is now " + status.getName());
        return user;
    }

    private Person findUser(String userId) {
        ServiceValidations.requireText(userId, "user id");
        return personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User '" + userId + "' does not exist"));
    }
}
