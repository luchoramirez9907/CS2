package application.adapters.useCases;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.logistics.ConsultShipmentService;
import application.domain.services.logistics.CreateShipmentService;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.services.catalog.ManageProductService;
import application.domain.services.user.ManageUserService;
import application.domain.services.warehouse.ManageWarehouseService;
import application.domain.services.inventory.RegisterInventoryMovementService;
import application.domain.services.order.UpdateOrderStatusService;
import application.domain.services.logistics.UpdateShipmentStatusService;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case Adapter implementing LogisticsOperatorPort. Shipments created
 * through this port are assigned to the authenticated operator; delegates
 * to the Domain Services and
 * contains no business rules.
 */
@Service
public class LogisticsOperatorUseCaseImpl implements LogisticsOperatorPort {

    private final ManageUserService manageUserService;
    private final ManageWarehouseService manageWarehouseService;
    private final ManageProductService manageProductService;
    private final RegisterInventoryMovementService registerInventoryMovementService;
    private final ConsultInventoryService consultInventoryService;
    private final CreateShipmentService createShipmentService;
    private final UpdateShipmentStatusService updateShipmentStatusService;
    private final ConsultShipmentService consultShipmentService;
    private final ConsultOrderService consultOrderService;
    private final UpdateOrderStatusService updateOrderStatusService;

    public LogisticsOperatorUseCaseImpl(ManageUserService manageUserService,
                                        ManageWarehouseService manageWarehouseService,
                                        ManageProductService manageProductService,
                                        RegisterInventoryMovementService registerInventoryMovementService,
                                        ConsultInventoryService consultInventoryService,
                                        CreateShipmentService createShipmentService,
                                        UpdateShipmentStatusService updateShipmentStatusService,
                                        ConsultShipmentService consultShipmentService,
                                        ConsultOrderService consultOrderService,
                                        UpdateOrderStatusService updateOrderStatusService) {
        this.manageUserService = manageUserService;
        this.manageWarehouseService = manageWarehouseService;
        this.manageProductService = manageProductService;
        this.registerInventoryMovementService = registerInventoryMovementService;
        this.consultInventoryService = consultInventoryService;
        this.createShipmentService = createShipmentService;
        this.updateShipmentStatusService = updateShipmentStatusService;
        this.consultShipmentService = consultShipmentService;
        this.consultOrderService = consultOrderService;
        this.updateOrderStatusService = updateOrderStatusService;
    }

    // ---------------------------------------------------------------- Profile

    @Override
    public Person consultMyProfile(User user) {
        return manageUserService.consultUser(user.getIdentifier(), user.getIdentifier());
    }

    @Override
    public Person updateMyProfile(User user, String fullName, String email) {
        return manageUserService.updateUser(user.getIdentifier(), user.getIdentifier(), fullName, email);
    }

    // ---------------------------------------------------------------- Warehouses, catalog and inventory

    @Override
    public Warehouse consultWarehouse(User user, String warehouseId) {
        return manageWarehouseService.consultWarehouse(user.getIdentifier(), warehouseId);
    }

    @Override
    public Product consultProduct(User user, String productId) {
        return manageProductService.consultProduct(user.getIdentifier(), productId);
    }

    @Override
    public Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                               InventoryMovementType type, int quantity) {
        return registerInventoryMovementService.registerMovement(user.getIdentifier(), productId, warehouseId,
                type, quantity);
    }

    @Override
    public Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity) {
        return registerInventoryMovementService.registerDamagedStock(user.getIdentifier(), productId,
                warehouseId, quantity);
    }

    @Override
    public Inventory consultInventory(User user, String productId, String warehouseId) {
        return consultInventoryService.consultInventory(user.getIdentifier(), productId, warehouseId);
    }

    // ---------------------------------------------------------------- Logistics

    @Override
    public Shipment createShipment(User user, String orderId, String originWarehouseId) {
        // The shipment is assigned to the authenticated operator.
        return createShipmentService.createShipment(user.getIdentifier(), orderId, originWarehouseId,
                user.getIdentifier());
    }

    @Override
    public Shipment dispatchShipment(User user, String orderId) {
        return updateShipmentStatusService.dispatchShipment(user.getIdentifier(), orderId);
    }

    @Override
    public Shipment confirmDelivery(User user, String orderId) {
        return updateShipmentStatusService.confirmDelivery(user.getIdentifier(), orderId);
    }

    @Override
    public Shipment consultShipment(User user, String orderId) {
        return consultShipmentService.consultShipment(user.getIdentifier(), orderId);
    }

    @Override
    public List<ShipmentTrackingEvent> consultTracking(User user, String orderId) {
        return consultShipmentService.consultTracking(user.getIdentifier(), orderId);
    }

    // ---------------------------------------------------------------- Orders

    @Override
    public Order consultOrder(User user, String orderId) {
        return consultOrderService.consultOrder(user.getIdentifier(), orderId);
    }

    @Override
    public Order finalizeOrder(User user, String orderId) {
        return updateOrderStatusService.finalizeOrder(user.getIdentifier(), orderId);
    }

    // ---------------------------------------------------------------- Listings, history and failed deliveries

    @Override
    public List<Warehouse> listWarehouses(User user) {
        return manageWarehouseService.listWarehouses(user.getIdentifier());
    }

    @Override
    public List<Order> consultAssignedOrders(User user, OrderStatus status) {
        // Only the orders whose shipment is assigned to the operator are visible.
        return consultOrderService.listOrders(user.getIdentifier(), status);
    }

    @Override
    public Shipment registerShipmentReturn(User user, String orderId, String reason) {
        return updateShipmentStatusService.registerShipmentReturn(user.getIdentifier(), orderId, reason);
    }

    @Override
    public List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId) {
        return consultInventoryService.consultMovementHistory(user.getIdentifier(), productId, warehouseId);
    }
}
