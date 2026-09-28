package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Person;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.ports.in.ProcessRefundUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.ReturnRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefundProcessingService
 *
 * Implements the ProcessRefundUseCase. A Refund can only be generated
 * from an approved Return and must be approved by an Administrator or a
 * Supervisor. By default the reimbursed amount is the total of the order,
 * and it can never exceed it.
 */
public class RefundProcessingService implements ProcessRefundUseCase {

    private final ReturnRepository returnRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public RefundProcessingService(ReturnRepository returnRepository,
                                   AuthorizationService authorizationService,
                                   NotificationService notificationService) {
        if (returnRepository == null || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("RefundProcessingService requires its dependencies");
        }
        this.returnRepository = returnRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Override
    public Refund processRefund(String approverId, String returnId, BigDecimal amount) {
        Person approver = authorizationService.requirePermission(approverId, BusinessOperation.PROCESS_REFUND);
        Return returnRequest = findReturn(returnId);

        BigDecimal orderTotal = returnRequest.getOrder().getTotalAmount();
        BigDecimal refundAmount = amount == null ? orderTotal : amount;
        if (refundAmount.compareTo(orderTotal) > 0) {
            throw new IllegalArgumentException("Refund amount " + refundAmount
                    + " exceeds the order total " + orderTotal);
        }

        Refund refund = Refund.issueFor(returnRequest, refundAmount, approver,
                UUID.randomUUID().toString(), LocalDateTime.now());
        refund.process();

        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Refund processed",
                "A refund of " + refundAmount + " was processed for return " + returnId);
        return refund;
    }

    @Override
    public Refund consultRefund(String requesterId, String returnId) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_REFUND);
        Return returnRequest = findReturn(returnId);
        authorizationService.requireBuyerAccess(requester, returnRequest.getBuyer());
        if (returnRequest.getRefund() == null) {
            throw new IllegalArgumentException("Return '" + returnId + "' has no refund");
        }
        return returnRequest.getRefund();
    }

    private Return findReturn(String returnId) {
        ServiceValidations.requireText(returnId, "return id");
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return '" + returnId + "' does not exist"));
    }
}
