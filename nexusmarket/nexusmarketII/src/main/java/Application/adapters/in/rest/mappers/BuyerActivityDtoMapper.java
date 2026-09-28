package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.BuyerActivityResponse;
import Application.adapters.in.rest.responses.RefundResponse;
import Application.adapters.in.rest.responses.ReturnResponse;
import Application.domain.ports.in.ManageBuyerUseCase;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper: Buyer activity (use case output) to Response DTO.
 */
public final class BuyerActivityDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private BuyerActivityDtoMapper() {
    }

    public static BuyerActivityResponse toResponse(ManageBuyerUseCase.BuyerActivity activity) {
        List<ReturnResponse> returns = new ArrayList<>();
        List<RefundResponse> refunds = new ArrayList<>();
        for (Application.domain.models.Return returnRequest : activity.returns()) {
            returns.add(new ReturnResponse(
                    returnRequest.getReturnId(),
                    returnRequest.getOrder().getOrderId(),
                    returnRequest.getBuyer().getIdentifier(),
                    returnRequest.getReason(),
                    returnRequest.getReturnStatus().name(),
                    returnRequest.getRequestDate().format(ISO)));
            if (returnRequest.getRefund() != null) {
                Application.domain.models.Refund refund = returnRequest.getRefund();
                refunds.add(new RefundResponse(
                        refund.getRefundId(),
                        returnRequest.getReturnId(),
                        refund.getAmount(),
                        refund.getRefundDate().format(ISO),
                        refund.getRefundStatus().name()));
            }
        }
        return new BuyerActivityResponse(
                BuyerDtoMapper.toResponse(activity.buyer()),
                activity.orders().stream().map(OrderDtoMapper::toResponse).toList(),
                returns,
                refunds);
    }
}
