package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.LoginResponse;
import application.domain.models.AuthenticationResult;

/**
 * Mapper: authentication domain results to Response DTOs.
 */
public final class AuthDtoMapper {

    private static final String BEARER = "Bearer";

    private AuthDtoMapper() {
    }

    public static LoginResponse toResponse(AuthenticationResult result) {
        return new LoginResponse(result.accessToken(), BEARER, result.expiresInSeconds(),
                PersonDtoMapper.toResponse(result.user().getPerson()));
    }
}
