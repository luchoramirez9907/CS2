package application.adapters.useCases;

import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.services.authentication.AuthenticateUserService;
import application.domain.services.catalog.ManageProductService;
import application.domain.ports.in.PublicAccessPort;
import application.domain.services.buyer.RegisterBuyerService;
import application.domain.valueobjects.Address;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case Adapter implementing PublicAccessPort (onboarding and
 * authentication). Delegates to the Domain Services; contains no business rules.
 */
@Service
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final RegisterBuyerService registerBuyerService;
    private final ManageProductService manageProductService;
    private final AuthenticateUserService authenticateUserService;

    public PublicAccessUseCaseImpl(RegisterBuyerService registerBuyerService,
                                   ManageProductService manageProductService,
                                   AuthenticateUserService authenticateUserService) {
        this.registerBuyerService = registerBuyerService;
        this.manageProductService = manageProductService;
        this.authenticateUserService = authenticateUserService;
    }

    @Override
    public Buyer registerBuyer(String identifier, String fullName, String email, Address primaryAddress,
                               String password) {
        // No performer: buyer self-registration.
        return registerBuyerService.registerBuyer(null, identifier, fullName, email, primaryAddress, password);
    }

    @Override
    public Product consultCatalogProduct(String productId) {
        // Anonymous access: only published products are visible.
        return manageProductService.consultProduct(null, productId);
    }

    @Override
    public AuthenticationResult login(String email, String password) {
        return authenticateUserService.login(email, password);
    }

    @Override
    public void logout(User user) {
        authenticateUserService.logout(user);
    }

    @Override
    public void changeMyPassword(User user, String currentPassword, String newPassword) {
        authenticateUserService.changePassword(user, currentPassword, newPassword);
    }

    @Override
    public List<Product> searchCatalog(String text, String sellerId) {
        return manageProductService.searchCatalog(text, sellerId);
    }
}
