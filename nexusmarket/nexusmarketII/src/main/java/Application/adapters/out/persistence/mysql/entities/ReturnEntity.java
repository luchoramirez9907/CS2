package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for post-sale Return requests, including the refund each
 * one may have generated.
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

    @Column(name = "refund_id", length = 64)
    private String refundId;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "refund_approved_by_id", length = 64)
    private String refundApprovedById;

    @Column(name = "refund_status_code", length = 32)
    private String refundStatusCode;
}
