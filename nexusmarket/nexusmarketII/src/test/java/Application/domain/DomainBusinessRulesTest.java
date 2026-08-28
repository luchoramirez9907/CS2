package Application.domain;

import Application.domain.models.Administrator;
import Application.domain.models.Buyer;
import Application.domain.models.Inventory;
import Application.domain.models.InventoryMovement;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.models.Seller;
import Application.domain.models.ShoppingCart;
import Application.domain.models.Supervisor;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.InventoryMovementRepository;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.ReturnRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.ports.out.ShoppingCartRepository;
import Application.domain.services.InventoryReservationService;
import Application.domain.services.OrderCheckoutService;
import Application.domain.services.ProductCatalogService;
import Application.domain.services.RefundProcessingService;
import Application.domain.services.ReturnManagementService;
import Application.domain.services.SellerRegistrationService;
import Application.domain.services.ShoppingCartService;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;
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
 * Pure domain tests: validate the SDD business rules without any
 * infrastructure (constraint 10 - the Domain must be fully testable
 * without requiring infrastructure components).
 */
class DomainBusinessRulesTest {

    // ------------------------------------------------------------------
    // In-memory fakes of the output ports (single shared person store,
    // mirroring the single persons table of the relational model)
    // ------------------------------------------------------------------

    static class FakePersonRepository implements PersonRepository {
        final Map<String, Person> store;

        FakePersonRepository(Map<String, Person> store) { this.store = store; }

        public void save(Person person) { store.put(person.getIdentifier(), person); }

        public Optional<Person> findById(String id) { return Optional.ofNullable(store.get(id)); }

        public boolean existsByIdentifier(String id) { return store.containsKey(id); }

        public boolean existsByEmail(String email) {
            return store.values().stream().anyMatch(p -> p.getEmail().equalsIgnoreCase(email));
        }
    }

    static class FakeBuyerRepository implements BuyerRepository {
        private final Map<String, Person> shared;

        FakeBuyerRepository(Map<String, Person> shared) { this.shared = shared; }

        public void save(Buyer buyer) { shared.put(buyer.getIdentifier(), buyer); }

        public Optional<Buyer> findById(String id) {
            return Optional.ofNullable(shared.get(id))
                    .filter(Buyer.class::isInstance)
                    .map(Buyer.class::cast);
        }
    }

    static class FakeSellerRepository implements SellerRepository {
        private final Map<String, Person> shared;

        FakeSellerRepository(Map<String, Person> shared) { this.shared = shared; }

        public void save(Seller seller) { shared.put(seller.getIdentifier(), seller); }

        public Optional<Seller> findById(String id) {
            return Optional.ofNullable(shared.get(id))
                    .filter(Seller.class::isInstance)
                    .map(Seller.class::cast);
        }
    }

    static class FakeProductRepository implements ProductRepository {
        final Map<String, Product> store = new HashMap<>();

        public void save(Product product) { store.put(product.getIdentifier(), product); }

        public Optional<Product> findById(String id) { return Optional.ofNullable(store.get(id)); }
    }

    static class FakeInventoryRepository implements InventoryRepository {
        final Map<String, Inventory> store = new HashMap<>();

        public void save(Inventory inventory) { store.put(inventory.getIdentifier(), inventory); }

        public Optional<Inventory> findById(String id) { return Optional.ofNullable(store.get(id)); }

        public List<Inventory> findByProductId(String productId) {
            return store.values().stream()
                    .filter(i -> i.getProduct().getIdentifier().equals(productId))
                    .toList();
        }
    }

    static class FakeMovementRepository implements InventoryMovementRepository {
        final List<InventoryMovement> movements = new ArrayList<>();

        public void save(InventoryMovement movement) { movements.add(movement); }
    }

    static class FakeCartRepository implements ShoppingCartRepository {
        final Map<String, ShoppingCart> store = new HashMap<>();

        public void save(ShoppingCart cart) { store.put(cart.getCartId(), cart); }

