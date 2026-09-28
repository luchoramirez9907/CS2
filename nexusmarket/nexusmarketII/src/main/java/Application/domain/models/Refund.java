package Application.domain.models;

import Application.domain.enums.RefundStatus;
import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.exceptions.RefundNotAllowedException;
import Application.domain.valueobjects.SystemRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Refund
 *
 * Represents the monetary reimbursement resulting from an approved return.
 *
 * Business rule: a Refund can only be generated from an approved Return,
 * and is approved by an Administrator or a Supervisor.
 */
public class Refund {

    private final String refundId;
    private final Return relatedReturn;
    private final BigDecimal amount;
    private final LocalDateTime refundDate;
    private final Person approvedBy;
    private RefundStatus refundStatus;

    public Refund(String refundId, Return relatedReturn, BigDecimal amount,
                  LocalDateTime refundDate, Person approvedBy) {
        if (refundId == null || refundId.isBlank()) {
            throw new IllegalArgumentException("Refund id must not be null or blank");
        }
        if (relatedReturn == null) {
            throw new IllegalArgumentException("A Refund originates from exactly one Return");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        if (refundDate == null) {
            throw new IllegalArgumentException("Refund date must not be null");
        }
        if (approvedBy == null) {
            throw new IllegalArgumentException("A Refund is approved by one Person");
        }
        if (approvedBy.getRole() != SystemRole.ADMINISTRATOR
                && approvedBy.getRole() != SystemRole.SUPERVISOR) {
            throw new InvalidRoleAssignmentException("approve a Refund", approvedBy.getRole(),
                    "an ADMINISTRATOR or a SUPERVISOR");
        }
        this.refundId = refundId;
        this.relatedReturn = relatedReturn;
        this.amount = amount;
        this.refundDate = refundDate;
        this.approvedBy = approvedBy;
        this.refundStatus = RefundStatus.PENDING;
    }

    /**
     * Creates and processes the refund of an approved return.
     *
     * @throws RefundNotAllowedException when the return is not approved
     */
    public static Refund issueFor(Return relatedReturn, BigDecimal amount, Person approvedBy,
                                  String refundId, LocalDateTime refundDate) {
        if (relatedReturn.getReturnStatus() != Application.domain.enums.ReturnStatus.APPROVED
                && relatedReturn.getReturnStatus() != Application.domain.enums.ReturnStatus.COMPLETED) {
            throw new RefundNotAllowedException(relatedReturn.getReturnId(),
                    "refunds can only be generated from approved returns (current status: "
                            + relatedReturn.getReturnStatus() + ")");
        }
        Refund refund = new Refund(refundId, relatedReturn, amount, refundDate, approvedBy);
        relatedReturn.attachRefund(refund);
        return refund;
    }

    public String getRefundId() { return refundId; }

    public Return getRelatedReturn() { return relatedReturn; }

    public BigDecimal getAmount() { return amount; }

    public LocalDateTime getRefundDate() { return refundDate; }

    public Person getApprovedBy() { return approvedBy; }

    public RefundStatus getRefundStatus() { return refundStatus; }

    /** Marks the refund as processed (money reimbursed to the buyer). */
    public void process() {
        requireStatus(RefundStatus.PENDING, "process");
        this.refundStatus = RefundStatus.PROCESSED;
    }

    /** Rejects the refund. */
    public void reject() {
        requireStatus(RefundStatus.PENDING, "reject");
        this.refundStatus = RefundStatus.REJECTED;
    }

    /**
     * Rebuilds a persisted refund and links it to its return without
     * re-applying issuing validations. Used exclusively by persistence
     * mappers.
     */
    public static Refund reconstruct(String refundId, Return relatedReturn, BigDecimal amount,
                                     LocalDateTime refundDate, Person approvedBy,
                                     RefundStatus refundStatus) {
        Refund refund = new Refund(refundId, relatedReturn, amount, refundDate, approvedBy);
        refund.refundStatus = refundStatus;
        relatedReturn.attachRefund(refund);
        return refund;
    }

    private void requireStatus(RefundStatus expected, String operation) {
        if (refundStatus != expected) {
            throw new IllegalArgumentException("Cannot " + operation + " refund '" + refundId
                    + "': status is " + refundStatus + " (expected " + expected + ")");
        }
    }
}
