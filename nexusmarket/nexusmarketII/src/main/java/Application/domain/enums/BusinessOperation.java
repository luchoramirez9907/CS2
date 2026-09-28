package Application.domain.enums;

/**
 * BusinessOperation
 *
 * Enumerates the business operations exposed by the service catalog,
 * used by the Authorization services to decide whether a user may
 * perform them according to its role (RG-02 / RG-03).
 */
public enum BusinessOperation {
    REGISTER_USER("register a user"),
    CONSULT_USER("consult a user"),
    UPDATE_USER("update a user"),
    CHANGE_USER_STATUS("change the status of a user"),

    REGISTER_SELLER("register a seller"),
    CONSULT_SELLER("consult a seller"),
    UPDATE_SELLER("update a seller"),
    CHANGE_SELLER_STATUS("change the status of a seller"),

    REGISTER_BUYER("register a buyer"),
    CONSULT_BUYER("consult a buyer"),
    UPDATE_BUYER("update a buyer"),
    CHANGE_BUYER_COMMERCIAL_STATUS("change the commercial status of a buyer"),

    REGISTER_WAREHOUSE("register a warehouse"),
    CONSULT_WAREHOUSE("consult a warehouse"),
    UPDATE_WAREHOUSE("update a warehouse"),

    PUBLISH_PRODUCT("publish a product"),
    CONSULT_PRODUCT("consult a product"),
    UPDATE_PRODUCT("update a product"),
    CHANGE_PRODUCT_STATUS("change the status of a product"),

    REGISTER_INVENTORY_MOVEMENT("register an inventory movement"),
    CONSULT_INVENTORY("consult inventory"),

    MANAGE_CART("manage a shopping cart"),
    CONSULT_CART("consult a shopping cart"),

    CONFIRM_ORDER("confirm an order"),
    CONFIRM_ORDER_PAYMENT("confirm the payment of an order"),
    FINALIZE_ORDER("finalize an order"),
    CONSULT_ORDER("consult an order"),

    GENERATE_INVOICE("generate an invoice"),
    CONSULT_INVOICE("consult an invoice"),

    CREATE_SHIPMENT("create a shipment"),
    UPDATE_SHIPMENT_STATUS("update the status of a shipment"),
    CONSULT_SHIPMENT("consult a shipment"),

    REQUEST_RETURN("request a return"),
    RESOLVE_RETURN("resolve a return"),
    CONSULT_RETURN("consult a return"),

    PROCESS_REFUND("process a refund"),
    CONSULT_REFUND("consult a refund"),

    CONSULT_COMMERCIAL_REPORT("consult the commercial report"),
    CONSULT_SELLER_PERFORMANCE_REPORT("consult a seller performance report");

    private final String description;

    BusinessOperation(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
