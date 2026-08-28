package Application.domain.valueobjects;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SystemRole
 *
 * Represents the responsibilities and permissions assigned to a participant
 * within the NexusMarket system (RG-02: each person holds exactly one role).
 */
public final class SystemRole extends DomainCatalog {

    public static final SystemRole BUYER =
            new SystemRole("BUYER", "Buyer",
                    "Participant who acquires products published in the catalog.");
    public static final SystemRole SELLER =
            new SystemRole("SELLER", "Seller",
                    "Participant responsible for registering and managing products.");
    public static final SystemRole LOGISTICS_OPERATOR =
            new SystemRole("LOGISTICS_OPERATOR", "Logistics Operator",
                    "Participant responsible for the physical operation of warehouses and dispatches.");
    public static final SystemRole ADMINISTRATOR =
            new SystemRole("ADMINISTRATOR", "Administrator",
                    "Participant responsible for administering sellers and warehouses.");
    public static final SystemRole SUPERVISOR =
            new SystemRole("SUPERVISOR", "Supervisor",
                    "Participant with read-only access for operational monitoring.");

    private static final Map<String, SystemRole> VALUES_BY_CODE;

    static {
        Map<String, SystemRole> values = new LinkedHashMap<>();
        values.put(BUYER.code(), BUYER);
        values.put(SELLER.code(), SELLER);
        values.put(LOGISTICS_OPERATOR.code(), LOGISTICS_OPERATOR);
        values.put(ADMINISTRATOR.code(), ADMINISTRATOR);
        values.put(SUPERVISOR.code(), SUPERVISOR);
        VALUES_BY_CODE = Collections.unmodifiableMap(values);
    }

    private SystemRole(String code, String name, String description) {
        super(code, name, description);
    }

    public static SystemRole fromCode(String code) {
        SystemRole role = VALUES_BY_CODE.get(code);
        if (role == null) {
            throw new IllegalArgumentException("Unknown SystemRole code: " + code);
        }
        return role;
    }

    public static Collection<SystemRole> values() {
        return VALUES_BY_CODE.values();
    }

    private String code() {
        return getCode();
    }
}
