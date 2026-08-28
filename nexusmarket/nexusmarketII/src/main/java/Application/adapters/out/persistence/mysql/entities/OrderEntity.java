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
 * JPA entity for Orders, including their invoice data.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "order_id", length = 64)
    private String orderId;

    @Column(name = "buyer_id", nullable = false, length = 64)
    private String buyerId;

    @Column(name = "order_status_code", nullable = false, length = 32)
    private String orderStatusCode;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "invoice_id", length = 64)
    private String invoiceId;

    @Column(name = "invoice_issue_date")
    private LocalDateTime invoiceIssueDate;

    @Column(name = "invoice_total_amount", precision = 12, scale = 2)
    private BigDecimal invoiceTotalAmount;

    @Column(name = "invoice_tax_amount", precision = 12, scale = 2)
    private BigDecimal invoiceTaxAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();
}
