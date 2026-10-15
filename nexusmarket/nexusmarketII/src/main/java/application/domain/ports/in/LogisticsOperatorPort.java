package application.domain.ports.in;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;

import java.util.List;

/**
 * Role Input Port (LOGISTICS_OPERATOR): physical operation of warehouses
 * (inventory movements) and the dispatch and delivery of orders.
 *
 * Every method receives the authenticated User; the operator accesses the
 * orders whose shipment is assigned to it (RG-03).
 */
public interface LogisticsOperatorPort {

    // Profile
    Person consultMyProfile(User user);

    Person updateMyProfile(User user, String fullName, String email);

    // Warehouses, catalog and inventory
    Warehouse consultWarehouse(User user, String warehouseId);

    Product consultProduct(User user, String productId);

    Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                        InventoryMovementType type, int quantity);

    Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity);

    Inventory consultInventory(User user, String productId, String warehouseId);

    // Logistics (the shipment is assigned to the authenticated operator)
    Shipment createShipment(User user, String orderId, String originWarehouseId);

    Shipment dispatchShipment(User user, String orderId);

    Shipment confirmDelivery(User user, String orderId);

    Shipment consultShipment(User user, String orderId);

    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);

    // Orders
    Order consultOrder(User user, String orderId);

    Order finalizeOrder(User user, String orderId);

    // Listings, history and failed deliveries
    List<Warehouse> listWarehouses(User user);

    List<Order> consultAssignedOrders(User user, OrderStatus status);

    Shipment registerShipmentReturn(User user, String orderId, String reason);

    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
}
