package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.ReturnEntity;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Return;
import Application.domain.enums.ReturnStatus;

/**
 * Mapper: Return domain models to JPA entities and back.
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
        entity.setStatusCode(returnRequest.getReturnStatus().name());
        entity.setRequestDate(returnRequest.getRequestDate());
        return entity;
    }

    public static void updateEntity(ReturnEntity entity, Return returnRequest) {
        entity.setStatusCode(returnRequest.getReturnStatus().name());
    }

    public static Return toDomain(ReturnEntity entity, Order order, Buyer buyer) {
        return Return.reconstruct(entity.getReturnId(), order, buyer, entity.getReason(),
                ReturnStatus.valueOf(entity.getStatusCode()), entity.getRequestDate());
    }
}