        public Optional<ShoppingCart> findById(String cartId) { return Optional.ofNullable(store.get(cartId)); }

        public Optional<ShoppingCart> findActiveByBuyerId(String buyerId) {
            return store.values().stream()
                    .filter(c -> c.getBuyer().getIdentifier().equals(buyerId))
                    .findFirst();
        }

        public void delete(ShoppingCart cart) { store.remove(cart.getCartId()); }
    }

    static class FakeOrderRepository implements OrderRepository {
        final Map<String, Order> store = new HashMap<>();

        public void save(Order order) { store.put(order.getOrderId(), order); }

        public Optional<Order> findById(String orderId) { return Optional.ofNullable(store.get(orderId)); }
    }

    static class FakeReturnRepository implements ReturnRepository {
        final Map<String, Return> store = new HashMap<>();

        public void save(Return returnRequest) { store.put(returnRequest.getReturnId(), returnRequest); }

        public Optional<Return> findById(String returnId) { return Optional.ofNullable(store.get(returnId)); }
    }

    static class FakeNotifications implements NotificationService {
        public void notify(Person recipient, String subject, String message) {
            // no-op
        }
    }

    // ------------------------------------------------------------------
    // Test world
    // ------------------------------------------------------------------
    private Administrator admin;
    private Supervisor supervisor;
    private Buyer buyer;
    private FakePersonRepository personRepo;
    private FakeBuyerRepository buyerRepo;
    private FakeSellerRepository sellerRepo;
    private FakeProductRepository productRepo;
    private FakeInventoryRepository inventoryRepo;
    private FakeMovementRepository movementRepo;
    private FakeCartRepository cartRepo;
    private FakeOrderRepository orderRepo;
    private FakeReturnRepository returnRepo;
    private SellerRegistrationService sellerRegistration;
    private ProductCatalogService productCatalog;
    private ShoppingCartService cartService;
    private OrderCheckoutService checkoutService;
    private ReturnManagementService returnManagement;
    private RefundProcessingService refundProcessing;

