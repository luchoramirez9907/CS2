package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Inventory records (quantities only; the movement
 * history is stored in MongoDB).
 */
@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
public class InventoryEntity {

    @Id
    @Column(name = "identifier", length = 64)
    private String identifier;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "warehouse_id", nullable = false, length = 64)
    private String warehouseId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;
}
