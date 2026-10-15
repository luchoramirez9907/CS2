package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.UserEntity;
import application.domain.models.Person;
import application.domain.models.User;

/**
 * Mapper: User domain models to JPA entities and back.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setPersonId(user.getIdentifier());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setAuthTokenVersion(user.getAuthTokenVersion());
        return entity;
    }

    /**
     * @param person the Person owning the account, already loaded
     */
    public static User toDomain(UserEntity entity, Person person) {
        return User.reconstruct(person, entity.getPasswordHash(), entity.getAuthTokenVersion());
    }
}
