package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.CartResponse;
import application.domain.models.CartItem;
import application.domain.models.ShoppingCart;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper: Domain Model (ShoppingCart) to Response DTO.
 */
public final class CartDtoMapper {

    private CartDtoMapper() {
    }

    public static CartResponse toResponse(ShoppingCart cart) {
        List<CartResponse.ItemResponse> items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            items.add(new CartResponse.ItemResponse(
                    item.getProduct().getIdentifier(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }
        return new CartResponse(cart.getCartId(), cart.getBuyer().getIdentifier(),
                items, cart.total());
    }
}
