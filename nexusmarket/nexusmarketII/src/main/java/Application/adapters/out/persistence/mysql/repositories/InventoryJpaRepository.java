package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for Inventory entities.
 */
public interface InventoryJpaRepository extends JpaRepository<InventoryEntity, String> {

    List<InventoryEntity> findByProductId(String productId);
}
