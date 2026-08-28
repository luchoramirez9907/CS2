package Application.domain.valueobjects;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserStatus
 *
 * Represents the current operational status of a person's participation
 * in the NexusMarket system.
 */
public final class UserStatus extends DomainCatalog {

    public static final UserStatus ACTIVE =
            new UserStatus("ACTIVE", "Active",
                    "Person can access and operate within the system normally.");
    public static final UserStatus INACTIVE =
            new UserStatus("INACTIVE", "Inactive",
                    "Person exists but is not currently active in the system.");
    public static final UserStatus BLOCKED =
            new UserStatus("BLOCKED", "Blocked",
                    "Person's access to the system has been suspended.");

    private static final Map<String, UserStatus> VALUES_BY_CODE;

    static {
        Map<String, UserStatus> values = new LinkedHashMap<>();
        values.put(ACTIVE.code(), ACTIVE);
        values.put(INACTIVE.code(), INACTIVE);
        values.put(BLOCKED.code(), BLOCKED);
        VALUES_BY_CODE = Collections.unmodifiableMap(values);
    }

    private UserStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static UserStatus fromCode(String code) {
        UserStatus status = VALUES_BY_CODE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown UserStatus code: " + code);
        }
        return status;
    }

    public static Collection<UserStatus> values() {
        return VALUES_BY_CODE.values();
    }

    private String code() {
        return getCode();
    }
}
