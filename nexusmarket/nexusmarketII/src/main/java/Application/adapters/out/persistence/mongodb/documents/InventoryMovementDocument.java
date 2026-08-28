package Application.adapters.out.persistence.mongodb.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for the inventory movement history (high-volume
 * operational data, per SDD - MongoDB Adapter).
 */
@Document(collection = "inventory_movements")
public class InventoryMovementDocument {

    @Id
    private String id;

    private String inventoryId;

    private Integer movementNumber;

    private String productId;

    private String movementTypeCode;

    private int quantity;

    private LocalDateTime movementDate;

    private String performedById;

    public InventoryMovementDocument() {
    }

    public InventoryMovementDocument(String id, String inventoryId, Integer movementNumber,
                                     String productId, String movementTypeCode, int quantity,
                                     LocalDateTime movementDate, String performedById) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.movementNumber = movementNumber;
        this.productId = productId;
        this.movementTypeCode = movementTypeCode;
        this.quantity = quantity;
        this.movementDate = movementDate;
        this.performedById = performedById;
    }

    public String getId() {
        return id;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public Integer getMovementNumber() {
        return movementNumber;
    }

    public String getProductId() {
        return productId;
    }

    public String getMovementTypeCode() {
        return movementTypeCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public String getPerformedById() {
        return performedById;
    }
}
