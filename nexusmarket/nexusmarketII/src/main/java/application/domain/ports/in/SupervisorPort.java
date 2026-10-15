package application.domain.ports.in;

import application.domain.enums.ReturnStatus;
import application.domain.models.Buyer;
import application.domain.models.CommercialReport;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.SellerPerformanceReport;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.SystemRole;

import java.util.List;

/**
 * Role Input Port (SUPERVISOR): read-only operational consultation and
 * monitoring. A supervisor never creates, approves or modifies business
 * entities, so this port only exposes consultations.
 *
 * Every method receives the authenticated User.
 */
public interface SupervisorPort {

    Person consultUser(User user, String userId);

    Seller consultSeller(User user, String sellerId);

    Buyer consultBuyer(User user, String buyerId);

    List<Order> consultBuyerOrders(User user, String buyerId);

    List<Return> consultBuyerReturns(User user, String buyerId);

    List<Refund> consultBuyerRefunds(User user, String buyerId);

    Warehouse consultWarehouse(User user, String warehouseId);

    Product consultProduct(User user, String productId);

    Inventory consultInventory(User user, String productId, String warehouseId);

    Order consultOrder(User user, String orderId);

    Invoice consultInvoice(User user, String orderId);

    Shipment consultShipment(User user, String orderId);

    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);

    Return consultReturn(User user, String returnId);

    Refund consultRefund(User user, String returnId);

    CommercialReport consultCommercialReport(User user);

    SellerPerformanceReport consultSellerPerformanceReport(User user, String sellerId);

    // Listings and history
    List<Person> listUsers(User user, SystemRole role);

    List<Warehouse> listWarehouses(User user);

    List<Product> consultSellerProducts(User user, String sellerId);

    List<Order> listOrders(User user, OrderStatus status);

    List<Return> listReturns(User user, ReturnStatus status);

    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
}
