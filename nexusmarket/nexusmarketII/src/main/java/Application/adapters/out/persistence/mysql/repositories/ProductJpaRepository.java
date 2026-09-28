package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for Product entities.
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findBySellerId(String sellerId);
}
