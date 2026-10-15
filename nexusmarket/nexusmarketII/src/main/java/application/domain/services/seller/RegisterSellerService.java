package application.domain.services.seller;

import application.domain.exceptions.DuplicateEmailException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.SellerSelfRegistrationNotAllowedException;
import application.domain.models.Administrator;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.SellerRepository;
import application.domain.services.authentication.UserAccountService;

/**
 * RegisterSellerService
 *
 * Catalog service "Register Seller". Sellers cannot
 * self-register: they are incorporated into the platform by an Administrator. The seller
 * identifier and email must be unique across the platform.
 */
public class RegisterSellerService {

    private final PersonRepository personRepository;
    private final SellerRepository sellerRepository;
    private final NotificationService notificationService;
    private final UserAccountService userAccountService;

    public RegisterSellerService(PersonRepository personRepository,
                                 SellerRepository sellerRepository,
                                 NotificationService notificationService,
                                 UserAccountService userAccountService) {
        if (personRepository == null || sellerRepository == null || notificationService == null
                || userAccountService == null) {
            throw new IllegalArgumentException("RegisterSellerService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.sellerRepository = sellerRepository;
        this.notificationService = notificationService;
        this.userAccountService = userAccountService;
    }

    /**
     * @param administratorId identifier of the Administrator performing the registration
     * @param identifier      unique platform identifier for the new seller
     * @param fullName        official full name of the seller
     * @param email           unique email of the seller
     * @param password        initial password of the access account (password policy applies)
     * @return the registered Seller
     */
    public Seller registerSeller(String administratorId, String identifier, String fullName, String email,
                                 String password) {
        requireText(administratorId, "administrator id");
        requireText(identifier, "seller identifier");
        requireText(fullName, "seller full name");
        requireText(email, "seller email");
        UserAccountService.requireValidPassword(password);

        Person performer = personRepository.findById(administratorId)
                .orElseThrow(() -> new EntityNotFoundException("Administrator", administratorId));
        performer.requireActive();
        if (performer.getRole() == application.domain.valueobjects.SystemRole.SELLER) {
            throw new SellerSelfRegistrationNotAllowedException(email);
        }
        performer.requireRole("register a Seller", application.domain.valueobjects.SystemRole.ADMINISTRATOR);
        Administrator administrator = (Administrator) performer;

        if (personRepository.existsByIdentifier(identifier)) {
            throw new DuplicateEmailException(identifier);
        }
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Seller seller = new Seller(identifier, fullName, email, administrator,
                application.domain.valueobjects.UserStatus.ACTIVE);
        sellerRepository.save(seller);
        userAccountService.createAccount(seller, password);
        notificationService.notify(seller, "Welcome to NexusMarket",
                "Your seller account was registered by administrator " + administrator.getFullName());
        return seller;
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
