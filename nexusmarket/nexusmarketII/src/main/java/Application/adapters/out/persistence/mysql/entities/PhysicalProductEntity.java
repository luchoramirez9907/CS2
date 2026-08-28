package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for tangible products requiring inventory and dispatch.
 */
@Entity
@Table(name = "physical_products")
@Getter
@Setter
@NoArgsConstructor
public class PhysicalProductEntity extends ProductEntity {
}
