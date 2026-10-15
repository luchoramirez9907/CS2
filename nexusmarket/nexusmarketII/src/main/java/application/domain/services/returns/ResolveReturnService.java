package application.domain.services.returns;

import application.domain.enums.BusinessOperation;
import application.domain.enums.ReturnStatus;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Administrator;
import application.domain.models.OrderItem;
import application.domain.models.Person;
import application.domain.models.Return;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.ReturnRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.services.inventory.InventoryReservationService;

import java.util.Comparator;
import java.util.List;

/**
 * ResolveReturnService
 *
 * Catalog service "Resolve Return". Only an
 * Administrator approves, rejects or completes a return; completing an
 * approved return reinstates the returned stock (RETURN movement). Return
 * requests are consulted and listed according to the access permissions
 * of the requesting user.
 */
public class ResolveReturnService {

    private final ReturnRepository returnRepository;
    private final InventoryReservationService inventoryReservationService;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public ResolveReturnService(ReturnRepository returnRepository,
                                InventoryReservationService inventoryReservationService,
                                ValidatePermissionsService permissionsService,
                                ValidateOwnershipAccessService ownershipService,
                                NotificationService notificationService) {
        if (returnRepository == null || inventoryReservationService == null || permissionsService == null
                || ownershipService == null || notificationService == null) {
            throw new IllegalArgumentException("ResolveReturnService requires its dependencies");
        }
        this.returnRepository = returnRepository;
        this.inventoryReservationService = inventoryReservationService;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    /**
     * @param administratorId identifier of the Administrator approving the return
     * @param returnId        identifier of the Return
     * @return the approved Return
     */
    public Return approveReturn(String administratorId, String returnId) {
        ServiceValidations.requireText(returnId, "return id");
        Person approver = permissionsService.requirePermission(administratorId,
                BusinessOperation.RESOLVE_RETURN);

        Return returnRequest = findReturn(returnId);
        returnRequest.approve((Administrator) approver);
        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Return approved",
                "Return " + returnId + " was approved and a refund may now be processed");
        return returnRequest;
    }

    public Return rejectReturn(String administratorId, String returnId) {
        ServiceValidations.requireText(returnId, "return id");
        permissionsService.requirePermission(administratorId, BusinessOperation.RESOLVE_RETURN);

        Return returnRequest = findReturn(returnId);
        returnRequest.reject();
        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Return rejected",
                "Return " + returnId + " was rejected");
        return returnRequest;
    }

    /**
     * Completes an approved return: reinstates every returned item into
     * stock (RETURN movement) and closes the return process.
     */
    public Return completeReturn(String administratorId, String returnId) {
        ServiceValidations.requireText(returnId, "return id");
        Person performedBy = permissionsService.requirePermission(administratorId,
                BusinessOperation.RESOLVE_RETURN);
        Return returnRequest = findReturn(returnId);
        returnRequest.complete();

        for (OrderItem item : returnRequest.getOrder().getItems()) {
            if (item.getProduct().requiresPhysicalDispatch()) {
                inventoryReservationService.reinstateStock(item.getProduct(), item.getQuantity(), performedBy);
            }
        }
        returnRepository.save(returnRequest);
        notificationService.notify(returnRequest.getBuyer(), "Return completed",
                "Return " + returnId + " was completed and the stock was reinstated");
        return returnRequest;
    }

    public Return consultReturn(String requesterId, String returnId) {
        ServiceValidations.requireText(returnId, "return id");
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_RETURN);
        Return returnRequest = findReturn(returnId);
        ownershipService.requireOrderAccess(requester, returnRequest.getOrder());
        return returnRequest;
    }

    /**
     * Lists the return requests the requester may access (all for
     * Administrators and Supervisors; returns over its orders for a Buyer or
     * a Seller), newest first.
     *
     * @param status optional status filter
     */
    public List<Return> listReturns(String requesterId, ReturnStatus status) {
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CONSULT_RETURN);
        return returnRepository.findAll().stream()
                .filter(returnRequest -> status == null || returnRequest.getReturnStatus() == status)
                .filter(returnRequest -> ownershipService.canAccessOrder(requester, returnRequest.getOrder()))
                .sorted(Comparator.comparing(Return::getRequestDate).reversed())
                .toList();
    }

    private Return findReturn(String returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new EntityNotFoundException("Return", returnId));
    }
}
