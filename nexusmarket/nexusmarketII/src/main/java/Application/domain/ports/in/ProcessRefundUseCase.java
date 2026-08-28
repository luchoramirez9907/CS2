package Application.domain.ports.in;

import Application.domain.models.Refund;

import java.math.BigDecimal;

/**
 * Input port (use case): processes the refund of an approved return.
 */
public interface ProcessRefundUseCase {

    /**
     * @param approverId identifier of the Administrator or Supervisor approving the refund
     * @param returnId   identifier of the approved Return
     * @param amount     amount to reimburse (when null, the order total is used)
     * @return the processed Refund
     */
    Refund processRefund(String approverId, String returnId, BigDecimal amount);
}
