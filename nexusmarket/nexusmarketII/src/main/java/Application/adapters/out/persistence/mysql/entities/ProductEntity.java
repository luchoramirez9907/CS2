package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for Products of the catalog (joined inheritance:
 * physical/digital are genuine specializations, not a type column).
 */
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class ProductEntity {

    @Id
    @Column(name = "identifier", length = 64)
    private String identifier;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "status_code", nullable = false, length = 32)
    private String statusCode;

    @Column(name = "seller_id", nullable = false, length = 64)
    private String sellerId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();
}
