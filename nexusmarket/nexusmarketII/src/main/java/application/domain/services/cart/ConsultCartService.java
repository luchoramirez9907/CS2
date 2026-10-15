package application.domain.services.cart;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.ShoppingCart;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.ShoppingCartRepository;
import application.domain.services.ServiceValidations;

import java.util.Optional;

/**
 * ConsultCartService
 *
 * Catalog service "Consult Cart". Retrieves the active
 * shopping cart of a buyer, if any. A buyer only consults its own cart.
 */
public class ConsultCartService {

    private final BuyerRepository buyerRepository;
    private final ShoppingCartRepository cartRepository;

    public ConsultCartService(BuyerRepository buyerRepository, ShoppingCartRepository cartRepository) {
        if (buyerRepository == null || cartRepository == null) {
            throw new IllegalArgumentException("ConsultCartService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * @return the active cart, or empty when the buyer has no active cart
     */
    public Optional<ShoppingCart> consultCart(String buyerId) {
        ServiceValidations.requireText(buyerId, "buyer id");
        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        buyer.requireActive();
        return cartRepository.findActiveByBuyerId(buyer.getIdentifier());
    }
}
