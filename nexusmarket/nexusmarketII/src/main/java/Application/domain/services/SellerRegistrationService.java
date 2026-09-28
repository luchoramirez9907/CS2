package Application.domain.services;

import Application.domain.exceptions.DuplicateEmailException;
import Application.domain.exceptions.SellerSelfRegistrationNotAllowedException;
import Application.domain.models.Administrator;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.ports.in.ManageSellerUseCase;
import Application.domain.ports.in.RegisterSellerUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

/**
 * SellerRegistrationService
 *
 * Implements the Seller Management services (per SDD - Services):
 * - Register Seller: sellers cannot self-register; they are incorporated
 *   into the platform by an Administrator. Identifier and email must be
 *   unique across the platform.
 * - Manage Seller: consults an existing seller and changes its
 *   operational status according to the requester's permissions.
 */
public class SellerRegistrationService implements RegisterSellerUseCase, ManageSellerUseCase {

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

    @Override
    public Seller consultSeller(String requesterId, String sellerId) {
        requireText(requesterId, "requester id");
        requireText(sellerId, "seller id");

        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();
        if (!requester.getIdentifier().equals(sellerId)
                && requester.getRole() != SystemRole.ADMINISTRATOR
                && requester.getRole() != SystemRole.SUPERVISOR) {
            throw new Application.domain.exceptions.InvalidRoleAssignmentException(
                    "consult seller information", requester.getRole(),
                    "an ADMINISTRATOR, a SUPERVISOR or the seller itself");
        }
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller '" + sellerId + "' does not exist"));
    }

    @Override
    public Seller changeSellerStatus(String performerId, String sellerId, String action) {
        requireText(performerId, "performer id");
        requireText(sellerId, "seller id");
        requireText(action, "status action");

        Person performer = personRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrator '" + performerId + "' does not exist"));
        performer.requireActive();
        performer.requireRole("change a seller status", SystemRole.ADMINISTRATOR);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller '" + sellerId + "' does not exist"));

        switch (action.toUpperCase()) {
            case "ACTIVE" -> seller.activate();
            case "INACTIVE" -> seller.deactivate();
            case "BLOCKED" -> seller.block();
            default -> throw new IllegalArgumentException(
                    "Unknown seller status action: " + action
                            + " (expected ACTIVE, INACTIVE or BLOCKED)");
        }
        sellerRepository.save(seller);
        notificationService.notify(seller, "Account status updated",
                "Your operational status is now " + seller.getStatus().getCode());
        return seller;
    }
}
