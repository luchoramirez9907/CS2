package application.adapters.in.rest.mappers;

import application.domain.enums.ReturnStatus;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.SystemRole;

/**
 * Mapper: optional query-string filters to domain Value Objects and enums.
 * A missing or blank filter means "no filter" (null); an unknown code is
 * rejected with IllegalArgumentException (HTTP 400).
 */
public final class FilterParamMapper {

    private FilterParamMapper() {
    }

    public static OrderStatus orderStatus(String code) {
        return isBlank(code) ? null : OrderStatus.fromCode(code);
    }

    public static ReturnStatus returnStatus(String code) {
        return isBlank(code) ? null : ReturnStatus.valueOf(code);
    }

    public static SystemRole role(String code) {
        return isBlank(code) ? null : SystemRole.fromCode(code);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
