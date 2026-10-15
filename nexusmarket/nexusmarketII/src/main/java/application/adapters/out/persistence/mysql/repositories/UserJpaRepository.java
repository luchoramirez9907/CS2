package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for User (access account) entities.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
}
