package Application.domain.services;

import Application.domain.exceptions.DuplicateEmailException;
import Application.domain.exceptions.InvalidRoleAssignmentException;
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
 * Implements the User Management services (per SDD - Services):
 * - Register User: creates a new system participant and establishes the
 *   person's initial identity information, assigned role and operational
 *   status (only an Administrator may register other participants).
 * - Manage User: consults an existing participant and changes the
 *   person's operational status according to the requester's permissions.
 */
public class UserManagementService implements RegisterUserUseCase, ManageUserUseCase {

    private final PersonRepository personRepository;
    private final NotificationService notificationService;

    public UserManagementService(PersonRepository personRepository,
                                 NotificationService notificationService) {
        if (personRepository == null || notificationService == null) {
            throw new IllegalArgumentException("UserManagementService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Person registerUser(String performerId, String identifier, String fullName,
                               String email, String roleCode) {
        requireText(performerId, "performer id");
        requireText(identifier, "user identifier");
        requireText(fullName, "user full name");
        requireText(email, "user email");
        requireText(roleCode, "role code");

        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrator '" + performerId + "' does not exist"));
        performer.requireActive();
        performer.requireRole("register a user", SystemRole.ADMINISTRATOR);

        SystemRole requestedRole = SystemRole.fromCode(roleCode);
        if (requestedRole == SystemRole.BUYER || requestedRole == SystemRole.SELLER) {
            throw new IllegalArgumentException("Role '" + requestedRole.getCode()
                    + "' participants are registered through their own subdomain services");
        }

        requireUniqueIdentity(identifier, email);

        Person newUser = switch (requestedRole) {
            case ADMINISTRATOR -> new Administrator(identifier, fullName, email, UserStatus.ACTIVE);
            case LOGISTICS_OPERATOR -> new LogisticsOperator(identifier, fullName, email, UserStatus.ACTIVE);
            case SUPERVISOR -> new Supervisor(identifier, fullName, email, UserStatus.ACTIVE);
            default -> throw new IllegalArgumentException(
                    "Role '" + requestedRole.getCode() + "' cannot be registered");
        };
        personRepository.save(newUser);
        notificationService.notify(newUser, "Welcome to NexusMarket",
                "Your " + requestedRole.getName() + " account was registered by "
                        + performer.getFullName());
        return newUser;
    }

    @Override
    public Person consultUser(String requesterId, String userId) {
        requireText(requesterId, "requester id");
        requireText(userId, "user id");

        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();

        if (!requester.getIdentifier().equals(userId)
                && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException("consult a user", requester.getRole(),
                    "an ADMINISTRATOR, a SUPERVISOR or the user itself");
        }
        return personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User '" + userId + "' does not exist"));
    }

    @Override
    public Person changeUserStatus(String performerId, String userId, String action) {
        requireText(performerId, "performer id");
        requireText(userId, "user id");
        requireText(action, "status action");

        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrator '" + performerId + "' does not exist"));
        performer.requireActive();
        performer.requireRole("change a user status", SystemRole.ADMINISTRATOR);

        if (performer.getIdentifier().equals(userId)) {
            throw new IllegalStateException(
                    "An administrator cannot change its own operational status");
        }

        Person target = personRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User '" + userId + "' does not exist"));

        switch (action.toUpperCase()) {
            case "ACTIVE" -> target.activate();
            case "INACTIVE" -> target.deactivate();
            case "BLOCKED" -> target.block();
            default -> throw new IllegalArgumentException(
                    "Unknown user status action: " + action
                            + " (expected ACTIVE, INACTIVE or BLOCKED)");
        }
        personRepository.save(target);
        notificationService.notify(target, "Account status updated",
                "Your operational status is now " + target.getStatus().getCode());
        return target;
    }

    private void requireUniqueIdentity(String identifier, String email) {
        if (personRepository.existsByIdentifier(identifier)) {
            throw new DuplicateEmailException(identifier);
        }
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
