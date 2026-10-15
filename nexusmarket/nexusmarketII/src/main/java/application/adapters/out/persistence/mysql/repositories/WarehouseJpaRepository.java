package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for Warehouse entities.
 */
public interface WarehouseJpaRepository extends JpaRepository<WarehouseEntity, String> {
}
