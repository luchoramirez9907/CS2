package Application.domain.valueobjects;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * InventoryMovementType
 *
 * Represents the type of movement recorded against an inventory record.
 * Movements provide traceability of stock changes independently from the
 * current available and reserved quantities of the inventory record.
 *
 * Note: RESERVATION movements carry a signed quantity (+) when stock is
 * reserved and (-) when the reservation is released, so that every change
 * to an inventory record generates exactly one movement (see SDD rule).
 */
public final class InventoryMovementType extends DomainCatalog {

    public static final InventoryMovementType STOCK_IN =
            new InventoryMovementType("STOCK_IN", "Stock In",
                    "Incoming stock registered into a warehouse.");
    public static final InventoryMovementType RESERVATION =
            new InventoryMovementType("RESERVATION", "Reservation",
                    "Stock reserved as a result of an active shopping cart or order.");
    public static final InventoryMovementType SALE_EXIT =
            new InventoryMovementType("SALE_EXIT", "Sale Exit",
                    "Stock removed from inventory as a result of a completed sale.");
    public static final InventoryMovementType ADJUSTMENT =
            new InventoryMovementType("ADJUSTMENT", "Adjustment",
                    "Manual correction of the recorded stock quantity.");
    public static final InventoryMovementType RETURN =
            new InventoryMovementType("RETURN", "Return",
                    "Stock reinstated as a result of a product return.");

    private static final Map<String, InventoryMovementType> VALUES_BY_CODE;

    static {
        Map<String, InventoryMovementType> values = new LinkedHashMap<>();
        values.put(STOCK_IN.code(), STOCK_IN);
        values.put(RESERVATION.code(), RESERVATION);
        values.put(SALE_EXIT.code(), SALE_EXIT);
        values.put(ADJUSTMENT.code(), ADJUSTMENT);
        values.put(RETURN.code(), RETURN);
        VALUES_BY_CODE = Collections.unmodifiableMap(values);
    }

    private InventoryMovementType(String code, String name, String description) {
        super(code, name, description);
    }

    public static InventoryMovementType fromCode(String code) {
        InventoryMovementType type = VALUES_BY_CODE.get(code);
        if (type == null) {
            throw new IllegalArgumentException("Unknown InventoryMovementType code: " + code);
        }
        return type;
    }

    public static Collection<InventoryMovementType> values() {
        return VALUES_BY_CODE.values();
    }

    private String code() {
        return getCode();
    }
}
