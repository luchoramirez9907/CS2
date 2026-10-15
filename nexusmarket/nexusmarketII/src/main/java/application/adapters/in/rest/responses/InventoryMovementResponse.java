package application.adapters.in.rest.responses;

/**
 * Response DTO: one entry of the movement history of an inventory record.
 */
public record InventoryMovementResponse(Integer movementNumber, String movementType, int quantity,
                                        String movementDate, String performedById) {
}
