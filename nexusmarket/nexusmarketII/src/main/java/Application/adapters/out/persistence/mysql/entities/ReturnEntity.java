package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity for post-sale Return requests.
 */
@Entity
@Table(name = "returns")
@Getter
@Setter
@NoArgsConstructor
public class ReturnEntity {

    @Id
    @Column(name = "return_id", length = 64)
    private String returnId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "buyer_id", nullable = false, length = 64)
    private String buyerId;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "status_code", nullable = false, length = 32)
    private String statusCode;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;
}
