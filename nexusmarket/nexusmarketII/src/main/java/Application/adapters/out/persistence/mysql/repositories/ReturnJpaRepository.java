package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for post-sale Return entities.
 */
public interface ReturnJpaRepository extends JpaRepository<ReturnEntity, String> {
}
