package application.adapters.out.persistence.mongodb.mappers;

import application.adapters.out.persistence.mongodb.documents.InventoryMovementDocument;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Person;
import application.domain.valueobjects.InventoryMovementType;

import java.util.UUID;

/**
 * Mapper: InventoryMovement domain models to MongoDB documents and back.
 * The movement history is append-only: documents are never updated.
 */
public final class InventoryMovementDocumentMapper {

    private InventoryMovementDocumentMapper() {
    }

    public static InventoryMovementDocument toDocument(InventoryMovement movement) {
        return new InventoryMovementDocument(
                UUID.randomUUID().toString(),
                movement.getInventory().getIdentifier(),
                movement.getMovementId(),
                movement.getInventory().getProduct().getIdentifier(),
                movement.getMovementType().getCode(),
                movement.getQuantity(),
                movement.getMovementDate(),
                movement.getPerformedBy().getIdentifier());
    }

    /**
     * @param inventory   the inventory record the movement belongs to, already loaded
     * @param performedBy the person who triggered the movement, already loaded
     */
    public static InventoryMovement toDomain(InventoryMovementDocument document, Inventory inventory,
                                             Person performedBy) {
        return new InventoryMovement(document.getMovementNumber(),
                InventoryMovementType.fromCode(document.getMovementTypeCode()), document.getQuantity(),
                document.getMovementDate(), inventory, performedBy);
    }
}
