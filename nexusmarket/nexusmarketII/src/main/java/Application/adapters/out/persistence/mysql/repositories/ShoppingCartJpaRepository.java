package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.ShoppingCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for active ShoppingCart entities.
 */
public interface ShoppingCartJpaRepository extends JpaRepository<ShoppingCartEntity, String> {

    Optional<ShoppingCartEntity> findByBuyerId(String buyerId);
}
