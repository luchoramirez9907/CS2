package Application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO: shopping cart information.
 */
public record CartResponse(String cartId, String buyerId, List<ItemResponse> items,
                           BigDecimal total) {

    public record ItemResponse(String productId, String productName, int quantity,
                               BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
