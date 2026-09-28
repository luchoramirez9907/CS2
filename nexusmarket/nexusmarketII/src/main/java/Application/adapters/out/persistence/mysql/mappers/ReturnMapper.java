package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.ReturnEntity;
import Application.domain.enums.RefundStatus;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.enums.ReturnStatus;

import java.util.function.Function;

/**
 * Mapper: Return domain models (and their Refund) to JPA entities and back.
 */
public final class ReturnMapper {

    private ReturnMapper() {
    }

    public static ReturnEntity toEntity(Return returnRequest) {
        ReturnEntity entity = new ReturnEntity();
        entity.setReturnId(returnRequest.getReturnId());
        entity.setOrderId(returnRequest.getOrder().getOrderId());
        entity.setBuyerId(returnRequest.getBuyer().getIdentifier());
        entity.setReason(returnRequest.getReason());
        entity.setRequestDate(returnRequest.getRequestDate());
        updateEntity(entity, returnRequest);
        return entity;
    }

    public static void updateEntity(ReturnEntity entity, Return returnRequest) {
        entity.setStatusCode(returnRequest.getReturnStatus().name());
        Refund refund = returnRequest.getRefund();
        if (refund != null) {
            entity.setRefundId(refund.getRefundId());
            entity.setRefundAmount(refund.getAmount());
            entity.setRefundDate(refund.getRefundDate());
            entity.setRefundApprovedById(refund.getApprovedBy().getIdentifier());
            entity.setRefundStatusCode(refund.getRefundStatus().name());
        }
    }

    /**
     * @param personResolver resolves the person who approved the refund
     */
    public static Return toDomain(ReturnEntity entity, Order order, Buyer buyer,
                                  Function<String, Person> personResolver) {
        Return returnRequest = Return.reconstruct(entity.getReturnId(), order, buyer, entity.getReason(),
                ReturnStatus.valueOf(entity.getStatusCode()), entity.getRequestDate());
        if (entity.getRefundId() != null) {
            Refund.reconstruct(entity.getRefundId(), returnRequest, entity.getRefundAmount(),
                    entity.getRefundDate(), personResolver.apply(entity.getRefundApprovedById()),
                    RefundStatus.valueOf(entity.getRefundStatusCode()));
        }
        return returnRequest;
    }
}
