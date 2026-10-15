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
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Role Input Port (ADMINISTRATOR): administration of participants, sellers,
 * buyers and warehouses, the order and post-sale lifecycle, and the
 * administrative reports.
 *
 * Every method receives the authenticated User.
 */
public interface AdministratorPort {

    // Users (administrators, supervisors and logistics operators)
    Person registerUser(User user, String identifier, String fullName, String email, SystemRole role,
                        String password);

    Person consultUser(User user, String userId);

    Person updateUser(User user, String userId, String fullName, String email);

    Person changeUserStatus(User user, String userId, UserStatus status);

    // Sellers (a seller can only be registered by an administrator)
    Seller registerSeller(User user, String identifier, String fullName, String email, String password);

    Seller consultSeller(User user, String sellerId);

    Seller updateSeller(User user, String sellerId, String fullName, String email);

    Seller changeSellerStatus(User user, String sellerId, UserStatus status);

    // Buyers
    Buyer registerBuyer(User user, String identifier, String fullName, String email, Address primaryAddress,
                        String password);

    Buyer consultBuyer(User user, String buyerId);

    Buyer updateBuyer(User user, String buyerId, String fullName, String email, Address primaryAddress,
                      List<Address> additionalAddresses);

    Buyer changeBuyerCommercialStatus(User user, String buyerId, BuyerCommercialStatus status);

    List<Order> consultBuyerOrders(User user, String buyerId);

    List<Return> consultBuyerReturns(User user, String buyerId);

    List<Refund> consultBuyerRefunds(User user, String buyerId);

    // Warehouses (ownerSellerId null registers a Marketplace warehouse)
    Warehouse registerWarehouse(User user, String identifier, String name, Address address,
                                String ownerSellerId);

    Warehouse consultWarehouse(User user, String warehouseId);

    Warehouse updateWarehouse(User user, String warehouseId, String name, Address address);

    // Catalog and inventory
    Product consultProduct(User user, String productId);

    Product changeProductStatus(User user, String productId, ProductStatus status);

    Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                        InventoryMovementType type, int quantity);

    Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity);

    Inventory consultInventory(User user, String productId, String warehouseId);

    // Orders and billing
    Order consultOrder(User user, String orderId);

    Order confirmOrderPayment(User user, String orderId);

    Order finalizeOrder(User user, String orderId);

    Invoice generateInvoice(User user, String orderId);

    Invoice consultInvoice(User user, String orderId);

    // Logistics
    Shipment createShipment(User user, String orderId, String originWarehouseId, String logisticsOperatorId);

    Shipment dispatchShipment(User user, String orderId);

    Shipment confirmDelivery(User user, String orderId);

    Shipment consultShipment(User user, String orderId);

    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);

    // Returns and refunds (only an administrator resolves returns and approves refunds)
    Return approveReturn(User user, String returnId);

    Return rejectReturn(User user, String returnId);

    Return completeReturn(User user, String returnId);

    Return consultReturn(User user, String returnId);

    Refund processRefund(User user, String returnId, BigDecimal amount);

    Refund consultRefund(User user, String returnId);

    // Reporting
    CommercialReport consultCommercialReport(User user);

    SellerPerformanceReport consultSellerPerformanceReport(User user, String sellerId);

    // Listings and history
    List<Person> listUsers(User user, SystemRole role);

    List<Warehouse> listWarehouses(User user);

    List<Product> consultSellerProducts(User user, String sellerId);

    List<Order> listOrders(User user, OrderStatus status);

    List<Return> listReturns(User user, ReturnStatus status);

    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);

    // Failed deliveries and refund decisions
    Shipment registerShipmentReturn(User user, String orderId, String reason);

    Refund rejectRefund(User user, String returnId);
}
