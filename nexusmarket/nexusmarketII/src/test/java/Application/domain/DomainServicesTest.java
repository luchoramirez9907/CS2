package Application.domain;

import Application.domain.enums.BusinessOperation;
import Application.domain.enums.RefundStatus;
import Application.domain.enums.ReturnStatus;
import Application.domain.enums.ShipmentStatus;
import Application.domain.exceptions.DuplicateEmailException;
import Application.domain.exceptions.InsufficientInventoryException;
import Application.domain.exceptions.InvalidRoleAssignmentException;
import Application.domain.exceptions.OrderAlreadyFinalizedException;
import Application.domain.exceptions.OwnershipAccessDeniedException;
import Application.domain.models.Administrator;
import Application.domain.models.Buyer;
import Application.domain.models.CommercialReport;
import Application.domain.models.DigitalProduct;
import Application.domain.models.Inventory;
import Application.domain.models.LogisticsOperator;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.PhysicalProduct;
import Application.domain.models.Product;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.models.Seller;
import Application.domain.models.SellerPerformanceReport;
import Application.domain.models.ShipmentTrackingEvent;
import Application.domain.models.ShoppingCart;
import Application.domain.models.Supervisor;
import Application.domain.models.Warehouse;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.ShipmentTrackingRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.services.AuthorizationService;
import Application.domain.services.BillingService;
import Application.domain.services.BuyerManagementService;
import Application.domain.services.InventoryManagementService;
import Application.domain.services.InventoryReservationService;
import Application.domain.services.OrderCheckoutService;
import Application.domain.services.OrderManagementService;
import Application.domain.services.ProductManagementService;
import Application.domain.services.RefundProcessingService;
import Application.domain.services.ReportingService;
import Application.domain.services.ReturnManagementService;
import Application.domain.services.SellerManagementService;
import Application.domain.services.ShipmentDispatchService;
import Application.domain.services.ShoppingCartService;
import Application.domain.services.UserManagementService;
import Application.domain.services.WarehouseManagementService;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;
import Application.domain.valueobjects.InventoryMovementType;
import Application.domain.valueobjects.OrderStatus;
import Application.domain.valueobjects.ProductStatus;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure domain tests for the service catalog (SDD/domain/Services.md),
 * using in-memory fakes of the output ports.
 */
class DomainServicesTest {

    static class FakeWarehouseRepository implements WarehouseRepository {
        final Map<String, Warehouse> store = new HashMap<>();

        public void save(Warehouse warehouse) { store.put(warehouse.getIdentifier(), warehouse); }

        public Optional<Warehouse> findById(String id) { return Optional.ofNullable(store.get(id)); }
    }

    static class FakeTrackingRepository implements ShipmentTrackingRepository {
        final List<ShipmentTrackingEvent> events = new ArrayList<>();

        public void record(String shipmentId, String orderId, String event,
                           LocalDateTime occurredAt, String details) {
            events.add(new ShipmentTrackingEvent(shipmentId, orderId, event, occurredAt, details));
        }

        public List<ShipmentTrackingEvent> findByShipmentId(String shipmentId) {
            return events.stream().filter(e -> e.shipmentId().equals(shipmentId)).toList();
        }
    }

    private static final Address ADDRESS = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");

    private DomainBusinessRulesTest.FakePersonRepository personRepo;
    private DomainBusinessRulesTest.FakeBuyerRepository buyerRepo;
    private DomainBusinessRulesTest.FakeSellerRepository sellerRepo;
    private DomainBusinessRulesTest.FakeProductRepository productRepo;
    private DomainBusinessRulesTest.FakeInventoryRepository inventoryRepo;
    private DomainBusinessRulesTest.FakeMovementRepository movementRepo;
    private DomainBusinessRulesTest.FakeCartRepository cartRepo;
    private DomainBusinessRulesTest.FakeOrderRepository orderRepo;
    private DomainBusinessRulesTest.FakeReturnRepository returnRepo;
    private FakeWarehouseRepository warehouseRepo;
    private FakeTrackingRepository trackingRepo;

