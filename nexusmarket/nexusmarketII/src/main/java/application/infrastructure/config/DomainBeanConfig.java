package application.infrastructure.config;

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
import application.domain.ports.out.ShipmentTrackingRepository;
import application.domain.ports.out.ShoppingCartRepository;
import application.domain.ports.out.TokenService;
import application.domain.ports.out.UserRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.authentication.AuthenticateUserService;
import application.domain.services.authentication.LoadAuthenticatedUserService;
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
import application.domain.services.catalog.PublishProductService;
import application.domain.services.inventory.ConsultInventoryService;
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
import application.domain.services.seller.RegisterSellerService;
import application.domain.services.user.ManageUserService;
import application.domain.services.user.RegisterUserService;
import application.domain.services.warehouse.ManageWarehouseService;
import application.domain.services.warehouse.RegisterWarehouseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Dependency composition for the pure domain services. The domain never
 * depends on Spring: it is wired here, at the application core (per SDD -
 * App responsibilities: configure dependency injection).
 *
 * There is one bean per catalog service (SDD/domain/Services.md), grouped by
 * subdomain, plus the internal services they share.
 */
@Configuration
public class DomainBeanConfig {

    // ---------------------------------------------------------------- Authorization

    @Bean
    public ValidatePermissionsService validatePermissionsService(PersonRepository personRepository) {
        return new ValidatePermissionsService(personRepository);
    }

    @Bean
    public ValidateOwnershipAccessService validateOwnershipAccessService() {
        return new ValidateOwnershipAccessService();
    }

    // ---------------------------------------------------------------- Authentication

    @Bean
    public UserAccountService userAccountService(UserRepository userRepository,
                                                 PasswordService passwordService) {
        return new UserAccountService(userRepository, passwordService);
    }

    @Bean
    public AuthenticateUserService authenticateUserService(UserRepository userRepository,
                                                           PasswordService passwordService,
                                                           TokenService tokenService) {
        return new AuthenticateUserService(userRepository, passwordService, tokenService);
    }

    @Bean
    public LoadAuthenticatedUserService loadAuthenticatedUserService(UserRepository userRepository) {
        return new LoadAuthenticatedUserService(userRepository);
    }

    // ---------------------------------------------------------------- User Management

    @Bean
    public RegisterUserService registerUserService(PersonRepository personRepository,
                                                   ValidatePermissionsService permissionsService,
                                                   NotificationService notificationService,
                                                   UserAccountService userAccountService) {
        return new RegisterUserService(personRepository, permissionsService, notificationService,
                userAccountService);
    }

    @Bean
    public ManageUserService manageUserService(PersonRepository personRepository,
                                               ValidatePermissionsService permissionsService,
                                               ValidateOwnershipAccessService ownershipService,
                                               NotificationService notificationService) {
        return new ManageUserService(personRepository, permissionsService, ownershipService,
                notificationService);
    }

    // ---------------------------------------------------------------- Seller Management

    @Bean
    public RegisterSellerService registerSellerService(PersonRepository personRepository,
                                                       SellerRepository sellerRepository,
                                                       NotificationService notificationService,
                                                       UserAccountService userAccountService) {
        return new RegisterSellerService(personRepository, sellerRepository, notificationService,
                userAccountService);
    }

    @Bean
    public ManageSellerService manageSellerService(SellerRepository sellerRepository,
                                                   PersonRepository personRepository,
                                                   ValidatePermissionsService permissionsService,
                                                   ValidateOwnershipAccessService ownershipService,
                                                   NotificationService notificationService) {
        return new ManageSellerService(sellerRepository, personRepository, permissionsService,
                ownershipService, notificationService);
    }

    // ---------------------------------------------------------------- Buyer Management

    @Bean
    public RegisterBuyerService registerBuyerService(BuyerRepository buyerRepository,
                                                     PersonRepository personRepository,
                                                     ValidatePermissionsService permissionsService,
                                                     NotificationService notificationService,
                                                     UserAccountService userAccountService) {
        return new RegisterBuyerService(buyerRepository, personRepository, permissionsService,
                notificationService, userAccountService);
    }

    @Bean
    public ManageBuyerService manageBuyerService(BuyerRepository buyerRepository,
                                                 PersonRepository personRepository,
                                                 OrderRepository orderRepository,
                                                 ReturnRepository returnRepository,
                                                 ValidatePermissionsService permissionsService,
                                                 ValidateOwnershipAccessService ownershipService,
                                                 NotificationService notificationService) {
        return new ManageBuyerService(buyerRepository, personRepository, orderRepository, returnRepository,
                permissionsService, ownershipService, notificationService);
    }

