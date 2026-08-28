package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for Order entities.
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {
}
