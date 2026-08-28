package Application.domain.valueobjects;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BuyerCommercialStatus
 *
 * Represents the commercial condition of a buyer for performing purchases
 * within the Marketplace. Independent from {@link UserStatus}: it reflects
 * the buyer's ability to transact rather than their general system access.
 */
public final class BuyerCommercialStatus extends DomainCatalog {

    public static final BuyerCommercialStatus ACTIVE =
            new BuyerCommercialStatus("ACTIVE", "Active",
                    "Buyer is authorized to place new orders.");
    public static final BuyerCommercialStatus RESTRICTED =
            new BuyerCommercialStatus("RESTRICTED", "Restricted",
                    "Buyer has limitations on purchasing activity.");
    public static final BuyerCommercialStatus BLOCKED =
            new BuyerCommercialStatus("BLOCKED", "Blocked",
                    "Buyer is not authorized to place new orders.");

    private static final Map<String, BuyerCommercialStatus> VALUES_BY_CODE;

    static {
        Map<String, BuyerCommercialStatus> values = new LinkedHashMap<>();
        values.put(ACTIVE.code(), ACTIVE);
        values.put(RESTRICTED.code(), RESTRICTED);
        values.put(BLOCKED.code(), BLOCKED);
        VALUES_BY_CODE = Collections.unmodifiableMap(values);
    }

    private BuyerCommercialStatus(String code, String name, String description) {
        super(code, name, description);
    }

    public static BuyerCommercialStatus fromCode(String code) {
        BuyerCommercialStatus status = VALUES_BY_CODE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown BuyerCommercialStatus code: " + code);
        }
        return status;
    }

    public static Collection<BuyerCommercialStatus> values() {
        return VALUES_BY_CODE.values();
    }

    private String code() {
        return getCode();
    }
}
