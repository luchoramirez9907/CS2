package application.domain.ports.in;

import application.domain.enums.ReturnStatus;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.SellerPerformanceReport;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Role Input Port (SELLER): operations a seller performs over its own
 * profile, warehouses, products and inventory, and the consultation of the
 * orders and returns that contain its products.
 *
 * Every method receives the authenticated User; the seller only operates
 * on its own information (RG-03).
 */
public interface SellerPort {

    // Profile
    Seller consultMyProfile(User user);

    Seller updateMyProfile(User user, String fullName, String email);

    // Warehouses (always owned by the seller)
    Warehouse registerWarehouse(User user, String identifier, String name, Address address);

    Warehouse consultWarehouse(User user, String warehouseId);

    Warehouse updateWarehouse(User user, String warehouseId, String name, Address address);

    // Catalog
    Product publishProduct(User user, Product product);

    Product consultProduct(User user, String productId);

    Product updateProduct(User user, String productId, String name, String description,
                          BigDecimal price, List<ProductVariant> variants);

    Product changeProductStatus(User user, String productId, ProductStatus status);

    // Inventory
    Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                        InventoryMovementType type, int quantity);

    Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity);

    Inventory consultInventory(User user, String productId, String warehouseId);

    // Orders and returns containing the seller's products
    Order consultOrder(User user, String orderId);

    Invoice consultInvoice(User user, String orderId);

    Shipment consultShipment(User user, String orderId);

    List<ShipmentTrackingEvent> consultTracking(User user, String orderId);

    Return consultReturn(User user, String returnId);

    // Reporting
    SellerPerformanceReport consultMyPerformanceReport(User user);

    // Listings and history
    List<Product> consultMyProducts(User user);

    List<Warehouse> consultMyWarehouses(User user);

    List<Order> consultMyOrders(User user, OrderStatus status);

    List<Return> consultMyReturns(User user, ReturnStatus status);

    List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId);
}
