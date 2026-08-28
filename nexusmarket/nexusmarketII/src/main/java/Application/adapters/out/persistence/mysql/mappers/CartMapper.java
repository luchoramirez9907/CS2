package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.CartItemEntity;
import Application.adapters.out.persistence.mysql.entities.ShoppingCartEntity;
import Application.domain.models.Buyer;
import Application.domain.models.CartItem;
import Application.domain.models.Product;
import Application.domain.models.ShoppingCart;

import java.util.function.Function;

/**
 * Mapper: ShoppingCart domain models to JPA entities and back.
 */
public final class CartMapper {

    private CartMapper() {
    }

    public static ShoppingCartEntity toEntity(ShoppingCart cart) {
        ShoppingCartEntity entity = new ShoppingCartEntity();
        entity.setCartId(cart.getCartId());
        entity.setBuyerId(cart.getBuyer().getIdentifier());
        entity.setCreatedDate(cart.getCreatedDate());
        for (CartItem item : cart.getItems()) {
            CartItemEntity itemEntity = new CartItemEntity();
            itemEntity.setProductId(item.getProduct().getIdentifier());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitPrice(item.getUnitPrice());
            itemEntity.setCart(entity);
            entity.getItems().add(itemEntity);
        }
        return entity;
    }

    public static ShoppingCart toDomain(ShoppingCartEntity entity, Buyer buyer,
                                        Function<String, Product> productResolver) {
        ShoppingCart cart = new ShoppingCart(entity.getCartId(), buyer, entity.getCreatedDate());
        for (CartItemEntity itemEntity : entity.getItems()) {
            Product product = productResolver.apply(itemEntity.getProductId());
            cart.addItem(product, itemEntity.getQuantity(), itemEntity.getUnitPrice());
        }
        buyer.attachCart(cart);
        return cart;
    }
}
