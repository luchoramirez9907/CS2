package application.domain.services.billing;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.ports.out.OrderRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;

/**
 * ConsultInvoiceService
 *
 * Catalog service "Consult Invoice". Retrieves the
 * invoice of an order for the requesters that may access that order.
 */
public class ConsultInvoiceService {

    private final OrderRepository orderRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ConsultInvoiceService(OrderRepository orderRepository,
                                 ValidatePermissionsService permissionsService,
                                 ValidateOwnershipAccessService ownershipService) {
        if (orderRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("ConsultInvoiceService requires its dependencies");
        }
        this.orderRepository = orderRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    public Invoice consultInvoice(String requesterId, String orderId) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_INVOICE);
        ServiceValidations.requireText(orderId, "order id");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        ownershipService.requireOrderAccess(requester, order);
        if (order.getInvoice() == null) {
            throw new IllegalArgumentException("Order '" + orderId + "' has no invoice");
        }
        return order.getInvoice();
    }
}
