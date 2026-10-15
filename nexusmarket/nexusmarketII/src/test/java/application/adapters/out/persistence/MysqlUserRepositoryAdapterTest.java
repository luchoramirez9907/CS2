package application.adapters.out.persistence;

import application.adapters.out.persistence.mysql.adapters.MysqlUserRepositoryAdapter;
import application.adapters.out.persistence.mysql.entities.UserEntity;
import application.adapters.out.persistence.mysql.repositories.UserJpaRepository;
import application.domain.models.Administrator;
import application.domain.models.User;
import application.domain.ports.out.PersonRepository;
import application.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests of the MySQL adapter of the UserRepository output port, with
 * the Spring Data repository and the PersonRepository port mocked.
 */
class MysqlUserRepositoryAdapterTest {

    private final UserJpaRepository jpaRepository = mock(UserJpaRepository.class);
    private final PersonRepository personRepository = mock(PersonRepository.class);
    private final MysqlUserRepositoryAdapter adapter = new MysqlUserRepositoryAdapter(jpaRepository, personRepository);
    private final Administrator admin = new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE);

    @Test
    void saveMapsTheDomainModelToAnEntity() {
        adapter.save(new User(admin, "hashed:x"));
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertEquals("admin-1", captor.getValue().getPersonId());
        assertEquals("hashed:x", captor.getValue().getPasswordHash());
    }

    @Test
    void findByEmailResolvesThePersonThroughItsOutputPort() {
        UserEntity entity = new UserEntity();
        entity.setPersonId("admin-1");
        entity.setPasswordHash("hashed:x");
        entity.setAuthTokenVersion(2);
        when(personRepository.findByEmail("admin@x.com")).thenReturn(Optional.of(admin));
        when(jpaRepository.findById("admin-1")).thenReturn(Optional.of(entity));

        User user = adapter.findByEmail("admin@x.com").orElseThrow();
        assertEquals(admin, user.getPerson());
        assertEquals(2, user.getAuthTokenVersion());
    }

    @Test
    void unknownAccountsAreEmpty() {
        when(personRepository.findByEmail("nobody@x.com")).thenReturn(Optional.empty());
        when(jpaRepository.findById("ghost")).thenReturn(Optional.empty());
        assertTrue(adapter.findByEmail("nobody@x.com").isEmpty());
        assertTrue(adapter.findByPersonId("ghost").isEmpty());
    }
}