    // ---------------------------------------------------------------- Warehouse Management

    @Bean
    public RegisterWarehouseService registerWarehouseService(WarehouseRepository warehouseRepository,
                                                             SellerRepository sellerRepository,
                                                             ValidatePermissionsService permissionsService,
                                                             ValidateOwnershipAccessService ownershipService) {
        return new RegisterWarehouseService(warehouseRepository, sellerRepository, permissionsService,
                ownershipService);
    }

    @Bean
    public ManageWarehouseService manageWarehouseService(WarehouseRepository warehouseRepository,
                                                         ValidatePermissionsService permissionsService,
                                                         ValidateOwnershipAccessService ownershipService) {
        return new ManageWarehouseService(warehouseRepository, permissionsService, ownershipService);
    }

    // ---------------------------------------------------------------- Catalog Management

    @Bean
    public PublishProductService publishProductService(SellerRepository sellerRepository,
                                                       ProductRepository productRepository,
                                                       NotificationService notificationService) {
        return new PublishProductService(sellerRepository, productRepository, notificationService);
    }

    @Bean
    public ManageProductService manageProductService(ProductRepository productRepository,
                                                     ValidatePermissionsService permissionsService,
                                                     ValidateOwnershipAccessService ownershipService) {
        return new ManageProductService(productRepository, permissionsService, ownershipService);
    }

    @Bean
    public ChangeProductStatusService changeProductStatusService(ProductRepository productRepository,
                                                                 ValidatePermissionsService permissionsService,
                                                                 ValidateOwnershipAccessService ownershipService,
                                                                 NotificationService notificationService) {
        return new ChangeProductStatusService(productRepository, permissionsService, ownershipService,
                notificationService);
    }

    // ---------------------------------------------------------------- Inventory Management

    @Bean
    public InventoryReservationService inventoryReservationService(InventoryRepository inventoryRepository,
                                                                   InventoryMovementRepository movementRepository) {
        return new InventoryReservationService(inventoryRepository, movementRepository);
    }

