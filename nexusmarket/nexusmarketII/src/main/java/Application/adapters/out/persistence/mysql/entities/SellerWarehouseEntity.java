package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Seller-owned warehouses.
 */
@Entity
@Table(name = "seller_warehouses")
@Getter
@Setter
@NoArgsConstructor
public class SellerWarehouseEntity extends WarehouseEntity {

    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;
}
