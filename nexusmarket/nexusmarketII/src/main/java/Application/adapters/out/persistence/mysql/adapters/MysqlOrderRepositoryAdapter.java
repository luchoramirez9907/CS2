package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.entities.OrderEntity;
import Application.adapters.out.persistence.mysql.mappers.OrderMapper;
import Application.adapters.out.persistence.mysql.repositories.OrderJpaRepository;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MySQL persistence adapter for the OrderRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlOrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final BuyerRepository buyerRepository;
    private final ProductRepository productRepository;

    public MysqlOrderRepositoryAdapter(OrderJpaRepository jpaRepository,
                                       BuyerRepository buyerRepository,
                                       ProductRepository productRepository) {
        this.jpaRepository = jpaRepository;
        this.buyerRepository = buyerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void save(Order order) {
        jpaRepository.findById(order.getOrderId()).ifPresentOrElse(
                existing -> {
                    OrderMapper.updateEntity(existing, order);
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(OrderMapper.toEntity(order)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(String orderId) {
        return jpaRepository.findById(orderId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByBuyerId(String buyerId) {
        return jpaRepository.findByBuyerId(buyerId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Order toDomain(OrderEntity entity) {
        Buyer buyer = buyerRepository.findById(entity.getBuyerId())
                .orElseThrow(() -> new IllegalStateException("Buyer '" + entity.getBuyerId()
                        + "' referenced by order does not exist"));
        return OrderMapper.toDomain(entity, buyer,
                productId -> productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalStateException("Product '" + productId
                                + "' referenced by order does not exist")));
    }
}
