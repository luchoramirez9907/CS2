package application.domain.ports.in;

import application.domain.models.AuthenticationResult;
import application.domain.models.Buyer;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.valueobjects.Address;

import java.util.List;

/**
 * Role Input Port: onboarding and authentication operations shared by every
 * participant. registerBuyer, consultCatalogProduct and login require no
 * authentication; logout and changeMyPassword act on the authenticated User
 * (any role).
 */
public interface PublicAccessPort {

    /**
     * Buyer self-registration: creates the buyer and its access account.
     */
    Buyer registerBuyer(String identifier, String fullName, String email, Address primaryAddress,
                        String password);

    /**
     * Consults a product of the public catalog (published products only).
     */
    Product consultCatalogProduct(String productId);

    /**
     * Authenticates a participant and issues its access token.
     */
    AuthenticationResult login(String email, String password);

    /**
     * Invalidates every access token previously issued to the user.
     */
    void logout(User user);

    /**
     * Changes the password of the authenticated user and invalidates its
     * previous access tokens.
     */
    void changeMyPassword(User user, String currentPassword, String newPassword);

    /**
     * Searches the public catalog (published products only).
     */
    List<Product> searchCatalog(String text, String sellerId);
}
