package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.entities.CartItemEntity;
import Application.adapters.out.persistence.mysql.entities.ShoppingCartEntity;
import Application.adapters.out.persistence.mysql.mappers.CartMapper;
import Application.adapters.out.persistence.mysql.repositories.ShoppingCartJpaRepository;
import Application.domain.models.Buyer;
import Application.domain.models.CartItem;
import Application.domain.models.ShoppingCart;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.ShoppingCartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the ShoppingCartRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlShoppingCartRepositoryAdapter implements ShoppingCartRepository {

    private final ShoppingCartJpaRepository jpaRepository;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;

    public MysqlShoppingCartRepositoryAdapter(ShoppingCartJpaRepository jpaRepository,
                                              BuyerRepository buyerRepository,
                                              ProductRepository productRepository) {
        this.jpaRepository = jpaRepository;
        this.buyerRepository = buyerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void save(ShoppingCart cart) {
        ShoppingCartEntity existing = jpaRepository.findById(cart.getCartId()).orElse(null);
        if (existing == null) {
            jpaRepository.save(CartMapper.toEntity(cart));
            return;
        }
        existing.getItems().clear();
        for (CartItem item : cart.getItems()) {
            CartItemEntity itemEntity = new CartItemEntity();
            itemEntity.setProductId(item.getProduct().getIdentifier());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice());
            itemEntity.setCart(existing);
            existing.getItems().add(itemEntity);
        }
        jpaRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findById(String cartId) {
        return jpaRepository.findById(cartId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShoppingCart> findActiveByBuyerId(String buyerId) {
        return jpaRepository.findByBuyerId(buyerId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void delete(ShoppingCart cart) {
        jpaRepository.findById(cart.getCartId()).ifPresent(jpaRepository::delete);
    }

    private ShoppingCart toDomain(ShoppingCartEntity entity) {
        Buyer buyer = buyerRepository.findById(entity.getBuyerId())
                .orElseThrow(() -> new IllegalStateException("Buyer '" + entity.getBuyerId()
                        + "' referenced by cart does not exist"));
        return CartMapper.toDomain(entity, buyer,
                productId -> productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalStateException("Product '" + productId
                                + "' referenced by cart does not exist")));
    }
}
