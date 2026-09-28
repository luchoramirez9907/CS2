package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
 * JPA entity for Orders, including their invoice and shipment data.
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

    @Column(name = "shipment_id", length = 64)
    private String shipmentId;

    @Column(name = "shipment_operator_id", length = 64)
    private String shipmentOperatorId;

    @Column(name = "shipment_origin_warehouse_id", length = 64)
    private String shipmentOriginWarehouseId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "shipping_street", length = 200)),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city", length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state", length = 100)),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country", length = 100)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", length = 20))
    })
    private AddressEmbeddable shippingAddress;

    @Column(name = "shipment_status_code", length = 32)
    private String shipmentStatusCode;

    @Column(name = "shipment_dispatch_date")
    private LocalDateTime shipmentDispatchDate;

    @Column(name = "shipment_delivery_date")
    private LocalDateTime shipmentDeliveryDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();
}
