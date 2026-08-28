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
import Application.domain.services.InventoryReservationService;
import Application.domain.services.OrderCheckoutService;
import Application.domain.services.ProductCatalogService;
import Application.domain.services.RefundProcessingService;
import Application.domain.services.ReturnManagementService;
import Application.domain.services.SellerRegistrationService;
import Application.domain.services.ShipmentDispatchService;
import Application.domain.services.ShoppingCartService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dependency composition for the pure domain services. The domain never
 * depends on Spring: it is wired here, at the application core (per SDD -
 * App responsibilities: configure dependency injection).
 */
@Configuration
public class DomainBeanConfig {

    @Bean
    public InventoryReservationService inventoryReservationService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository) {
        return new InventoryReservationService(inventoryRepository, movementRepository);
    }

    @Bean
    public SellerRegistrationService sellerRegistrationService(
            PersonRepository personRepository,
            SellerRepository sellerRepository,
            NotificationService notificationService) {
        return new SellerRegistrationService(personRepository, sellerRepository, notificationService);
    }

    @Bean
    public ProductCatalogService productCatalogService(
            SellerRepository sellerRepository,
            ProductRepository productRepository,
            NotificationService notificationService) {
        return new ProductCatalogService(sellerRepository, productRepository, notificationService);
    }

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

    @Bean
    public OrderCheckoutService orderCheckoutService(
            BuyerRepository buyerRepository,
            ShoppingCartRepository cartRepository,
            OrderRepository orderRepository,
            NotificationService notificationService) {
        return new OrderCheckoutService(buyerRepository, cartRepository, orderRepository,
                notificationService);
    }

    @Bean
    public ReturnManagementService returnManagementService(
            BuyerRepository buyerRepository,
            OrderRepository orderRepository,
            ReturnRepository returnRepository,
            PersonRepository personRepository,
            InventoryRepository inventoryRepository,
            InventoryReservationService inventoryReservationService,
            NotificationService notificationService) {
        return new ReturnManagementService(buyerRepository, orderRepository, returnRepository,
                personRepository, inventoryRepository, inventoryReservationService, notificationService);
    }

    @Bean
    public RefundProcessingService refundProcessingService(
            ReturnRepository returnRepository,
            PersonRepository personRepository,
            NotificationService notificationService) {
        return new RefundProcessingService(returnRepository, personRepository, notificationService);
    }

    @Bean
    public ShipmentDispatchService shipmentDispatchService(
            OrderRepository orderRepository,
            ShipmentTrackingRepository trackingRepository,
            InventoryReservationService inventoryReservationService,
            NotificationService notificationService) {
        return new ShipmentDispatchService(orderRepository, trackingRepository,
                inventoryReservationService, notificationService);
    }
}
