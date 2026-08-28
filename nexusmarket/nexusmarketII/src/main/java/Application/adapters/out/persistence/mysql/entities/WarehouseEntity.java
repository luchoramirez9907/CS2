package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Warehouses (joined inheritance: marketplace/seller are
 * genuine specializations).
 */
@Entity
@Table(name = "warehouses")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class WarehouseEntity {

    @Id
    @Column(name = "identifier", length = 64)
    private String identifier;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Embedded
    private AddressEmbeddable address;
}
