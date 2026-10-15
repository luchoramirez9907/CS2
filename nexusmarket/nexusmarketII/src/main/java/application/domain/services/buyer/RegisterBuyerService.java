package application.domain.services.buyer;

import application.domain.enums.BusinessOperation;
import application.domain.models.Buyer;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.PersonRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authentication.UserAccountService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.UserStatus;

/**
 * RegisterBuyerService
 *
 * Catalog service "Register Buyer". A buyer may
 * register itself or be registered by an Administrator, and enters the
 * platform with an ACTIVE commercial status and its access account.
 */
public class RegisterBuyerService {

    private final BuyerRepository buyerRepository;
    private final PersonRepository personRepository;
    private final ValidatePermissionsService permissionsService;
    private final NotificationService notificationService;
    private final UserAccountService userAccountService;

    public RegisterBuyerService(BuyerRepository buyerRepository,
                                PersonRepository personRepository,
                                ValidatePermissionsService permissionsService,
                                NotificationService notificationService,
                                UserAccountService userAccountService) {
        if (buyerRepository == null || personRepository == null || permissionsService == null
                || notificationService == null || userAccountService == null) {
            throw new IllegalArgumentException("RegisterBuyerService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.personRepository = personRepository;
        this.permissionsService = permissionsService;
        this.notificationService = notificationService;
        this.userAccountService = userAccountService;
    }

    /**
     * @param performerId    identifier of the Administrator registering the buyer;
     *                       null or blank when the buyer registers itself
     * @param identifier     unique platform identifier of the buyer
     * @param fullName       full name of the buyer
     * @param email          unique email of the buyer
     * @param primaryAddress primary delivery address
     * @param password       initial password of the access account (password policy applies)
     * @return the registered Buyer
     */
    public Buyer registerBuyer(String performerId, String identifier, String fullName, String email,
                               Address primaryAddress, String password) {
        ServiceValidations.requireText(identifier, "buyer identifier");
        ServiceValidations.requireText(fullName, "buyer full name");
        ServiceValidations.requireText(email, "buyer email");
        if (primaryAddress == null) {
            throw new IllegalArgumentException("A buyer requires a primary delivery address");
        }
        UserAccountService.requireValidPassword(password);
        if (performerId != null && !performerId.isBlank()) {
            permissionsService.requirePermission(performerId, BusinessOperation.REGISTER_BUYER);
        }
        ServiceValidations.requireUniqueIdentity(personRepository, identifier, email);

        Buyer buyer = new Buyer(identifier, fullName, email, primaryAddress,
                BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        buyerRepository.save(buyer);
        userAccountService.createAccount(buyer, password);
        notificationService.notify(buyer, "Welcome to NexusMarket",
                "Your buyer account is ready to place orders");
        return buyer;
    }
}
