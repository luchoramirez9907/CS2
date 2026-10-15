package application.domain.services.seller;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.SellerRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.UserStatus;

/**
 * ManageSellerService
 *
 * Catalog service "Manage Seller". A seller consults
 * and updates its own information; Administrators and Supervisors consult any seller; only an
 * Administrator changes a seller's operational status. Registration is
 * handled by RegisterSellerService.
 */
public class ManageSellerService {

    private final SellerRepository sellerRepository;
    private final PersonRepository personRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public ManageSellerService(SellerRepository sellerRepository,
                               PersonRepository personRepository,
                               ValidatePermissionsService permissionsService,
                               ValidateOwnershipAccessService ownershipService,
                               NotificationService notificationService) {
        if (sellerRepository == null || personRepository == null
                || permissionsService == null || ownershipService == null || notificationService == null) {
            throw new IllegalArgumentException("ManageSellerService requires its dependencies");
        }
        this.sellerRepository = sellerRepository;
        this.personRepository = personRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    public Seller consultSeller(String requesterId, String sellerId) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_SELLER);
        Seller seller = findSeller(sellerId);
        ownershipService.requireSellerAccess(requester, seller);
        return seller;
    }

    public Seller updateSeller(String requesterId, String sellerId, String fullName, String email) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.UPDATE_SELLER);
        Seller seller = findSeller(sellerId);
        ownershipService.requireSellerAccess(requester, seller);

        ServiceValidations.requireEmailAvailableFor(personRepository, email, seller.getIdentifier());
        seller.updateContactInformation(fullName, email);
        sellerRepository.save(seller);
        notificationService.notify(seller, "Seller updated", "Your seller information was updated");
        return seller;
    }

    public Seller changeSellerStatus(String requesterId, String sellerId, UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Seller status must not be null");
        }
        permissionsService.requirePermission(requesterId, BusinessOperation.CHANGE_SELLER_STATUS);
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
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
    }
}
