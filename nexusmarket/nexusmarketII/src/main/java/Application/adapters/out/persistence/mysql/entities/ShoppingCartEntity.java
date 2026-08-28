package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for active ShoppingCarts.
 */
@Entity
@Table(name = "shopping_carts")
@Getter
@Setter
@NoArgsConstructor
public class ShoppingCartEntity {

    @Id
    @Column(name = "cart_id", length = 64)
    private String cartId;

    @Column(name = "buyer_id", nullable = false, length = 64)
    private String buyerId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();
}
