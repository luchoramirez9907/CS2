package Application.adapters.in.rest.responses;

import java.util.List;

/**
 * Response DTO: consolidated activity of a buyer (orders, returns and
 * their refunds).
 */
public record BuyerActivityResponse(BuyerResponse buyer,
                                    List<OrderResponse> orders,
                                    List<ReturnResponse> returns,
                                    List<RefundResponse> refunds) {
}