    @BeforeEach
    void setUp() {
        Map<String, Person> personStore = new HashMap<>();
        personRepo = new FakePersonRepository(personStore);
        buyerRepo = new FakeBuyerRepository(personStore);
        sellerRepo = new FakeSellerRepository(personStore);
        productRepo = new FakeProductRepository();
        inventoryRepo = new FakeInventoryRepository();
        movementRepo = new FakeMovementRepository();
        cartRepo = new FakeCartRepository();
        orderRepo = new FakeOrderRepository();
        returnRepo = new FakeReturnRepository();

        InventoryReservationService reservationService =
                new InventoryReservationService(inventoryRepo, movementRepo);
        sellerRegistration = new SellerRegistrationService(personRepo, sellerRepo, new FakeNotifications());
        productCatalog = new ProductCatalogService(sellerRepo, productRepo, new FakeNotifications());
        cartService = new ShoppingCartService(buyerRepo, cartRepo, productRepo,
                reservationService, new FakeNotifications());
        checkoutService = new OrderCheckoutService(buyerRepo, cartRepo, orderRepo, new FakeNotifications());
        returnManagement = new ReturnManagementService(buyerRepo, orderRepo, returnRepo,
                personRepo, inventoryRepo, reservationService, new FakeNotifications());
        refundProcessing = new RefundProcessingService(returnRepo, personRepo, new FakeNotifications());

        admin = new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE);
        supervisor = new Supervisor("sup-1", "Sam Supervisor", "sup@x.com", UserStatus.ACTIVE);
        buyer = new Buyer("buyer-1", "Bruno Buyer", "buyer@x.com",
                new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111"),
                BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        personRepo.save(admin);
        personRepo.save(supervisor);
        buyerRepo.save(buyer);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private Seller newSeller() {
        Seller seller = new Seller("seller-1", "TechStore", "seller@x.com", admin, UserStatus.ACTIVE);
        personRepo.save(seller);
        sellerRepo.save(seller);
        return seller;
    }

    private Product publishedPhysicalProduct() {
        return new Application.domain.models.PhysicalProduct("product-1", "Keyboard", "RGB keyboard",
                ProductStatus.PUBLISHED, newSeller());
    }

    private Application.domain.models.MarketplaceWarehouse warehouse() {
        return new Application.domain.models.MarketplaceWarehouse("wh-1", "Main Warehouse",
                new Address("Calle 2", "Bogota", "CUND", "Colombia", "110111"), admin);
    }

    private Order confirmedOrder() {
        Product product = publishedPhysicalProduct();
        productRepo.save(product);
        Inventory inventory = new Inventory("inv-1", product, warehouse(), 0, 0);
        inventory.stockIn(50, admin);
        inventoryRepo.save(inventory);

        cartService.addItemToCart(buyer.getIdentifier(), null, product.getIdentifier(),
                2, new BigDecimal("99.90"));
        Order order = checkoutService.checkout(buyer.getIdentifier(), null);
        orderRepo.save(order);
        return order;
    }

    // ------------------------------------------------------------------
    // Inventory rules
    // ------------------------------------------------------------------

    @Test
    void insufficientStockIsRejected() {
        Inventory inventory = new Inventory("inv-1", publishedPhysicalProduct(), warehouse(), 10, 0);
        inventoryRepo.save(inventory);

        assertThrows(Application.domain.exceptions.InsufficientInventoryException.class,
                () -> inventory.reserve(11, admin));
        assertEquals(10, inventory.getAvailableQuantity());
    }

    @Test
    void everyStockChangeGeneratesMovement() {
        Inventory inventory = new Inventory("inv-1", publishedPhysicalProduct(), warehouse(), 0, 0);
        inventory.stockIn(10, admin);
        inventory.reserve(4, admin);
        assertThrows(IllegalArgumentException.class, () -> inventory.adjust(-100, admin));
        inventory.confirmSale(4, admin);
        inventory.reinstate(2, admin);

        assertEquals(8, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(4, inventory.getMovements().size());
        assertEquals(Application.domain.valueobjects.InventoryMovementType.STOCK_IN,
                inventory.getMovements().get(0).getMovementType());
        assertEquals(Application.domain.valueobjects.InventoryMovementType.RETURN,
                inventory.getMovements().get(3).getMovementType());
    }

    // ------------------------------------------------------------------
    // Order lifecycle rules
    // ------------------------------------------------------------------

    @Test
    void orderLifecycleIsControlledAndFinalizedOrdersAreImmutable() {
        Order order = confirmedOrder();
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getOrderStatus());
        assertNotNull(order.getInvoice());
        assertEquals(new BigDecimal("199.80"), order.getTotalAmount());
        assertEquals(2, movementRepo.movements.size());
        assertEquals(Application.domain.valueobjects.InventoryMovementType.RESERVATION,
                movementRepo.movements.get(1).getMovementType());

        assertThrows(IllegalArgumentException.class, order::markShipped);

        order.markPaid();
        order.markShipped();
        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());

        assertThrows(Application.domain.exceptions.OrderAlreadyFinalizedException.class, order::markPaid);
    }

    // ------------------------------------------------------------------
    // Post-sale rules
    // ------------------------------------------------------------------

    @Test
    void returnsOnlyAllowedOnDeliveredOrders() {
        Order order = confirmedOrder();
        assertThrows(Application.domain.exceptions.ReturnNotAllowedException.class,
                () -> Application.domain.models.Return.request(order, buyer, "damaged",
                        "ret-1", LocalDateTime.now()));

        order.markPaid();
        order.markShipped();
        order.markDelivered();
        Return returnRequest = Application.domain.models.Return.request(order, buyer, "damaged",
                "ret-1", LocalDateTime.now());
        assertEquals(Application.domain.enums.ReturnStatus.REQUESTED, returnRequest.getReturnStatus());
    }

