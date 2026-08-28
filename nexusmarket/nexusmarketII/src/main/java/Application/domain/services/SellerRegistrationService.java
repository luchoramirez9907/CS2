package Application.domain.services;

import Application.domain.exceptions.DuplicateEmailException;
import Application.domain.exceptions.SellerSelfRegistrationNotAllowedException;
import Application.domain.models.Administrator;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.ports.in.RegisterSellerUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.SellerRepository;

/**
 * SellerRegistrationService
 *
 * Implements the RegisterSellerUseCase. Sellers cannot self-register:
 * they are incorporated into the platform by an Administrator. The seller
 * identifier and email must be unique across the platform.
 */
public class SellerRegistrationService implements RegisterSellerUseCase {

    private final PersonRepository personRepository;
    private final SellerRepository sellerRepository;
    private final NotificationService notificationService;

    public SellerRegistrationService(PersonRepository personRepository,
                                     SellerRepository sellerRepository,
                                     NotificationService notificationService) {
        if (personRepository == null || sellerRepository == null || notificationService == null) {
            throw new IllegalArgumentException("SellerRegistrationService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.sellerRepository = sellerRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Seller registerSeller(String administratorId, String identifier, String fullName, String email) {
        requireText(administratorId, "administrator id");
        requireText(identifier, "seller identifier");
        requireText(fullName, "seller full name");
        requireText(email, "seller email");

        Person performer = personRepository.findById(administratorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrator '" + administratorId + "' does not exist"));
        performer.requireActive();
        if (performer.getRole() == Application.domain.valueobjects.SystemRole.SELLER) {
            throw new SellerSelfRegistrationNotAllowedException(email);
        }
        performer.requireRole("register a Seller", Application.domain.valueobjects.SystemRole.ADMINISTRATOR);
        Administrator administrator = (Administrator) performer;

        if (personRepository.existsByIdentifier(identifier)) {
            throw new DuplicateEmailException(identifier);
        }
        if (personRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Seller seller = new Seller(identifier, fullName, email, administrator,
                Application.domain.valueobjects.UserStatus.ACTIVE);
        sellerRepository.save(seller);
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
