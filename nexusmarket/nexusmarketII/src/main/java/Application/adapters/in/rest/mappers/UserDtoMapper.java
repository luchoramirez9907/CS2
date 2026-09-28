package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.UserResponse;
import Application.domain.models.Person;

/**
 * Mapper: Domain Model (Person) to Response DTO.
 */
public final class UserDtoMapper {

    private UserDtoMapper() {
    }

    public static UserResponse toResponse(Person person) {
        return new UserResponse(
                person.getIdentifier(),
                person.getFullName(),
                person.getEmail(),
                person.getRole().getCode(),
                person.getStatus().getCode());
    }
}