    private AuthorizationService authorization;
    private UserManagementService users;
    private SellerManagementService sellers;
    private BuyerManagementService buyers;
    private WarehouseManagementService warehouses;
    private ProductManagementService products;
    private InventoryManagementService inventory;
    private ShoppingCartService carts;
    private OrderCheckoutService checkout;
    private OrderManagementService orders;
    private BillingService billing;
    private ShipmentDispatchService logistics;
    private ReturnManagementService returns;
    private RefundProcessingService refunds;
    private ReportingService reports;

    private Administrator admin;
    private Supervisor supervisor;
    private LogisticsOperator operator;
    private Seller seller;
    private Buyer buyer;
    private Buyer otherBuyer;
    private PhysicalProduct keyboard;

    @BeforeEach
    void setUp() {
        Map<String, Person> personStore = new HashMap<>();
        personRepo = new DomainBusinessRulesTest.FakePersonRepository(personStore);
        buyerRepo = new DomainBusinessRulesTest.FakeBuyerRepository(personStore);
        sellerRepo = new DomainBusinessRulesTest.FakeSellerRepository(personStore);
        productRepo = new DomainBusinessRulesTest.FakeProductRepository();
        inventoryRepo = new DomainBusinessRulesTest.FakeInventoryRepository();
        movementRepo = new DomainBusinessRulesTest.FakeMovementRepository();
        cartRepo = new DomainBusinessRulesTest.FakeCartRepository();
        orderRepo = new DomainBusinessRulesTest.FakeOrderRepository();
        returnRepo = new DomainBusinessRulesTest.FakeReturnRepository();
        warehouseRepo = new FakeWarehouseRepository();
        trackingRepo = new FakeTrackingRepository();
        NotificationService notifications = new DomainBusinessRulesTest.FakeNotifications();

        authorization = new AuthorizationService(personRepo);
        InventoryReservationService reservation = new InventoryReservationService(inventoryRepo, movementRepo);
        users = new UserManagementService(personRepo, authorization, notifications);
        sellers = new SellerManagementService(sellerRepo, personRepo, authorization, notifications);
        buyers = new BuyerManagementService(buyerRepo, personRepo, orderRepo, returnRepo,
                authorization, notifications);
        warehouses = new WarehouseManagementService(warehouseRepo, sellerRepo, authorization);
        products = new ProductManagementService(productRepo, authorization, notifications);
        inventory = new InventoryManagementService(inventoryRepo, movementRepo, productRepo,
                warehouseRepo, authorization);
        carts = new ShoppingCartService(buyerRepo, cartRepo, productRepo, reservation, notifications);
        billing = new BillingService(orderRepo, authorization, notifications);
        checkout = new OrderCheckoutService(buyerRepo, cartRepo, orderRepo, billing, notifications);
        orders = new OrderManagementService(orderRepo, authorization, notifications);
        logistics = new ShipmentDispatchService(orderRepo, warehouseRepo, personRepo, trackingRepo,
                reservation, authorization, notifications);
        returns = new ReturnManagementService(buyerRepo, orderRepo, returnRepo, inventoryRepo,
                reservation, authorization, notifications);
        refunds = new RefundProcessingService(returnRepo, authorization, notifications);
        reports = new ReportingService(orderRepo, returnRepo, inventoryRepo, productRepo, sellerRepo,
                authorization);

        admin = new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE);
        personRepo.save(admin);
        supervisor = (Supervisor) users.registerUser("admin-1", "sup-1", "Sam Supervisor", "sup@x.com",
                SystemRole.SUPERVISOR);
        operator = (LogisticsOperator) users.registerUser("admin-1", "op-1", "Olga Operator", "op@x.com",
                SystemRole.LOGISTICS_OPERATOR);
        seller = new Seller("seller-1", "TechStore", "seller@x.com", admin, UserStatus.ACTIVE);
        sellerRepo.save(seller);
        buyer = buyers.registerBuyer(null, "buyer-1", "Bruno Buyer", "buyer@x.com", ADDRESS);
        otherBuyer = buyers.registerBuyer("admin-1", "buyer-2", "Berta Buyer", "buyer2@x.com", ADDRESS);

