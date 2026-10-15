package application.domain.services.user;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Person;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.PersonRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

import java.util.Comparator;
import java.util.List;

/**
 * ManageUserService
 *
 * Catalog service "Manage User". Every participant may
 * consult and update its own identity information; Administrators manage
 * every participant and are the only ones who change the operational
 * status. Administrators and Supervisors may list the participants.
 */
public class ManageUserService {

    private final PersonRepository personRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public ManageUserService(PersonRepository personRepository,
                             ValidatePermissionsService permissionsService,
                             ValidateOwnershipAccessService ownershipService,
                             NotificationService notificationService) {
        if (personRepository == null || permissionsService == null || ownershipService == null
                || notificationService == null) {
            throw new IllegalArgumentException("ManageUserService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    public Person consultUser(String requesterId, String userId) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_USER);
        Person user = findUser(userId);
        ownershipService.requirePersonAccess(requester, user);
        return user;
    }

    public Person updateUser(String requesterId, String userId, String fullName, String email) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.UPDATE_USER);
        Person user = findUser(userId);
        ownershipService.requirePersonAccess(requester, user);

        ServiceValidations.requireEmailAvailableFor(personRepository, email, user.getIdentifier());
        user.updateContactInformation(fullName, email);
        personRepository.save(user);
        notificationService.notify(user, "Profile updated", "Your identity information was updated");
        return user;
    }

    public Person changeUserStatus(String requesterId, String userId, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("User status must not be null");
        }
        Person requester = permissionsService.requirePermission(requesterId,
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

    /**
     * Lists the participants the requester may access (all for
     * Administrators and Supervisors), sorted by identifier.
     *
     * @param role optional role filter
     */
    public List<Person> listUsers(String requesterId, SystemRole role) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_USER);
        return personRepository.findAll().stream()
                .filter(person -> role == null || person.getRole().equals(role))
                .filter(person -> ownershipService.canAccessPerson(requester, person))
                .sorted(Comparator.comparing(Person::getIdentifier))
                .toList();
    }

    private Person findUser(String userId) {
        ServiceValidations.requireText(userId, "user id");
        return personRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }
}
