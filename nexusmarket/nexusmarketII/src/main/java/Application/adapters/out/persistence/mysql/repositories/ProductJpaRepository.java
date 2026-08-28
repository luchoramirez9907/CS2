package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for Product entities.
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
}
