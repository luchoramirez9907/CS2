package Application.domain.services;

import Application.domain.models.Person;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.ports.in.ProcessRefundUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ReturnRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefundProcessingService
 *
 * Implements the ProcessRefundUseCase. A Refund can only be generated
 * from an approved Return and must be approved by an Administrator or a
 * Supervisor. By default the reimbursed amount is the total of the order.
 */
public class RefundProcessingService implements ProcessRefundUseCase {

    private final ReturnRepository returnRepository;
    private final PersonRepository personRepository;
    private final NotificationService notificationService;

    public RefundProcessingService(ReturnRepository returnRepository,
                                   PersonRepository personRepository,
                                   NotificationService notificationService) {
        if (returnRepository == null || personRepository == null || notificationService == null) {
            throw new IllegalArgumentException("RefundProcessingService requires its dependencies");
        }
        this.returnRepository = returnRepository;
        this.personRepository = personRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Refund processRefund(String approverId, String returnId, BigDecimal amount) {
        if (approverId == null || approverId.isBlank()) {
            throw new IllegalArgumentException("Approver id must not be null or blank");
        }
        if (returnId == null || returnId.isBlank()) {
            throw new IllegalArgumentException("Return id must not be null or blank");
        }

        Person approver = personRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person '" + approverId + "' does not exist"));
        approver.requireActive();

        Return returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return '" + returnId + "' does not exist"));

        BigDecimal refundAmount = amount == null
                ? returnRequest.getOrder().getTotalAmount()
                : amount;

        Refund refund = Refund.issueFor(returnRequest, refundAmount, approver,
                UUID.randomUUID().toString(), LocalDateTime.now());
        refund.process();

        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Refund processed",
                "A refund of " + refundAmount + " was processed for return " + returnId);
        return refund;
    }
}
