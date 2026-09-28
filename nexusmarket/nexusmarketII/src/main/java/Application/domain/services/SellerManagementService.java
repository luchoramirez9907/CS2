package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.ports.in.ManageSellerUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.valueobjects.UserStatus;

/**
 * SellerManagementService
 *
 * Implements ManageSellerUseCase. A seller consults and updates its own
 * information; Administrators and Supervisors consult any seller; only an
 * Administrator changes a seller's operational status. Registration is
 * handled by SellerRegistrationService.
 */
public class SellerManagementService implements ManageSellerUseCase {

    private final SellerRepository sellerRepository;
    private final PersonRepository personRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public SellerManagementService(SellerRepository sellerRepository,
                                   PersonRepository personRepository,
                                   AuthorizationService authorizationService,
                                   NotificationService notificationService) {
        if (sellerRepository == null || personRepository == null
                || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("SellerManagementService requires its dependencies");
        }
        this.sellerRepository = sellerRepository;
        this.personRepository = personRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Override
    public Seller consultSeller(String requesterId, String sellerId) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_SELLER);
        Seller seller = findSeller(sellerId);
        authorizationService.requireSellerAccess(requester, seller);
        return seller;
    }

    @Override
    public Seller updateSeller(String requesterId, String sellerId, String fullName, String email) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.UPDATE_SELLER);
        Seller seller = findSeller(sellerId);
        authorizationService.requireSellerAccess(requester, seller);

        ServiceValidations.requireEmailAvailableFor(personRepository, email, seller.getIdentifier());
        seller.updateContactInformation(fullName, email);
        sellerRepository.save(seller);
        notificationService.notify(seller, "Seller updated", "Your seller information was updated");
        return seller;
    }

    @Override
    public Seller changeSellerStatus(String requesterId, String sellerId, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Seller status must not be null");
        }
        authorizationService.requirePermission(requesterId, BusinessOperation.CHANGE_SELLER_STATUS);
        Seller seller = findSeller(sellerId);
        seller.changeStatus(status);
        sellerRepository.save(seller);
        notificationService.notify(seller, "Seller status changed",
                "Your seller account status is now " + status.getName());
        return seller;
    }

    private Seller findSeller(String sellerId) {
        ServiceValidations.requireText(sellerId, "seller id");
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller '" + sellerId + "' does not exist"));
    }
}
