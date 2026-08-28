package Application.domain.services;

import Application.domain.models.Administrator;
import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Return;
import Application.domain.ports.in.ApproveReturnUseCase;
import Application.domain.ports.in.RequestReturnUseCase;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ReturnRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ReturnManagementService
 *
 * Implements RequestReturnUseCase and ApproveReturnUseCase. A Return can
 * only be requested against a delivered Order by its own buyer, and only
 * an Administrator may approve it. Completing a return reinstates the
 * returned stock (RETURN movement).
 */
public class ReturnManagementService implements RequestReturnUseCase, ApproveReturnUseCase {

    private final BuyerRepository buyerRepository;
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final PersonRepository personRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationService inventoryReservationService;
    private final NotificationService notificationService;

    public ReturnManagementService(BuyerRepository buyerRepository,
                                   OrderRepository orderRepository,
                                   ReturnRepository returnRepository,
                                   PersonRepository personRepository,
                                   InventoryRepository inventoryRepository,
                                   InventoryReservationService inventoryReservationService,
                                   NotificationService notificationService) {
        if (buyerRepository == null || orderRepository == null || returnRepository == null
                || personRepository == null || inventoryRepository == null
                || inventoryReservationService == null || notificationService == null) {
            throw new IllegalArgumentException("ReturnManagementService requires its dependencies");
        }
        this.buyerRepository = buyerRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.personRepository = personRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.notificationService = notificationService;
    }

    @Override
    public Return requestReturn(String buyerId, String orderId, String reason) {
        requireText(buyerId, "buyer id");
        requireText(orderId, "order id");
        requireText(reason, "reason");

        Buyer buyer = buyerRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Buyer '" + buyerId + "' does not exist"));
        buyer.requireActive();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order '" + orderId + "' does not exist"));

        Return returnRequest = Return.request(order, buyer, reason,
                UUID.randomUUID().toString(), LocalDateTime.now());
        returnRepository.save(returnRequest);
        notificationService.notify(buyer, "Return requested",
                "Return " + returnRequest.getReturnId() + " was requested for order " + orderId);
        return returnRequest;
    }

    @Override
    public Return approveReturn(String administratorId, String returnId) {
        requireText(administratorId, "administrator id");
        requireText(returnId, "return id");

        Person approver = personRepository.findById(administratorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person '" + administratorId + "' does not exist"));
        approver.requireActive();
        approver.requireRole("approve a Return", Application.domain.valueobjects.SystemRole.ADMINISTRATOR);

        Return returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return '" + returnId + "' does not exist"));
        returnRequest.approve((Administrator) approver);
        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Return approved",
                "Return " + returnId + " was approved and a refund may now be processed");
        return returnRequest;
    }

    /**
     * Completes an approved return: reinstates every returned item into
     * stock (RETURN movement) and closes the return process.
     */
    public Return completeReturn(String returnId, Person performedBy) {
        requireText(returnId, "return id");
        Return returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return '" + returnId + "' does not exist"));
        returnRequest.complete();

        Order order = returnRequest.getOrder();
        for (Application.domain.models.OrderItem item : order.getItems()) {
            if (item.getProduct().requiresPhysicalDispatch()) {
                inventoryReservationService.reinstateStock(item.getProduct(), item.getQuantity(), performedBy);
            }
        }
        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Return completed",
                "Return " + returnId + " was completed and the stock was reinstated");
        return returnRequest;
    }

    /**
     * Total availability of a product across every warehouse (monitoring).
     */
    public int availableStockOf(String productId) {
        return inventoryRepository.findByProductId(productId).stream()
                .mapToInt(Application.domain.models.Inventory::getAvailableQuantity)
                .sum();
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}