    @Test
    void refundsRequireApprovedReturnAndAuthorizedApprover() {
        Order order = confirmedOrder();
        order.markPaid();
        order.markShipped();
        order.markDelivered();
        Return returnRequest = Application.domain.models.Return.request(order, buyer, "damaged",
                "ret-1", LocalDateTime.now());

        assertThrows(Application.domain.exceptions.RefundNotAllowedException.class,
                () -> Application.domain.models.Refund.issueFor(returnRequest, order.getTotalAmount(),
                        admin, "ref-1", LocalDateTime.now()));

        returnRequest.approve(admin);
        assertThrows(Application.domain.exceptions.InvalidRoleAssignmentException.class,
                () -> Application.domain.models.Refund.issueFor(returnRequest, order.getTotalAmount(),
                        buyer, "ref-2", LocalDateTime.now()));

        Refund refund = Application.domain.models.Refund.issueFor(returnRequest, order.getTotalAmount(),
                supervisor, "ref-2", LocalDateTime.now());
        refund.process();
        assertEquals(Application.domain.enums.RefundStatus.PROCESSED, refund.getRefundStatus());
        assertNotNull(returnRequest.getRefund());
    }

    // ------------------------------------------------------------------
    // Person and registration rules
    // ------------------------------------------------------------------

    @Test
    void sellerSelfRegistrationIsBlockedAndEmailsAreUnique() {
        Seller self = newSeller();
        assertThrows(Application.domain.exceptions.SellerSelfRegistrationNotAllowedException.class,
                () -> sellerRegistration.registerSeller(self.getIdentifier(), "seller-2",
                        "Other Seller", "seller2@x.com"));

        Seller registered = sellerRegistration.registerSeller(admin.getIdentifier(), "seller-2",
                "Other Seller", "seller2@x.com");
        assertEquals(SystemRole.SELLER.getCode(), registered.getRole().getCode());

        assertThrows(Application.domain.exceptions.DuplicateEmailException.class,
                () -> sellerRegistration.registerSeller(admin.getIdentifier(), "seller-3",
                        "Another Seller", "seller2@x.com"));
    }

    // ------------------------------------------------------------------
    // Value object semantics
    // ------------------------------------------------------------------

    @Test
    void valueObjectsAreComparedByValue() {
        Address first = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");
        Address second = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");
        assertEquals(first, second);

        assertEquals(UserStatus.ACTIVE, Application.domain.valueobjects.UserStatus.fromCode("ACTIVE"));

        assertTrue(OrderStatus.CART.canTransitionTo(OrderStatus.PENDING_PAYMENT));
        assertFalse(OrderStatus.CART.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.DELIVERED));
    }

    // ------------------------------------------------------------------
    // Full happy path through the use cases
    // ------------------------------------------------------------------

    @Test
    void fullPurchaseHappyPathThroughUseCases() {
        Order order = confirmedOrder();
        assertTrue(cartRepo.store.isEmpty());

        order.markPaid();
        order.markShipped();
        order.markDelivered();
        orderRepo.save(order);

        Return returnRequest = returnManagement.requestReturn(buyer.getIdentifier(),
                order.getOrderId(), "damaged box");
        assertEquals(Application.domain.enums.ReturnStatus.REQUESTED, returnRequest.getReturnStatus());

        assertThrows(Application.domain.exceptions.InvalidRoleAssignmentException.class,
                () -> returnManagement.approveReturn(buyer.getIdentifier(), returnRequest.getReturnId()));

        returnManagement.approveReturn(admin.getIdentifier(), returnRequest.getReturnId());
        assertEquals(Application.domain.enums.ReturnStatus.APPROVED, returnRequest.getReturnStatus());

        Refund refund = refundProcessing.processRefund(supervisor.getIdentifier(),
                returnRequest.getReturnId(), null);
        assertEquals(order.getTotalAmount(), refund.getAmount());
        assertEquals(Application.domain.enums.RefundStatus.PROCESSED, refund.getRefundStatus());
        assertTrue(buyer.getOrders().stream().anyMatch(o -> o.getOrderId().equals(order.getOrderId())));
    }
}


