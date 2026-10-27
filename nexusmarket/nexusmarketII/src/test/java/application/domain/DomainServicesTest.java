package application.domain;

import application.domain.enums.BusinessOperation;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.enums.TrackingEventType;
import application.domain.exceptions.DuplicateEmailException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InsufficientInventoryException;
import application.domain.exceptions.InvalidRoleAssignmentException;
import application.domain.exceptions.OrderAlreadyFinalizedException;
import application.domain.exceptions.OwnershipAccessDeniedException;
import application.domain.exceptions.ReturnNotAllowedException;
import application.domain.exceptions.WeakPasswordException;
import application.domain.models.Administrator;
import application.domain.models.Buyer;
import application.domain.models.CommercialReport;
import application.domain.models.DigitalProduct;
import application.domain.models.Inventory;
import application.domain.models.LogisticsOperator;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.SellerPerformanceReport;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.ShoppingCart;
import application.domain.models.Supervisor;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.ShipmentTrackingRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.authentication.UserAccountService;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.services.billing.ConsultInvoiceService;
import application.domain.services.billing.GenerateInvoiceService;
import application.domain.services.buyer.ManageBuyerService;
import application.domain.services.buyer.RegisterBuyerService;
import application.domain.services.cart.ConsultCartService;
import application.domain.services.cart.ManageCartItemsService;
import application.domain.services.catalog.ChangeProductStatusService;
import application.domain.services.catalog.ManageProductService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.inventory.RegisterInventoryMovementService;
import application.domain.services.logistics.ConsultShipmentService;
import application.domain.services.logistics.CreateShipmentService;
import application.domain.services.logistics.UpdateShipmentStatusService;
import application.domain.services.order.ConfirmOrderService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.order.UpdateOrderStatusService;
import application.domain.services.reporting.ConsultCommercialReportService;
import application.domain.services.reporting.ConsultSellerPerformanceReportService;
import application.domain.services.returns.ProcessRefundService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.returns.ResolveReturnService;
import application.domain.services.seller.ManageSellerService;
import application.domain.services.user.ManageUserService;
import application.domain.services.user.RegisterUserService;
import application.domain.services.warehouse.ManageWarehouseService;
import application.domain.services.warehouse.RegisterWarehouseService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
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

        public List<Warehouse> findAll() { return new ArrayList<>(store.values()); }
    }

    static class FakeTrackingRepository implements ShipmentTrackingRepository {
        final List<ShipmentTrackingEvent> events = new ArrayList<>();

        public void record(String shipmentId, String orderId, TrackingEventType event,
                           LocalDateTime occurredAt, String details) {
            events.add(new ShipmentTrackingEvent(shipmentId, orderId, event, occurredAt, details));
        }

        public List<ShipmentTrackingEvent> findByShipmentId(String shipmentId) {
            return events.stream().filter(e -> e.shipmentId().equals(shipmentId)).toList();
        }
    }

    private static final Address ADDRESS = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");
    private static final String PASSWORD = DomainBusinessRulesTest.TEST_PASSWORD;

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
    private DomainBusinessRulesTest.FakeUserRepository userRepo;

    private ValidatePermissionsService permissions;
    private RegisterUserService userRegistration;
    private ManageUserService users;
    private ManageSellerService sellers;
    private RegisterBuyerService buyerRegistration;
    private ManageBuyerService buyers;
    private RegisterWarehouseService warehouseRegistration;
    private ManageWarehouseService warehouses;
    private ManageProductService products;
    private ChangeProductStatusService productStatus;
    private RegisterInventoryMovementService inventory;
    private ManageCartItemsService carts;
    private ConsultCartService cartQueries;
    private ConfirmOrderService checkout;
    private UpdateOrderStatusService orders;
    private ConsultOrderService orderQueries;
    private GenerateInvoiceService billing;
    private ConsultInvoiceService invoiceQueries;
    private CreateShipmentService shipments;
    private UpdateShipmentStatusService logistics;
    private ConsultShipmentService shipmentQueries;
    private RequestReturnService returnRequests;
    private ResolveReturnService returns;
    private ProcessRefundService refunds;
    private ConsultCommercialReportService reports;
    private ConsultSellerPerformanceReportService sellerReports;

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
        userRepo = new DomainBusinessRulesTest.FakeUserRepository();
        UserAccountService accounts = DomainBusinessRulesTest.newUserAccountService(userRepo);

        permissions = new ValidatePermissionsService(personRepo);
        ValidateOwnershipAccessService ownership = new ValidateOwnershipAccessService();
        InventoryReservationService reservation = new InventoryReservationService(inventoryRepo, movementRepo);
        userRegistration = new RegisterUserService(personRepo, permissions, notifications, accounts);
        users = new ManageUserService(personRepo, permissions, ownership, notifications);
        sellers = new ManageSellerService(sellerRepo, personRepo, permissions, ownership, notifications);
        buyerRegistration = new RegisterBuyerService(buyerRepo, personRepo, permissions, notifications, accounts);
        buyers = new ManageBuyerService(buyerRepo, personRepo, orderRepo, returnRepo,
                permissions, ownership, notifications);
        warehouseRegistration = new RegisterWarehouseService(warehouseRepo, sellerRepo, permissions, ownership);
        warehouses = new ManageWarehouseService(warehouseRepo, permissions, ownership);
        products = new ManageProductService(productRepo, permissions, ownership);
        productStatus = new ChangeProductStatusService(productRepo, permissions, ownership, notifications);
        inventory = new RegisterInventoryMovementService(inventoryRepo, movementRepo, productRepo,
                warehouseRepo, permissions, ownership);
        carts = new ManageCartItemsService(buyerRepo, cartRepo, productRepo, reservation, notifications);
        cartQueries = new ConsultCartService(buyerRepo, cartRepo);
        billing = new GenerateInvoiceService(orderRepo, permissions, notifications);
        invoiceQueries = new ConsultInvoiceService(orderRepo, permissions, ownership);
        checkout = new ConfirmOrderService(buyerRepo, cartRepo, orderRepo, billing, notifications);
        orders = new UpdateOrderStatusService(orderRepo, permissions, ownership, notifications);
        orderQueries = new ConsultOrderService(orderRepo, permissions, ownership);
        shipments = new CreateShipmentService(orderRepo, warehouseRepo, personRepo, trackingRepo,
                permissions, notifications);
        logistics = new UpdateShipmentStatusService(orderRepo, trackingRepo, reservation, permissions,
                ownership, notifications);
        shipmentQueries = new ConsultShipmentService(orderRepo, trackingRepo, permissions, ownership);
        returnRequests = new RequestReturnService(buyerRepo, orderRepo, returnRepo, notifications);
        returns = new ResolveReturnService(returnRepo, reservation, permissions, ownership, notifications);
        refunds = new ProcessRefundService(returnRepo, permissions, ownership, notifications);
        reports = new ConsultCommercialReportService(orderRepo, returnRepo, inventoryRepo, permissions);
        sellerReports = new ConsultSellerPerformanceReportService(orderRepo, returnRepo, productRepo,
                sellerRepo, permissions, ownership);

        admin = new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE);
        personRepo.save(admin);
        supervisor = (Supervisor) userRegistration.registerUser("admin-1", "sup-1", "Sam Supervisor", "sup@x.com",
                SystemRole.SUPERVISOR, PASSWORD);
        operator = (LogisticsOperator) userRegistration.registerUser("admin-1", "op-1", "Olga Operator", "op@x.com",
                SystemRole.LOGISTICS_OPERATOR, PASSWORD);
        seller = new Seller("seller-1", "TechStore", "seller@x.com", admin, UserStatus.ACTIVE);
        sellerRepo.save(seller);
        buyer = buyerRegistration.registerBuyer(null, "buyer-1", "Bruno Buyer", "buyer@x.com", ADDRESS, PASSWORD);
        otherBuyer = buyerRegistration.registerBuyer("admin-1", "buyer-2", "Berta Buyer", "buyer2@x.com",
                ADDRESS, PASSWORD);

        warehouseRegistration.registerWarehouse("admin-1", "wh-1", "Main Warehouse", ADDRESS, null);
        keyboard = new PhysicalProduct("product-1", "Keyboard", "RGB keyboard", new BigDecimal("50.00"),
                ProductStatus.PUBLISHED, seller);
        productRepo.save(keyboard);
        inventory.registerMovement("op-1", "product-1", "wh-1", InventoryMovementType.STOCK_IN, 10);
    }

    private Order paidOrder(int quantity) {
        carts.addItemToCart("buyer-1", null, "product-1", quantity);
        Order order = checkout.checkout("buyer-1", null);
        return orders.confirmPayment("admin-1", order.getOrderId());
    }

    private Inventory stock() {
        return inventoryRepo.findByProductIdAndWarehouseId("product-1", "wh-1").orElseThrow();
    }

    // ------------------------------------------------------------------ Authorization

    @Test
    void registrationCreatesAnAccessAccountWithAHashedPassword() {
        User account = userRepo.findByPersonId("buyer-1").orElseThrow();
        assertEquals(buyer, account.getPerson());
        assertNotEquals(PASSWORD, account.getPasswordHash());
        assertTrue(new DomainBusinessRulesTest.FakePasswordService().matches(PASSWORD, account.getPasswordHash()));
        assertTrue(userRepo.findByPersonId("sup-1").isPresent());

        // A weak password is rejected before the person is stored.
        assertThrows(WeakPasswordException.class,
                () -> buyerRegistration.registerBuyer(null, "buyer-3", "Weak", "weak@x.com", ADDRESS, "short1"));
        assertThrows(WeakPasswordException.class,
                () -> buyerRegistration.registerBuyer(null, "buyer-3", "Weak", "weak@x.com", ADDRESS, "onlyletters"));
        assertFalse(personRepo.existsByIdentifier("buyer-3"));
        assertTrue(userRepo.findByPersonId("buyer-3").isEmpty());
    }

    @Test
    void userAccountInvalidatesPreviousTokensWhenCredentialsChange() {
        User account = userRepo.findByPersonId("buyer-1").orElseThrow();
        int version = account.getAuthTokenVersion();
        assertTrue(account.acceptsTokenVersion(version));

        account.changePasswordHash("hashed:N3wPassword");
        assertFalse(account.acceptsTokenVersion(version));
        assertTrue(account.acceptsTokenVersion(version + 1));

        account.invalidateSessions();
        assertEquals(version + 2, account.getAuthTokenVersion());

        users.changeUserStatus("admin-1", "op-1", UserStatus.BLOCKED);
        User blocked = userRepo.findByPersonId("op-1").orElseThrow();
        assertThrows(IllegalStateException.class, blocked::requireCanAuthenticate);
    }

    @Test
    void missingEntitiesAreReportedAsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> orderQueries.consultOrder("admin-1", "missing-order"));
        assertThrows(EntityNotFoundException.class, () -> products.consultProduct("admin-1", "missing-product"));
    }

    @Test
    void permissionsFollowTheRoleMatrix() {
        assertTrue(permissions.hasPermission(admin, BusinessOperation.REGISTER_USER));
        assertFalse(permissions.hasPermission(supervisor, BusinessOperation.UPDATE_PRODUCT));
        assertFalse(permissions.hasPermission(buyer, BusinessOperation.CONSULT_COMMERCIAL_REPORT));
        assertTrue(permissions.hasPermission(seller, BusinessOperation.PUBLISH_PRODUCT));

        assertThrows(InvalidRoleAssignmentException.class,
                () -> permissions.requirePermission("buyer-1", BusinessOperation.CONFIRM_ORDER_PAYMENT));

        users.changeUserStatus("admin-1", "op-1", UserStatus.BLOCKED);
        assertFalse(permissions.hasPermission(operator, BusinessOperation.CREATE_SHIPMENT));
        assertThrows(IllegalStateException.class,
                () -> permissions.requirePermission("op-1", BusinessOperation.CREATE_SHIPMENT));
    }

    // ------------------------------------------------------------------ Users, sellers, buyers

    @Test
    void usersAreRegisteredByAdministratorsAndOnlyManageThemselves() {
        assertThrows(InvalidRoleAssignmentException.class,
                () -> userRegistration.registerUser("sup-1", "op-2", "Other", "op2@x.com",
                        SystemRole.LOGISTICS_OPERATOR, PASSWORD));
        assertThrows(IllegalArgumentException.class,
                () -> userRegistration.registerUser("admin-1", "seller-2", "S", "s2@x.com",
                        SystemRole.SELLER, PASSWORD));
        assertThrows(DuplicateEmailException.class,
                () -> userRegistration.registerUser("admin-1", "op-3", "Dup", "op@x.com",
                        SystemRole.LOGISTICS_OPERATOR, PASSWORD));

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
        assertThrows(application.domain.exceptions.BuyerNotAuthorizedException.class,
                () -> carts.addItemToCart("buyer-1", null, "product-1", 1));

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
        Warehouse own = warehouseRegistration.registerWarehouse("seller-1", "wh-s1", "Seller WH", ADDRESS, "seller-1");
        assertEquals("wh-s1", own.getIdentifier());
        assertThrows(InvalidRoleAssignmentException.class,
                () -> warehouseRegistration.registerWarehouse("seller-1", "wh-x", "Marketplace", ADDRESS, null));
        assertThrows(OwnershipAccessDeniedException.class,
                () -> warehouses.updateWarehouse("seller-1", "wh-1", "Hacked", ADDRESS));

        Warehouse renamed = warehouses.updateWarehouse("seller-1", "wh-s1", "Renamed", ADDRESS);
        assertEquals("Renamed", renamed.getName());
    }

    @Test
    void discontinuedProductsArePermanentAndHiddenFromTheCatalog() {
        Product updated = products.updateProduct("seller-1", "product-1", "Keyboard Pro", "New",
                new BigDecimal("55.00"), null);
        assertEquals("Keyboard Pro", updated.getName());
        assertEquals(new BigDecimal("55.00"), updated.getPrice());

        productStatus.changeProductStatus("seller-1", "product-1", ProductStatus.SUSPENDED);
        assertThrows(IllegalArgumentException.class, () -> products.consultProduct(null, "product-1"));
        assertEquals("product-1", products.consultProduct("seller-1", "product-1").getIdentifier());

        productStatus.changeProductStatus("admin-1", "product-1", ProductStatus.DISCONTINUED);
        assertThrows(IllegalArgumentException.class,
                () -> productStatus.changeProductStatus("seller-1", "product-1", ProductStatus.PUBLISHED));
        assertThrows(IllegalArgumentException.class,
                () -> products.updateProduct("seller-1", "product-1", "Again", null, null, null));
    }

    // ------------------------------------------------------------------ Inventory and cart

    @Test
    void damagedStockCannotBeReservedAndStockNeverGoesNegative() {
        inventory.registerDamagedStock("op-1", "product-1", "wh-1", 8);
        Inventory record = stock();
        assertEquals(2, record.getAvailableQuantity());
        assertEquals(8, record.getDamagedQuantity());

        // Reservations are recorded only by the shopping cart, never by hand.
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "product-1", "wh-1", InventoryMovementType.RESERVATION, 3));
        // Damaged stock is still never reservable (rule checked on the Inventory entity).
        assertThrows(InsufficientInventoryException.class, () -> stock().reserve(3, admin));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "product-1", "wh-1", InventoryMovementType.ADJUSTMENT, -5));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "product-1", "wh-1", InventoryMovementType.SALE_EXIT, 1));

        DigitalProduct ebook = new DigitalProduct("ebook-1", "Ebook", null, new BigDecimal("10.00"),
                ProductStatus.PUBLISHED, seller, "link");
        productRepo.save(ebook);
        assertThrows(IllegalArgumentException.class,
                () -> inventory.registerMovement("admin-1", "ebook-1", "wh-1", InventoryMovementType.STOCK_IN, 1));
    }

    @Test
    void cartChangesReserveAndReleaseStock() {
        carts.addItemToCart("buyer-1", null, "product-1", 4);
        assertEquals(6, stock().getAvailableQuantity());

        carts.updateItemQuantity("buyer-1", "product-1", 2);
        assertEquals(8, stock().getAvailableQuantity());
        assertEquals(2, stock().getReservedQuantity());

        ShoppingCart cart = carts.removeItem("buyer-1", "product-1");
        assertTrue(cart.isEmpty());
        assertEquals(10, stock().getAvailableQuantity());
        assertEquals(0, stock().getReservedQuantity());
        assertTrue(cartQueries.consultCart("buyer-2").isEmpty());
    }

    // ------------------------------------------------------------------ Order lifecycle, logistics, billing

    @Test
    void aFailedDispatchLeavesTheOrderAndShipmentUntouched() {
        Order order = paidOrder(2);
        String orderId = order.getOrderId();
        shipments.createShipment("admin-1", orderId, "wh-1", "op-1");
        // The reservation of the order is lost (no longer possible through the API).
        stock().releaseReservation(2, admin);

        assertThrows(IllegalStateException.class, () -> logistics.dispatchShipment("op-1", orderId));
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertEquals(ShipmentStatus.PENDING,
                shipmentQueries.consultShipment("buyer-1", orderId).getShipmentStatus());
        assertEquals(10, stock().getAvailableQuantity());
        assertEquals(0, stock().getReservedQuantity());
    }

    @Test
    void fullOrderLifecycleThroughLogisticsAndFinalization() {
        Order order = paidOrder(3);
        String orderId = order.getOrderId();
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertNotNull(invoiceQueries.consultInvoice("buyer-1", orderId));
        assertThrows(IllegalArgumentException.class, () -> billing.generateInvoice("admin-1", orderId));
        assertThrows(OwnershipAccessDeniedException.class, () -> orderQueries.consultOrder("buyer-2", orderId));
        assertEquals(orderId, orderQueries.consultOrder("seller-1", orderId).getOrderId());

        shipments.createShipment("admin-1", orderId, "wh-1", "op-1");
        assertThrows(IllegalArgumentException.class, () -> orders.finalizeOrder("admin-1", orderId));

        logistics.dispatchShipment("op-1", orderId);
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());
        assertEquals(0, stock().getReservedQuantity());
        assertEquals(7, stock().getAvailableQuantity());

        logistics.confirmDelivery("op-1", orderId);
        assertEquals(ShipmentStatus.DELIVERED, shipmentQueries.consultShipment("buyer-1", orderId).getShipmentStatus());
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());

        orders.finalizeOrder("op-1", orderId);
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
        assertThrows(OrderAlreadyFinalizedException.class, () -> orders.confirmPayment("admin-1", orderId));

        List<ShipmentTrackingEvent> tracking = shipmentQueries.consultTracking("buyer-1", orderId);
        assertEquals(List.of(TrackingEventType.CREATED, TrackingEventType.DISPATCHED, TrackingEventType.DELIVERED),
                tracking.stream().map(ShipmentTrackingEvent::event).toList());
        assertEquals(1, buyers.consultBuyerOrders("buyer-1", "buyer-1").size());
    }

    @Test
    void digitalOnlyOrdersAreDeliveredUponPaymentConfirmation() {
        DigitalProduct ebook = new DigitalProduct("ebook-1", "Ebook", null, new BigDecimal("10.00"),
                ProductStatus.PUBLISHED, seller, "link");
        productRepo.save(ebook);
        carts.addItemToCart("buyer-1", null, "ebook-1", 1);
        Order order = checkout.checkout("buyer-1", null);

        orders.confirmPayment("admin-1", order.getOrderId());
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
        assertThrows(IllegalArgumentException.class,
                () -> shipments.createShipment("admin-1", order.getOrderId(), "wh-1", "op-1"));
    }

    // ------------------------------------------------------------------ Returns, refunds and reports

    @Test
    void returnsAreResolvedAndRefundsConsultedByTheirOwners() {
        Order order = paidOrder(2);
        String orderId = order.getOrderId();
        shipments.createShipment("op-1", orderId, "wh-1", "op-1");
        logistics.dispatchShipment("op-1", orderId);
        logistics.confirmDelivery("op-1", orderId);
        orders.finalizeOrder("admin-1", orderId);

        Return rejected = returnRequests.requestReturn("buyer-1", orderId, "changed my mind");
        returns.rejectReturn("admin-1", rejected.getReturnId());
        assertEquals(ReturnStatus.REJECTED, returns.consultReturn("buyer-1", rejected.getReturnId()).getReturnStatus());

        Return approved = returnRequests.requestReturn("buyer-1", orderId, "damaged box");
        assertThrows(ReturnNotAllowedException.class,
                () -> returnRequests.requestReturn("buyer-1", orderId, "duplicate request"));
        returns.approveReturn("admin-1", approved.getReturnId());
        assertThrows(ReturnNotAllowedException.class,
                () -> returnRequests.requestReturn("buyer-1", orderId, "duplicate request"));
        assertThrows(IllegalArgumentException.class,
                () -> refunds.processRefund("admin-1", approved.getReturnId(), new BigDecimal("1000")));
        assertThrows(InvalidRoleAssignmentException.class,
                () -> refunds.processRefund("sup-1", approved.getReturnId(), new BigDecimal("40.00")));
        refunds.processRefund("admin-1", approved.getReturnId(), new BigDecimal("40.00"));

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

        SellerPerformanceReport performance = sellerReports.consultSellerPerformanceReport("seller-1", "seller-1");
        assertEquals(2, performance.unitsSold());
        assertEquals(2, performance.returnsCount());
        assertThrows(InvalidRoleAssignmentException.class,
                () -> reports.consultCommercialReport("seller-1"));
    }
}
