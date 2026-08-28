package Application.adapters.in.rest.responses;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO: order information.
 */
public record OrderResponse(String orderId, String buyerId, String orderStatus,
                            String creationDate, BigDecimal totalAmount,
                            List<ItemResponse> items,
                            String invoiceId, BigDecimal taxAmount,
                            boolean hasShipment) {

    public record ItemResponse(String productId, String productName, int quantity,
                               BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