    @Bean
    public RegisterInventoryMovementService registerInventoryMovementService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            ValidatePermissionsService permissionsService,
            ValidateOwnershipAccessService ownershipService) {
        return new RegisterInventoryMovementService(inventoryRepository, movementRepository, productRepository,
                warehouseRepository, permissionsService, ownershipService);
    }

    @Bean
    public ConsultInventoryService consultInventoryService(InventoryRepository inventoryRepository,
                                                           InventoryMovementRepository movementRepository,
                                                           ProductRepository productRepository,
                                                           WarehouseRepository warehouseRepository,
                                                           ValidatePermissionsService permissionsService,
                                                           ValidateOwnershipAccessService ownershipService) {
        return new ConsultInventoryService(inventoryRepository, movementRepository, productRepository,
                warehouseRepository, permissionsService, ownershipService);
    }

    // ---------------------------------------------------------------- Shopping Cart Management

    @Bean
    public ManageCartItemsService manageCartItemsService(BuyerRepository buyerRepository,
                                                         ShoppingCartRepository cartRepository,
                                                         ProductRepository productRepository,
                                                         InventoryReservationService inventoryReservationService,
                                                         NotificationService notificationService) {
        return new ManageCartItemsService(buyerRepository, cartRepository, productRepository,
                inventoryReservationService, notificationService);
    }

    @Bean
    public ConsultCartService consultCartService(BuyerRepository buyerRepository,
                                                 ShoppingCartRepository cartRepository) {
        return new ConsultCartService(buyerRepository, cartRepository);
    }

    // ---------------------------------------------------------------- Billing Management

    @Bean
    public GenerateInvoiceService generateInvoiceService(OrderRepository orderRepository,
                                                         ValidatePermissionsService permissionsService,
                                                         NotificationService notificationService,
                                                         @Value("${nexusmarket.billing.tax-rate:0.19}") BigDecimal taxRate) {
        return new GenerateInvoiceService(orderRepository, permissionsService, notificationService, taxRate);
    }

    @Bean
    public ConsultInvoiceService consultInvoiceService(OrderRepository orderRepository,
                                                       ValidatePermissionsService permissionsService,
                                                       ValidateOwnershipAccessService ownershipService) {
        return new ConsultInvoiceService(orderRepository, permissionsService, ownershipService);
    }

    // ---------------------------------------------------------------- Order Management

    @Bean
    public ConfirmOrderService confirmOrderService(BuyerRepository buyerRepository,
                                                   ShoppingCartRepository cartRepository,
                                                   OrderRepository orderRepository,
                                                   GenerateInvoiceService generateInvoiceService,
                                                   NotificationService notificationService) {
        return new ConfirmOrderService(buyerRepository, cartRepository, orderRepository, generateInvoiceService,
                notificationService);
    }

    @Bean
    public UpdateOrderStatusService updateOrderStatusService(OrderRepository orderRepository,
                                                             ValidatePermissionsService permissionsService,
                                                             ValidateOwnershipAccessService ownershipService,
                                                             NotificationService notificationService) {
        return new UpdateOrderStatusService(orderRepository, permissionsService, ownershipService,
                notificationService);
    }

    @Bean
    public ConsultOrderService consultOrderService(OrderRepository orderRepository,
                                                   ValidatePermissionsService permissionsService,
                                                   ValidateOwnershipAccessService ownershipService) {
        return new ConsultOrderService(orderRepository, permissionsService, ownershipService);
    }

    // ---------------------------------------------------------------- Logistics Management

    @Bean
    public CreateShipmentService createShipmentService(OrderRepository orderRepository,
                                                       WarehouseRepository warehouseRepository,
                                                       PersonRepository personRepository,
                                                       ShipmentTrackingRepository trackingRepository,
                                                       ValidatePermissionsService permissionsService,
                                                       NotificationService notificationService) {
        return new CreateShipmentService(orderRepository, warehouseRepository, personRepository,
                trackingRepository, permissionsService, notificationService);
    }

    @Bean
    public UpdateShipmentStatusService updateShipmentStatusService(
            OrderRepository orderRepository,
            ShipmentTrackingRepository trackingRepository,
            InventoryReservationService inventoryReservationService,
            ValidatePermissionsService permissionsService,
            ValidateOwnershipAccessService ownershipService,
            NotificationService notificationService) {
        return new UpdateShipmentStatusService(orderRepository, trackingRepository, inventoryReservationService,
                permissionsService, ownershipService, notificationService);
    }

    @Bean
    public ConsultShipmentService consultShipmentService(OrderRepository orderRepository,
                                                         ShipmentTrackingRepository trackingRepository,
                                                         ValidatePermissionsService permissionsService,
                                                         ValidateOwnershipAccessService ownershipService) {
        return new ConsultShipmentService(orderRepository, trackingRepository, permissionsService,
                ownershipService);
    }

    // ---------------------------------------------------------------- Returns and Refunds Management

    @Bean
    public RequestReturnService requestReturnService(BuyerRepository buyerRepository,
                                                     OrderRepository orderRepository,
                                                     ReturnRepository returnRepository,
                                                     NotificationService notificationService) {
        return new RequestReturnService(buyerRepository, orderRepository, returnRepository, notificationService);
    }

    @Bean
    public ResolveReturnService resolveReturnService(ReturnRepository returnRepository,
                                                     InventoryReservationService inventoryReservationService,
                                                     ValidatePermissionsService permissionsService,
                                                     ValidateOwnershipAccessService ownershipService,
                                                     NotificationService notificationService) {
        return new ResolveReturnService(returnRepository, inventoryReservationService, permissionsService,
                ownershipService, notificationService);
    }

    @Bean
    public ProcessRefundService processRefundService(ReturnRepository returnRepository,
                                                     ValidatePermissionsService permissionsService,
                                                     ValidateOwnershipAccessService ownershipService,
                                                     NotificationService notificationService) {
        return new ProcessRefundService(returnRepository, permissionsService, ownershipService,
                notificationService);
    }

    // ---------------------------------------------------------------- Administrative Reporting

    @Bean
    public ConsultCommercialReportService consultCommercialReportService(OrderRepository orderRepository,
                                                                         ReturnRepository returnRepository,
                                                                         InventoryRepository inventoryRepository,
                                                                         ValidatePermissionsService permissionsService) {
        return new ConsultCommercialReportService(orderRepository, returnRepository, inventoryRepository,
                permissionsService);
    }

    @Bean
    public ConsultSellerPerformanceReportService consultSellerPerformanceReportService(
            OrderRepository orderRepository,
            ReturnRepository returnRepository,
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            ValidatePermissionsService permissionsService,
            ValidateOwnershipAccessService ownershipService) {
        return new ConsultSellerPerformanceReportService(orderRepository, returnRepository, productRepository,
                sellerRepository, permissionsService, ownershipService);
    }
}
