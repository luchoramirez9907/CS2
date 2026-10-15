package application.domain;

import application.domain.models.Administrator;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.ShoppingCart;
import application.domain.models.Supervisor;
import application.domain.models.User;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.InventoryMovementRepository;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.PasswordService;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.ReturnRepository;
import application.domain.ports.out.SellerRepository;
import application.domain.ports.out.ShoppingCartRepository;
import application.domain.ports.out.UserRepository;
import application.domain.services.authentication.UserAccountService;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.services.billing.GenerateInvoiceService;
import application.domain.services.cart.ManageCartItemsService;
import application.domain.services.catalog.PublishProductService;
import application.domain.services.inventory.InventoryReservationService;
import application.domain.services.order.ConfirmOrderService;
import application.domain.services.returns.ProcessRefundService;
import application.domain.services.returns.RequestReturnService;
import application.domain.services.returns.ResolveReturnService;
import application.domain.services.seller.RegisterSellerService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
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
 * Pure domain tests: validate the SDD business rules without any
 * infrastructure (constraint 10 - the Domain must be fully testable
 * without requiring infrastructure components).
 */
class DomainBusinessRulesTest {

    /** Test-only password that satisfies the password policy. */
    static final String TEST_PASSWORD = "Passw0rd1";

    // ------------------------------------------------------------------
    // In-memory fakes of the output ports (single shared person store,
    // mirroring the single persons table of the relational model)
    // ------------------------------------------------------------------

    static class FakePersonRepository implements PersonRepository {
        final Map<String, Person> store;

        FakePersonRepository(Map<String, Person> store) { this.store = store; }

        public List<Person> findAll() { return new ArrayList<>(store.values()); }

        public void save(Person person) { store.put(person.getIdentifier(), person); }

        public Optional<Person> findById(String id) { return Optional.ofNullable(store.get(id)); }

        public Optional<Person> findByEmail(String email) {
            return store.values().stream().filter(p -> p.getEmail().equalsIgnoreCase(email)).findFirst();
        }

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

        public List<Product> findAll() { return new ArrayList<>(store.values()); }

        public void save(Product product) { store.put(product.getIdentifier(), product); }

        public Optional<Product> findById(String id) { return Optional.ofNullable(store.get(id)); }

        public List<Product> findBySellerId(String sellerId) {
            return store.values().stream()
                    .filter(p -> p.getSeller().getIdentifier().equals(sellerId))
                    .toList();
        }
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

        public Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId) {
            return store.values().stream()
                    .filter(i -> i.getProduct().getIdentifier().equals(productId)
                            && i.getWarehouse().getIdentifier().equals(warehouseId))
                    .findFirst();
        }

