package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.mappers.ReturnMapper;
import Application.adapters.out.persistence.mysql.repositories.ReturnJpaRepository;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Return;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.ReturnRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the ReturnRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlReturnRepositoryAdapter implements ReturnRepository {

    private final ReturnJpaRepository jpaRepository;
    private final OrderRepository orderRepository;
    private final BuyerRepository buyerRepository;

    public MysqlReturnRepositoryAdapter(ReturnJpaRepository jpaRepository,
                                        OrderRepository orderRepository,
                                        BuyerRepository buyerRepository) {
        this.jpaRepository = jpaRepository;
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
    }

    @Override
    @Transactional
    public void save(Return returnRequest) {
        jpaRepository.findById(returnRequest.getReturnId()).ifPresentOrElse(
                existing -> {
                    ReturnMapper.updateEntity(existing, returnRequest);
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(ReturnMapper.toEntity(returnRequest)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Return> findById(String returnId) {
        return jpaRepository.findById(returnId).map(entity -> {
            Order order = orderRepository.findById(entity.getOrderId())
                    .orElseThrow(() -> new IllegalStateException("Order '" + entity.getOrderId()
                            + "' referenced by return does not exist"));
            Buyer buyer = buyerRepository.findById(entity.getBuyerId())
                    .orElseThrow(() -> new IllegalStateException("Buyer '" + entity.getBuyerId()
                            + "' referenced by return does not exist"));
            return ReturnMapper.toDomain(entity, order, buyer);
        });
    }
}