        warehouses.registerWarehouse("admin-1", "wh-1", "Main Warehouse", ADDRESS, null);
        keyboard = new PhysicalProduct("product-1", "Keyboard", "RGB keyboard", ProductStatus.PUBLISHED, seller);
        productRepo.save(keyboard);
        inventory.registerMovement("op-1", "product-1", "wh-1", InventoryMovementType.STOCK_IN, 10);
    }

    private Order paidOrder(int quantity) {
        carts.addItemToCart("buyer-1", null, "product-1", quantity, new BigDecimal("50.00"));
        Order order = checkout.checkout("buyer-1", null);
        return orders.confirmPayment("admin-1", order.getOrderId());
    }

    private Inventory stock() {
        return inventoryRepo.findByProductIdAndWarehouseId("product-1", "wh-1").orElseThrow();
    }

    // ------------------------------------------------------------------ Authorization

    @Test
    void permissionsFollowTheRoleMatrix() {
        assertTrue(authorization.hasPermission(admin, BusinessOperation.REGISTER_USER));
        assertFalse(authorization.hasPermission(supervisor, BusinessOperation.UPDATE_PRODUCT));
        assertFalse(authorization.hasPermission(buyer, BusinessOperation.CONSULT_COMMERCIAL_REPORT));
        assertTrue(authorization.hasPermission(seller, BusinessOperation.PUBLISH_PRODUCT));

        assertThrows(InvalidRoleAssignmentException.class,
                () -> authorization.requirePermission("buyer-1", BusinessOperation.CONFIRM_ORDER_PAYMENT));

        users.changeUserStatus("admin-1", "op-1", UserStatus.BLOCKED);
        assertFalse(authorization.hasPermission(operator, BusinessOperation.CREATE_SHIPMENT));
        assertThrows(IllegalStateException.class,
                () -> authorization.requirePermission("op-1", BusinessOperation.CREATE_SHIPMENT));
    }

    // ------------------------------------------------------------------ Users, sellers, buyers

    @Test
    void usersAreRegisteredByAdministratorsAndOnlyManageThemselves() {
        assertThrows(InvalidRoleAssignmentException.class,
                () -> users.registerUser("sup-1", "op-2", "Other", "op2@x.com", SystemRole.LOGISTICS_OPERATOR));
        assertThrows(IllegalArgumentException.class,
                () -> users.registerUser("admin-1", "seller-2", "S", "s2@x.com", SystemRole.SELLER));
        assertThrows(DuplicateEmailException.class,
                () -> users.registerUser("admin-1", "op-3", "Dup", "op@x.com", SystemRole.LOGISTICS_OPERATOR));

        Person updated = users.updateUser("op-1", "op-1", "Olga O.", "olga@x.com");
        assertEquals("olga@x.com", updated.getEmail());
        assertThrows(OwnershipAccessDeniedException.class, () -> users.consultUser("op-1", "sup-1"));
        assertThrows(DuplicateEmailException.class,
                () -> users.updateUser("op-1", "op-1", "Olga", "admin@x.com"));
    }

    @Test
    void sellersAndBuyersOnlyAccessTheirOwnInformation() {
        assertThrows(OwnershipAccessDeniedException.class, () -> buyers.consultBuyer("buyer-2", "buyer-1"));
        assertEquals("buyer-1", buyers.consultBuyer("sup-1", "buyer-1").getIdentifier());

        Buyer updated = buyers.updateBuyer("buyer-1", "buyer-1", "Bruno B.", "bruno@x.com", null,
                List.of(ADDRESS));
        assertEquals(1, updated.getAdditionalAddresses().size());

        buyers.changeCommercialStatus("admin-1", "buyer-1", BuyerCommercialStatus.BLOCKED);
        assertThrows(Application.domain.exceptions.BuyerNotAuthorizedException.class,
                () -> carts.addItemToCart("buyer-1", null, "product-1", 1, BigDecimal.ONE));

        assertEquals("TechStore Updated",
                sellers.updateSeller("seller-1", "seller-1", "TechStore Updated", "seller@x.com").getFullName());
        assertThrows(InvalidRoleAssignmentException.class,
                () -> sellers.changeSellerStatus("seller-1", "seller-1", UserStatus.INACTIVE));
        assertEquals(UserStatus.INACTIVE,
                sellers.changeSellerStatus("admin-1", "seller-1", UserStatus.INACTIVE).getStatus());
    }

    // ------------------------------------------------------------------ Warehouses and catalog

    @Test
    void sellersOnlyRegisterAndManageTheirOwnWarehouses() {
        Warehouse own = warehouses.registerWarehouse("seller-1", "wh-s1", "Seller WH", ADDRESS, "seller-1");
        assertEquals("wh-s1", own.getIdentifier());
        assertThrows(InvalidRoleAssignmentException.class,
                () -> warehouses.registerWarehouse("seller-1", "wh-x", "Marketplace", ADDRESS, null));
        assertThrows(OwnershipAccessDeniedException.class,
                () -> warehouses.updateWarehouse("seller-1", "wh-1", "Hacked", ADDRESS));

        Warehouse renamed = warehouses.updateWarehouse("seller-1", "wh-s1", "Renamed", ADDRESS);
        assertEquals("Renamed", renamed.getName());
    }

    @Test
    void discontinuedProductsArePermanentAndHiddenFromTheCatalog() {
        Product updated = products.updateProduct("seller-1", "product-1", "Keyboard Pro", "New", null);
        assertEquals("Keyboard Pro", updated.getName());

        products.changeProductStatus("seller-1", "product-1", ProductStatus.SUSPENDED);
        assertThrows(IllegalArgumentException.class, () -> products.consultProduct(null, "product-1"));
        assertEquals("product-1", products.consultProduct("seller-1", "product-1").getIdentifier());

        products.changeProductStatus("admin-1", "product-1", ProductStatus.DISCONTINUED);
        assertThrows(IllegalArgumentException.class,
                () -> products.changeProductStatus("seller-1", "product-1", ProductStatus.PUBLISHED));
        assertThrows(IllegalArgumentException.class,
                () -> products.updateProduct("seller-1", "product-1", "Again", null, null));
    }

    // ------------------------------------------------------------------ Inventory and cart

    @Test
    void damagedStockCannotBeReservedAndStockNeverGoesNegative() {
        inventory.registerDamagedStock("op-1", "product-1", "wh-1", 8);
        Inventory record = stock();
        assertEquals(2, record.getAvailableQuantity());
        assertEquals(8, record.getDamagedQuantity());

        assertThrows(InsufficientInventoryException.class,
                () -> inventory.registerMovement("admin-1", "product-1", "wh-1", InventoryMovementType.RESERVATION, 3));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "product-1", "wh-1", InventoryMovementType.ADJUSTMENT, -5));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "product-1", "wh-1", InventoryMovementType.SALE_EXIT, 1));

        DigitalProduct ebook = new DigitalProduct("ebook-1", "Ebook", null, ProductStatus.PUBLISHED, seller, "link");
        productRepo.save(ebook);
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "ebook-1", "wh-1", InventoryMovementType.STOCK_IN, 1));
    }

    @Test
    void cartChangesReserveAndReleaseStock() {
        carts.addItemToCart("buyer-1", null, "product-1", 4, new BigDecimal("50.00"));
        assertEquals(6, stock().getAvailableQuantity());

        carts.updateItemQuantity("buyer-1", "product-1", 2);
        assertEquals(8, stock().getAvailableQuantity());
        assertEquals(2, stock().getReservedQuantity());

        ShoppingCart cart = carts.removeItem("buyer-1", "product-1");
        assertTrue(cart.isEmpty());
        assertEquals(10, stock().getAvailableQuantity());
        assertEquals(0, stock().getReservedQuantity());
        assertTrue(carts.consultCart("buyer-2").isEmpty());
    }

    // ------------------------------------------------------------------ Order lifecycle, logistics, billing

    @Test
    void fullOrderLifecycleThroughLogisticsAndFinalization() {
        Order order = paidOrder(3);
        String orderId = order.getOrderId();
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertNotNull(billing.consultInvoice("buyer-1", orderId));
        assertThrows(IllegalArgumentException.class, () -> billing.generateInvoice("admin-1", orderId));
        assertThrows(OwnershipAccessDeniedException.class, () -> orders.consultOrder("buyer-2", orderId));
        assertEquals(orderId, orders.consultOrder("seller-1", orderId).getOrderId());

        logistics.createShipment("admin-1", orderId, "wh-1", "op-1");
        assertThrows(IllegalArgumentException.class, () -> orders.finalizeOrder("admin-1", orderId));

        logistics.dispatchShipment("op-1", orderId);
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());
        assertEquals(0, stock().getReservedQuantity());
        assertEquals(7, stock().getAvailableQuantity());

        logistics.confirmDelivery("op-1", orderId);
        assertEquals(ShipmentStatus.DELIVERED, logistics.consultShipment("buyer-1", orderId).getShipmentStatus());
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());

        orders.finalizeOrder("op-1", orderId);
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
        assertThrows(OrderAlreadyFinalizedException.class, () -> orders.confirmPayment("admin-1", orderId));

        List<ShipmentTrackingEvent> tracking = logistics.consultTracking("buyer-1", orderId);
        assertEquals(List.of("CREATED", "DISPATCHED", "DELIVERED"),
                tracking.stream().map(ShipmentTrackingEvent::event).toList());
        assertEquals(1, buyers.consultBuyerOrders("buyer-1", "buyer-1").size());
    }

    @Test
    void digitalOnlyOrdersAreDeliveredUponPaymentConfirmation() {
        DigitalProduct ebook = new DigitalProduct("ebook-1", "Ebook", null, ProductStatus.PUBLISHED, seller, "link");
        productRepo.save(ebook);
        carts.addItemToCart("buyer-1", null, "ebook-1", 1, new BigDecimal("10.00"));
        Order order = checkout.checkout("buyer-1", null);

        orders.confirmPayment("admin-1", order.getOrderId());
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
        assertThrows(IllegalArgumentException.class,
                () -> logistics.createShipment("admin-1", order.getOrderId(), "wh-1", "op-1"));
    }

    // ------------------------------------------------------------------ Returns, refunds and reports

    @Test
    void returnsAreResolvedAndRefundsConsultedByTheirOwners() {
        Order order = paidOrder(2);
        String orderId = order.getOrderId();
        logistics.createShipment("op-1", orderId, "wh-1", "op-1");
        logistics.dispatchShipment("op-1", orderId);
        logistics.confirmDelivery("op-1", orderId);
        orders.finalizeOrder("admin-1", orderId);

        Return rejected = returns.requestReturn("buyer-1", orderId, "changed my mind");
        returns.rejectReturn("admin-1", rejected.getReturnId());
        assertEquals(ReturnStatus.REJECTED, returns.consultReturn("buyer-1", rejected.getReturnId()).getReturnStatus());

        Return approved = returns.requestReturn("buyer-1", orderId, "damaged box");
        returns.approveReturn("admin-1", approved.getReturnId());
        assertThrows(IllegalArgumentException.class,
                () -> refunds.processRefund("admin-1", approved.getReturnId(), new BigDecimal("1000")));
        refunds.processRefund("sup-1", approved.getReturnId(), new BigDecimal("40.00"));

        Refund refund = refunds.consultRefund("buyer-1", approved.getReturnId());
        assertEquals(RefundStatus.PROCESSED, refund.getRefundStatus());
        assertThrows(OwnershipAccessDeniedException.class,
                () -> refunds.consultRefund("buyer-2", approved.getReturnId()));
        assertEquals(1, buyers.consultBuyerRefunds("buyer-1", "buyer-1").size());
        assertEquals(2, buyers.consultBuyerReturns("buyer-1", "buyer-1").size());

        CommercialReport commercial = reports.consultCommercialReport("sup-1");
        assertEquals(1, commercial.totalOrders());
        assertEquals(0, new BigDecimal("100.00").compareTo(commercial.grossRevenue()));
        assertEquals(0, new BigDecimal("60.00").compareTo(commercial.netRevenue()));
        assertEquals(8, commercial.inventoryLevels().get(0).availableQuantity());

        SellerPerformanceReport performance = reports.consultSellerPerformanceReport("seller-1", "seller-1");
        assertEquals(2, performance.unitsSold());
        assertEquals(2, performance.returnsCount());
        assertThrows(InvalidRoleAssignmentException.class,
                () -> reports.consultCommercialReport("seller-1"));
    }
}
