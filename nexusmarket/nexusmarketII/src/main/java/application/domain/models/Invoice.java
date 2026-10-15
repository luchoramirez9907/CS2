package application.domain.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Invoice
 *
 * Represents the commercial billing information associated with a
 * confirmed order. An Invoice belongs to exactly one Order and is
 * generated as a direct consequence of it.
 */
public class Invoice {

    private final String invoiceId;
    private final Order order;
    private final LocalDateTime issueDate;
    private final BigDecimal totalAmount;
    private final BigDecimal taxAmount;

    public Invoice(String invoiceId, Order order, LocalDateTime issueDate,
                   BigDecimal totalAmount, BigDecimal taxAmount) {
        if (invoiceId == null || invoiceId.isBlank()) {
            throw new IllegalArgumentException("Invoice id must not be null or blank");
        }
        if (order == null) {
            throw new IllegalArgumentException("An Invoice belongs to exactly one Order");
        }
        if (issueDate == null) {
            throw new IllegalArgumentException("Issue date must not be null");
        }
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Total amount must not be null or negative");
        }
        if (taxAmount == null || taxAmount.signum() < 0) {
            throw new IllegalArgumentException("Tax amount must not be null or negative");
        }
        this.invoiceId = invoiceId;
        this.order = order;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
    }

    /**
     * Issues an invoice for a confirmed order using the given tax rate
     * (e.g. 0.19 for 19%). Catalog prices already include taxes, so the
     * invoiced total equals the order total and the tax amount is the tax
     * contained in it: total - total / (1 + taxRate).
     */
    public static Invoice issueFor(Order order, String invoiceId, LocalDateTime issueDate,
                                   BigDecimal taxRate) {
        if (taxRate == null || taxRate.signum() < 0) {
            throw new IllegalArgumentException("Tax rate must not be null or negative");
        }
        BigDecimal total = order.getTotalAmount();
        BigDecimal netAmount = total.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP);
        BigDecimal tax = total.subtract(netAmount);
        return new Invoice(invoiceId, order, issueDate, total, tax);
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Order getOrder() {
        return order;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
}
