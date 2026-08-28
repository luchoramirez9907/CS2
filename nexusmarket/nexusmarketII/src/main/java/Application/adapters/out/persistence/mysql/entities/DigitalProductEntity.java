package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for digital products delivered immediately after payment.
 */
@Entity
@Table(name = "digital_products")
@Getter
@Setter
@NoArgsConstructor
public class DigitalProductEntity extends ProductEntity {

    @Column(name = "digital_delivery_details", nullable = false, length = 500)
    private String digitalDeliveryDetails;
}
