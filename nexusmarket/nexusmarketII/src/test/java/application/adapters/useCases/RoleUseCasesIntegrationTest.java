package application.adapters.useCases;

import application.domain.enums.ReturnStatus;
import application.domain.exceptions.OwnershipAccessDeniedException;
import application.domain.models.Buyer;
import application.domain.models.CommercialReport;
import application.domain.models.Order;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.SellerPerformanceReport;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.in.AdministratorPort;
import application.domain.ports.in.BuyerPort;
import application.domain.ports.in.LogisticsOperatorPort;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.in.SellerPort;
import application.domain.ports.in.SupervisorPort;
import application.domain.ports.out.UserRepository;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the complete purchase and post-sale lifecycle exclusively
 * through the Role Input Ports (adapters/useCases), on the real Spring
 * context with in-memory persistence and the demo data set.
 */
@SpringBootTest
@ActiveProfiles({"memory", "demo"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RoleUseCasesIntegrationTest {

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
    void completeOrderLifecycleThroughRolePorts() {
        User admin = user("administrator-1");
        User buyer = user("buyer-1");
        User seller = user("seller-1");

        // Public catalog and onboarding of the remaining participants
        assertEquals(new BigDecimal("249900.00"), publicAccess.consultCatalogProduct("product-1").getPrice());
        administratorPort.registerUser(admin, "operator-9", "Olga Operator", "operator9@nexusmarket.com",
                SystemRole.LOGISTICS_OPERATOR, PASSWORD);
        administratorPort.registerUser(admin, "supervisor-9", "Sam Supervisor", "supervisor9@nexusmarket.com",
                SystemRole.SUPERVISOR, PASSWORD);
        Buyer otherBuyer = publicAccess.registerBuyer("buyer-2", "Berta Buyer", "buyer2@nexusmarket.com",
                ADDRESS, PASSWORD);
        User operator = user("operator-9");
        User supervisor = user("supervisor-9");

        // Buyer: cart and checkout (price taken from the catalog)
        buyerPort.addItemToCart(buyer, "product-1", 2);
        Order order = buyerPort.checkout(buyer);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
        assertEquals(new BigDecimal("499800.00"), order.getTotalAmount());
        assertEquals(1, buyerPort.consultMyOrders(buyer).size());
        String orderId = order.getOrderId();

        // A buyer never accesses another buyer's order (RG-03)
        assertThrows(OwnershipAccessDeniedException.class,
                () -> buyerPort.consultOrder(user(otherBuyer.getIdentifier()), orderId));

        // Administrator and logistics operator: payment, shipment and delivery
        administratorPort.confirmOrderPayment(admin, orderId);
        Shipment shipment = operatorPort.createShipment(operator, orderId, "warehouse-1");
        assertEquals("operator-9", shipment.getLogisticsOperator().getIdentifier());
        operatorPort.dispatchShipment(operator, orderId);
        operatorPort.confirmDelivery(operator, orderId);
        assertEquals(OrderStatus.DELIVERED, operatorPort.finalizeOrder(operator, orderId).getOrderStatus());
        assertEquals(3, buyerPort.consultTracking(buyer, orderId).size());

        // Post-sale: return requested by the buyer, approved and refunded by the administrator
        Return returnRequest = buyerPort.requestReturn(buyer, orderId, "damaged box");
        administratorPort.approveReturn(admin, returnRequest.getReturnId());
        Refund refund = administratorPort.processRefund(admin, returnRequest.getReturnId(), null);
        assertEquals(order.getTotalAmount(), refund.getAmount());
        assertEquals(1, buyerPort.consultMyRefunds(buyer).size());

        // Completing the return reinstates the sold units into the available stock
        assertEquals(48, administratorPort.consultInventory(admin, "product-1", "warehouse-1").getAvailableQuantity());
        assertEquals(ReturnStatus.COMPLETED,
                administratorPort.completeReturn(admin, returnRequest.getReturnId()).getReturnStatus());
        assertEquals(50, administratorPort.consultInventory(admin, "product-1", "warehouse-1").getAvailableQuantity());

        // Read-only consultations
        CommercialReport commercialReport = supervisorPort.consultCommercialReport(supervisor);
        assertTrue(commercialReport.totalOrders() >= 1);
        assertEquals(refund.getAmount(), supervisorPort.consultRefund(supervisor, returnRequest.getReturnId())
                .getAmount());
        SellerPerformanceReport performance = sellerPort.consultMyPerformanceReport(seller);
        assertEquals(1, performance.ordersCount());
        assertEquals(2, performance.unitsSold());
    }

    private User user(String personId) {
        return userRepository.findByPersonId(personId).orElseThrow();
    }
}
