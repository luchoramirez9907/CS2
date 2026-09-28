package Application.infrastructure.config;

import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.InventoryMovementRepository;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.OrderRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.ReturnRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.ports.out.ShipmentTrackingRepository;
import Application.domain.ports.out.ShoppingCartRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.services.AuthorizationService;
import Application.domain.services.BillingService;
import Application.domain.services.BuyerManagementService;
import Application.domain.services.InventoryManagementService;
import Application.domain.services.InventoryReservationService;
import Application.domain.services.OrderCheckoutService;
import Application.domain.services.OrderManagementService;
import Application.domain.services.ProductCatalogService;
import Application.domain.services.ProductManagementService;
import Application.domain.services.RefundProcessingService;
import Application.domain.services.ReportingService;
import Application.domain.services.ReturnManagementService;
import Application.domain.services.SellerManagementService;
import Application.domain.services.SellerRegistrationService;
import Application.domain.services.ShipmentDispatchService;
import Application.domain.services.ShoppingCartService;
import Application.domain.services.UserManagementService;
import Application.domain.services.WarehouseManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dependency composition for the pure domain services. The domain never
 * depends on Spring: it is wired here, at the application core (per SDD -
 * App responsibilities: configure dependency injection).
 *
 * Beans are grouped by subdomain, following the service catalog
 * (SDD/domain/Services.md).
 */
@Configuration
public class DomainBeanConfig {

    // ---------------------------------------------------------------- Authorization

    @Bean
    public AuthorizationService authorizationService(PersonRepository personRepository) {
        return new AuthorizationService(personRepository);
    }

    // ---------------------------------------------------------------- User Management

    @Bean
    public UserManagementService userManagementService(
            PersonRepository personRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new UserManagementService(personRepository, authorizationService, notificationService);
    }

    // ---------------------------------------------------------------- Seller Management

    @Bean
    public SellerRegistrationService sellerRegistrationService(
            PersonRepository personRepository,
            SellerRepository sellerRepository,
            NotificationService notificationService) {
        return new SellerRegistrationService(personRepository, sellerRepository, notificationService);
    }

    @Bean
    public SellerManagementService sellerManagementService(
            SellerRepository sellerRepository,
            PersonRepository personRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new SellerManagementService(sellerRepository, personRepository, authorizationService,
                notificationService);
    }

    // ---------------------------------------------------------------- Buyer Management

    @Bean
    public BuyerManagementService buyerManagementService(
            BuyerRepository buyerRepository,
            PersonRepository personRepository,
            OrderRepository orderRepository,
            ReturnRepository returnRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new BuyerManagementService(buyerRepository, personRepository, orderRepository,
                returnRepository, authorizationService, notificationService);
    }

    // ---------------------------------------------------------------- Warehouse Management

    @Bean
    public WarehouseManagementService warehouseManagementService(
            WarehouseRepository warehouseRepository,
            SellerRepository sellerRepository,
            AuthorizationService authorizationService) {
        return new WarehouseManagementService(warehouseRepository, sellerRepository, authorizationService);
    }

    // ---------------------------------------------------------------- Catalog Management

    @Bean
    public ProductCatalogService productCatalogService(
            SellerRepository sellerRepository,
            ProductRepository productRepository,
            NotificationService notificationService) {
        return new ProductCatalogService(sellerRepository, productRepository, notificationService);
    }

    @Bean
    public ProductManagementService productManagementService(
            ProductRepository productRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new ProductManagementService(productRepository, authorizationService, notificationService);
    }

    // ---------------------------------------------------------------- Inventory Management

    @Bean
    public InventoryReservationService inventoryReservationService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository) {
        return new InventoryReservationService(inventoryRepository, movementRepository);
    }

    @Bean
    public InventoryManagementService inventoryManagementService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            AuthorizationService authorizationService) {
        return new InventoryManagementService(inventoryRepository, movementRepository, productRepository,
                warehouseRepository, authorizationService);
    }

    // ---------------------------------------------------------------- Shopping Cart Management

    @Bean
    public ShoppingCartService shoppingCartService(
            BuyerRepository buyerRepository,
            ShoppingCartRepository cartRepository,
            ProductRepository productRepository,
            InventoryReservationService inventoryReservationService,
            NotificationService notificationService) {
        return new ShoppingCartService(buyerRepository, cartRepository, productRepository,
                inventoryReservationService, notificationService);
    }

    // ---------------------------------------------------------------- Order & Billing Management

    @Bean
    public BillingService billingService(
            OrderRepository orderRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new BillingService(orderRepository, authorizationService, notificationService);
    }

    @Bean
    public OrderCheckoutService orderCheckoutService(
            BuyerRepository buyerRepository,
            ShoppingCartRepository cartRepository,
            OrderRepository orderRepository,
            BillingService billingService,
            NotificationService notificationService) {
        return new OrderCheckoutService(buyerRepository, cartRepository, orderRepository,
                billingService, notificationService);
    }

    @Bean
    public OrderManagementService orderManagementService(
            OrderRepository orderRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new OrderManagementService(orderRepository, authorizationService, notificationService);
    }

    // ---------------------------------------------------------------- Logistics Management

    @Bean
    public ShipmentDispatchService shipmentDispatchService(
            OrderRepository orderRepository,
            WarehouseRepository warehouseRepository,
            PersonRepository personRepository,
            ShipmentTrackingRepository trackingRepository,
            InventoryReservationService inventoryReservationService,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new ShipmentDispatchService(orderRepository, warehouseRepository, personRepository,
                trackingRepository, inventoryReservationService, authorizationService, notificationService);
    }

    // ---------------------------------------------------------------- Returns and Refunds Management

    @Bean
    public ReturnManagementService returnManagementService(
            BuyerRepository buyerRepository,
            OrderRepository orderRepository,
            ReturnRepository returnRepository,
            InventoryRepository inventoryRepository,
            InventoryReservationService inventoryReservationService,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new ReturnManagementService(buyerRepository, orderRepository, returnRepository,
                inventoryRepository, inventoryReservationService, authorizationService, notificationService);
    }

    @Bean
    public RefundProcessingService refundProcessingService(
            ReturnRepository returnRepository,
            AuthorizationService authorizationService,
            NotificationService notificationService) {
        return new RefundProcessingService(returnRepository, authorizationService, notificationService);
    }

    // ---------------------------------------------------------------- Administrative Reporting

    @Bean
    public ReportingService reportingService(
            OrderRepository orderRepository,
            ReturnRepository returnRepository,
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            AuthorizationService authorizationService) {
        return new ReportingService(orderRepository, returnRepository, inventoryRepository,
                productRepository, sellerRepository, authorizationService);
    }
}
