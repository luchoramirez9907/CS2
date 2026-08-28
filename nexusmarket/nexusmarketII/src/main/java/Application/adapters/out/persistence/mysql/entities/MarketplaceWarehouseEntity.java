package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Marketplace-owned warehouses.
 */
@Entity
@Table(name = "marketplace_warehouses")
@Getter
@Setter
@NoArgsConstructor
public class MarketplaceWarehouseEntity extends WarehouseEntity {

    @Column(name = "managed_by_id", nullable = false, length = 64)
    private String managedById;
}
