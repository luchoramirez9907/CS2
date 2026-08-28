package Application.adapters.out.persistence.mysql.repositories;

import Application.adapters.out.persistence.mysql.entities.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for Person entities.
 */
public interface PersonJpaRepository extends JpaRepository<PersonEntity, String> {

    boolean existsByEmail(String email);

    Optional<PersonEntity> findByEmail(String email);
}