        public List<Inventory> findAll() { return List.copyOf(store.values()); }
    }

    static class FakeMovementRepository implements InventoryMovementRepository {
        final List<InventoryMovement> movements = new ArrayList<>();

        public void save(InventoryMovement movement) { movements.add(movement); }

        public List<InventoryMovement> findByInventory(Inventory inventory) {
            return movements.stream()
                    .filter(m -> m.getInventory().getIdentifier().equals(inventory.getIdentifier()))
                    .toList();
        }
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

        public List<Order> findByBuyerId(String buyerId) {
            return store.values().stream()
                    .filter(o -> o.getBuyer().getIdentifier().equals(buyerId))
                    .toList();
        }

        public List<Order> findAll() { return List.copyOf(store.values()); }
    }

    static class FakeReturnRepository implements ReturnRepository {
        final Map<String, Return> store = new HashMap<>();

        public void save(Return returnRequest) { store.put(returnRequest.getReturnId(), returnRequest); }

        public Optional<Return> findById(String returnId) { return Optional.ofNullable(store.get(returnId)); }

        public List<Return> findByBuyerId(String buyerId) {
            return store.values().stream()
                    .filter(r -> r.getBuyer().getIdentifier().equals(buyerId))
                    .toList();
        }

        public List<Return> findAll() { return List.copyOf(store.values()); }
    }

    static class FakeNotifications implements NotificationService {
        public void notify(Person recipient, String subject, String message) {
            // no-op
        }
    }

    static class FakeUserRepository implements UserRepository {
        final Map<String, User> store = new HashMap<>();

        public void save(User user) { store.put(user.getIdentifier(), user); }

        public Optional<User> findByPersonId(String personIdentifier) {
            return Optional.ofNullable(store.get(personIdentifier));
        }

        public Optional<User> findByEmail(String email) {
            return store.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }
    }

    /** Deterministic, test-only stand-in for the BCrypt adapter. */
    static class FakePasswordService implements PasswordService {
        public String hash(String rawPassword) { return "hashed:" + rawPassword; }

        public boolean matches(String rawPassword, String passwordHash) {
            return hash(rawPassword).equals(passwordHash);
        }
    }

    static UserAccountService newUserAccountService(UserRepository userRepository) {
        return new UserAccountService(userRepository, new FakePasswordService());
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
    private FakeUserRepository userRepo;
    private RegisterSellerService sellerRegistration;
    private PublishProductService productCatalog;
    private ManageCartItemsService cartService;
    private ConfirmOrderService checkoutService;
    private RequestReturnService returnRequests;
    private ResolveReturnService returnResolution;
    private ProcessRefundService refundProcessing;

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
        userRepo = new FakeUserRepository();
        sellerRegistration = new RegisterSellerService(personRepo, sellerRepo, new FakeNotifications(),
                newUserAccountService(userRepo));
        productCatalog = new PublishProductService(sellerRepo, productRepo, new FakeNotifications());
        cartService = new ManageCartItemsService(buyerRepo, cartRepo, productRepo,
                reservationService, new FakeNotifications());
        ValidatePermissionsService permissions = new ValidatePermissionsService(personRepo);
        ValidateOwnershipAccessService ownership = new ValidateOwnershipAccessService();
        GenerateInvoiceService billing = new GenerateInvoiceService(orderRepo, permissions, new FakeNotifications());
        checkoutService = new ConfirmOrderService(buyerRepo, cartRepo, orderRepo, billing,
                new FakeNotifications());
        returnRequests = new RequestReturnService(buyerRepo, orderRepo, returnRepo, new FakeNotifications());
        returnResolution = new ResolveReturnService(returnRepo, reservationService, permissions, ownership,
                new FakeNotifications());
        refundProcessing = new ProcessRefundService(returnRepo, permissions, ownership, new FakeNotifications());

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
        return new application.domain.models.PhysicalProduct("product-1", "Keyboard", "RGB keyboard",
                new BigDecimal("99.90"),
                ProductStatus.PUBLISHED, newSeller());
    }

    private application.domain.models.MarketplaceWarehouse warehouse() {
        return new application.domain.models.MarketplaceWarehouse("wh-1", "Main Warehouse",
                new Address("Calle 2", "Bogota", "CUND", "Colombia", "110111"), admin);
    }

    private Order confirmedOrder() {
        Product product = publishedPhysicalProduct();
        productRepo.save(product);
        Inventory inventory = new Inventory("inv-1", product, warehouse(), 0, 0);
        inventory.stockIn(50, admin);
        inventoryRepo.save(inventory);

        cartService.addItemToCart(buyer.getIdentifier(), null, product.getIdentifier(), 2);
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

        assertThrows(application.domain.exceptions.InsufficientInventoryException.class,
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
        assertEquals(application.domain.valueobjects.InventoryMovementType.STOCK_IN,
                inventory.getMovements().get(0).getMovementType());
        assertEquals(application.domain.valueobjects.InventoryMovementType.RETURN,
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
        // VAT 19% is contained in the price: 199.80 - 199.80 / 1.19 = 31.90
        assertEquals(new BigDecimal("199.80"), order.getInvoice().getTotalAmount());
        assertEquals(new BigDecimal("31.90"), order.getInvoice().getTaxAmount());
        assertEquals(2, movementRepo.movements.size());
        assertEquals(application.domain.valueobjects.InventoryMovementType.RESERVATION,
                movementRepo.movements.get(1).getMovementType());

        assertThrows(IllegalArgumentException.class, order::markShipped);

        order.markPaid();
        order.markShipped();
        order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());

        assertThrows(application.domain.exceptions.OrderAlreadyFinalizedException.class, order::markPaid);
    }

    // ------------------------------------------------------------------
    // Post-sale rules
    // ------------------------------------------------------------------

    @Test
    void returnsOnlyAllowedOnDeliveredOrders() {
        Order order = confirmedOrder();
        assertThrows(application.domain.exceptions.ReturnNotAllowedException.class,
                () -> application.domain.models.Return.request(order, buyer, "damaged",
                        "ret-1", LocalDateTime.now()));

        order.markPaid();
        order.markShipped();
        order.markDelivered();
        Return returnRequest = application.domain.models.Return.request(order, buyer, "damaged",
                "ret-1", LocalDateTime.now());
        assertEquals(application.domain.enums.ReturnStatus.REQUESTED, returnRequest.getReturnStatus());
    }

    @Test
    void refundsRequireApprovedReturnAndAuthorizedApprover() {
        Order order = confirmedOrder();
        order.markPaid();
        order.markShipped();
        order.markDelivered();
        Return returnRequest = application.domain.models.Return.request(order, buyer, "damaged",
                "ret-1", LocalDateTime.now());

        assertThrows(application.domain.exceptions.RefundNotAllowedException.class,
                () -> application.domain.models.Refund.issueFor(returnRequest, order.getTotalAmount(),
                        admin, "ref-1", LocalDateTime.now()));

        // Only an Administrator can approve a refund: Refund.approvedBy is typed as Administrator.
        returnRequest.approve(admin);
        Refund refund = application.domain.models.Refund.issueFor(returnRequest, order.getTotalAmount(),
                admin, "ref-2", LocalDateTime.now());
        refund.process();
        assertEquals(application.domain.enums.RefundStatus.PROCESSED, refund.getRefundStatus());
        assertNotNull(returnRequest.getRefund());
    }

    // ------------------------------------------------------------------
    // Person and registration rules
    // ------------------------------------------------------------------

    @Test
    void sellerSelfRegistrationIsBlockedAndEmailsAreUnique() {
        Seller self = newSeller();
        assertThrows(application.domain.exceptions.SellerSelfRegistrationNotAllowedException.class,
                () -> sellerRegistration.registerSeller(self.getIdentifier(), "seller-2",
                        "Other Seller", "seller2@x.com", TEST_PASSWORD));

        Seller registered = sellerRegistration.registerSeller(admin.getIdentifier(), "seller-2",
                "Other Seller", "seller2@x.com", TEST_PASSWORD);
        assertEquals(SystemRole.SELLER.getCode(), registered.getRole().getCode());

        assertThrows(application.domain.exceptions.DuplicateEmailException.class,
                () -> sellerRegistration.registerSeller(admin.getIdentifier(), "seller-3",
                        "Another Seller", "seller2@x.com", TEST_PASSWORD));
    }

    // ------------------------------------------------------------------
    // Value object semantics
    // ------------------------------------------------------------------

    @Test
    void valueObjectsAreComparedByValue() {
        Address first = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");
        Address second = new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111");
        assertEquals(first, second);

        assertEquals(UserStatus.ACTIVE, application.domain.valueobjects.UserStatus.fromCode("ACTIVE"));

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

        Return returnRequest = returnRequests.requestReturn(buyer.getIdentifier(),
                order.getOrderId(), "damaged box");
        assertEquals(application.domain.enums.ReturnStatus.REQUESTED, returnRequest.getReturnStatus());

        assertThrows(application.domain.exceptions.InvalidRoleAssignmentException.class,
                () -> returnResolution.approveReturn(buyer.getIdentifier(), returnRequest.getReturnId()));

        returnResolution.approveReturn(admin.getIdentifier(), returnRequest.getReturnId());
        assertEquals(application.domain.enums.ReturnStatus.APPROVED, returnRequest.getReturnStatus());

        // Supervisors are read-only: they cannot process refunds.
        assertThrows(application.domain.exceptions.InvalidRoleAssignmentException.class,
                () -> refundProcessing.processRefund(supervisor.getIdentifier(),
                        returnRequest.getReturnId(), null));

        Refund refund = refundProcessing.processRefund(admin.getIdentifier(),
                returnRequest.getReturnId(), null);
        assertEquals(order.getTotalAmount(), refund.getAmount());
        assertEquals(application.domain.enums.RefundStatus.PROCESSED, refund.getRefundStatus());
        assertTrue(buyer.getOrders().stream().anyMatch(o -> o.getOrderId().equals(order.getOrderId())));
    }
}


