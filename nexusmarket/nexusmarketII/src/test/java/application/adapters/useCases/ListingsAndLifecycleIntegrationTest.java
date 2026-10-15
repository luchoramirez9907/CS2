package application.adapters.useCases;

import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.TrackingEventType;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Return;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.ports.in.AdministratorPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.in.SupervisorPort;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises, through the Role Input Ports, the listings and searches, the
 * failed-delivery flow (shipment returned and dispatched again), the
 * inventory movement history, and the refund rejection.
 */
@SpringBootTest
@ActiveProfiles({"memory", "demo"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ListingsAndLifecycleIntegrationTest {

    /** Test-only password that satisfies the password policy. */
    private static final String PASSWORD = "Passw0rd1";
    private static final Address ADDRESS = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");

    @Autowired private PublicAccessPort publicAccess;
    @Autowired private BuyerPort buyerPort;
    @Autowired private SellerPort sellerPort;
    @Autowired private LogisticsOperatorPort operatorPort;
    @Autowired private AdministratorPort administratorPort;
    @Autowired private SupervisorPort supervisorPort;
    @Autowired private UserRepository userRepository;

    @Test
    void listingsFailedDeliveryMovementHistoryAndRefundRejection() {
        User admin = user("administrator-1");
        User buyer = user("buyer-1");
        User seller = user("seller-1");
        administratorPort.registerUser(admin, "operator-9", "Olga Operator", "operator9@nexusmarket.com",
                SystemRole.LOGISTICS_OPERATOR, PASSWORD);
        administratorPort.registerUser(admin, "supervisor-9", "Sam Supervisor", "supervisor9@nexusmarket.com",
                SystemRole.SUPERVISOR, PASSWORD);
        publicAccess.registerBuyer("buyer-2", "Berta Buyer", "buyer2@nexusmarket.com", ADDRESS, PASSWORD);
        User operator = user("operator-9");
        User supervisor = user("supervisor-9");

        // ---------------------------------------------------------------- Catalog search
        assertEquals(1, publicAccess.searchCatalog("keyboard", null).size());
        assertEquals(1, publicAccess.searchCatalog(null, "seller-1").size());
        assertTrue(publicAccess.searchCatalog("no such product", null).isEmpty());

        // ---------------------------------------------------------------- Purchase and failed delivery
        buyerPort.addItemToCart(buyer, "product-1", 3);
        String orderId = buyerPort.checkout(buyer).getOrderId();
        administratorPort.confirmOrderPayment(admin, orderId);
        operatorPort.createShipment(operator, orderId, "warehouse-1");
        operatorPort.dispatchShipment(operator, orderId);

        assertEquals(ShipmentStatus.RETURNED,
                operatorPort.registerShipmentReturn(operator, orderId, "Nobody at the address").getShipmentStatus());
        assertThrows(IllegalArgumentException.class, () -> operatorPort.finalizeOrder(operator, orderId));
        assertEquals(ShipmentStatus.IN_TRANSIT, operatorPort.dispatchShipment(operator, orderId).getShipmentStatus());
        operatorPort.confirmDelivery(operator, orderId);
        assertEquals(OrderStatus.DELIVERED, operatorPort.finalizeOrder(operator, orderId).getOrderStatus());

        List<TrackingEventType> tracking = buyerPort.consultTracking(buyer, orderId).stream()
                .map(ShipmentTrackingEvent::event).toList();
        assertEquals(List.of(TrackingEventType.CREATED, TrackingEventType.DISPATCHED, TrackingEventType.RETURNED,
                TrackingEventType.DISPATCHED, TrackingEventType.DELIVERED), tracking);

        // ---------------------------------------------------------------- Movement history (sold only once)
        List<InventoryMovementType> movements = sellerPort.consultInventoryMovements(seller, "product-1",
                "warehouse-1").stream().map(InventoryMovement::getMovementType).toList();
        assertEquals(List.of(InventoryMovementType.STOCK_IN, InventoryMovementType.RESERVATION,
                InventoryMovementType.SALE_EXIT), movements);
        assertEquals(47, administratorPort.consultInventory(admin, "product-1", "warehouse-1")
                .getAvailableQuantity());

        // ---------------------------------------------------------------- Order listings
        assertEquals(1, supervisorPort.listOrders(supervisor, null).size());
        assertEquals(1, supervisorPort.listOrders(supervisor, OrderStatus.DELIVERED).size());
        assertTrue(administratorPort.listOrders(admin, OrderStatus.PAID).isEmpty());
        assertEquals(1, operatorPort.consultAssignedOrders(operator, null).size());
        assertEquals(1, sellerPort.consultMyOrders(seller, null).size());
        List<Order> otherBuyerOrders = buyerPort.consultMyOrders(user("buyer-2"));
        assertTrue(otherBuyerOrders.isEmpty());

        // ---------------------------------------------------------------- Refund rejection
        Return returnRequest = buyerPort.requestReturn(buyer, orderId, "Not as described");
        administratorPort.approveReturn(admin, returnRequest.getReturnId());
        assertEquals(1, administratorPort.listReturns(admin, ReturnStatus.APPROVED).size());
        assertEquals(RefundStatus.REJECTED,
                administratorPort.rejectRefund(admin, returnRequest.getReturnId()).getRefundStatus());
        assertThrows(IllegalArgumentException.class,
                () -> administratorPort.processRefund(admin, returnRequest.getReturnId(), null));
        assertEquals(BigDecimal.ZERO, supervisorPort.consultCommercialReport(supervisor).refundedAmount());
        assertEquals(1, sellerPort.consultMyReturns(seller, null).size());

        // ---------------------------------------------------------------- User, warehouse and product listings
        assertEquals(2, administratorPort.listUsers(admin, SystemRole.BUYER).size());
        assertEquals(8, supervisorPort.listUsers(supervisor, null).size());

        assertTrue(sellerPort.consultMyWarehouses(seller).isEmpty());
        sellerPort.registerWarehouse(seller, "seller-wh-1", "TechStore Warehouse", ADDRESS);
        assertEquals(1, sellerPort.consultMyWarehouses(seller).size());
        assertEquals(2, administratorPort.listWarehouses(admin).size());
        assertEquals(2, operatorPort.listWarehouses(operator).size());

        administratorPort.changeProductStatus(admin, "product-1", ProductStatus.SUSPENDED);
        assertTrue(publicAccess.searchCatalog(null, null).isEmpty());
        assertEquals(1, sellerPort.consultMyProducts(seller).size());
        assertEquals(1, supervisorPort.consultSellerProducts(supervisor, "seller-1").size());
    }

    private User user(String personId) {
        return userRepository.findByPersonId(personId).orElseThrow();
    }
}
