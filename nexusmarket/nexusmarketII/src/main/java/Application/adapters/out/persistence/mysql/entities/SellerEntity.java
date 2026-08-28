package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Seller specializations of Person.
 */
@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
public class SellerEntity extends PersonEntity {

    @Column(name = "registered_by_id", nullable = false, length = 64)
    private String registeredById;
}
