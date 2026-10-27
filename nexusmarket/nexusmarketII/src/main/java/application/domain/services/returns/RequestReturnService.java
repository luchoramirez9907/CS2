package application.domain.services.returns;

import application.domain.enums.ReturnStatus;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.ReturnNotAllowedException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Return;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ReturnRepository;
import application.domain.services.ServiceValidations;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RequestReturnService
 *
 * Catalog service "Request Return". A Return can
 * only be requested against a delivered Order by the buyer who placed it,
 * and an Order holds at most one active return (a new one is allowed only
 * after the previous one was rejected).
 */
public class RequestReturnService {

    private final BuyerRepository buyerRepository;
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final NotificationService notificationService;

    public RequestReturnService(BuyerRepository buyerRepository,
                                OrderRepository orderRepository,
                                ReturnRepository returnRepository,
                                NotificationService notificationService) {
        if (buyerRepository == null || orderRepository == null || returnRepository == null
                || notificationService == null) {
            throw new IllegalArgumentException("RequestReturnService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.notificationService = notificationService;
    }

    /**
     * @param buyerId  identifier of the Buyer requesting the return
     * @param orderId  identifier of the delivered Order
     * @param reason   reason provided by the buyer
     * @return the requested Return (status REQUESTED)
     */
    public Return requestReturn(String buyerId, String orderId, String reason) {
        ServiceValidations.requireText(buyerId, "buyer id");
        ServiceValidations.requireText(orderId, "order id");
        ServiceValidations.requireText(reason, "reason");

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        buyer.requireActive();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        requireNoActiveReturn(buyerId, orderId);

        Return returnRequest = Return.request(order, buyer, reason,
                UUID.randomUUID().toString(), LocalDateTime.now());
        returnRepository.save(returnRequest);
        notificationService.notify(buyer, "Return requested",
                "Return " + returnRequest.getReturnId() + " was requested for order " + orderId);
        return returnRequest;
    }

    /** Only one active return per order; a new one is allowed only after a rejection. */
    private void requireNoActiveReturn(String buyerId, String orderId) {
        boolean active = returnRepository.findByBuyerId(buyerId).stream()
                .anyMatch(r -> r.getOrder().getOrderId().equals(orderId)
                        && r.getReturnStatus() != ReturnStatus.REJECTED);
        if (active) {
            throw new ReturnNotAllowedException(orderId, "the order already has an active return");
        }
    }
}
