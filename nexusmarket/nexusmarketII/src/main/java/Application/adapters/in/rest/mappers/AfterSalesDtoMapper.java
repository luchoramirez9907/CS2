package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.RefundResponse;
import Application.adapters.in.rest.responses.ReturnResponse;
import Application.domain.models.Refund;
import Application.domain.models.Return;

import java.time.format.DateTimeFormatter;

/**
 * Mapper: Domain Models (Return, Refund) to Response DTOs.
 */
public final class AfterSalesDtoMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private AfterSalesDtoMapper() {
    }

    public static ReturnResponse toResponse(Return returnRequest) {
        return new ReturnResponse(
                returnRequest.getReturnId(),
                returnRequest.getOrder().getOrderId(),
                returnRequest.getBuyer().getIdentifier(),
                returnRequest.getReason(),
                returnRequest.getReturnStatus().name(),
                returnRequest.getRequestDate().format(ISO));
    }

    public static RefundResponse toResponse(Refund refund) {
        return new RefundResponse(
                refund.getRefundId(),
                refund.getRelatedReturn().getReturnId(),
                refund.getAmount(),
                refund.getRefundStatus().name(),
                refund.getApprovedBy().getIdentifier());
    }
}
