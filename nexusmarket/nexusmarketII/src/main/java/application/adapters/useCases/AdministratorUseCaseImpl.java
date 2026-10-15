package application.adapters.useCases;

import application.domain.enums.ReturnStatus;
import application.domain.models.Buyer;
import application.domain.models.CommercialReport;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Refund;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.SellerPerformanceReport;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdministratorPort;
import application.domain.services.catalog.ChangeProductStatusService;
import application.domain.services.reporting.ConsultCommercialReportService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.billing.ConsultInvoiceService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.reporting.ConsultSellerPerformanceReportService;
import application.domain.services.logistics.ConsultShipmentService;
import application.domain.services.logistics.CreateShipmentService;
import application.domain.services.billing.GenerateInvoiceService;
import application.domain.services.buyer.ManageBuyerService;
import application.domain.services.catalog.ManageProductService;
import application.domain.services.seller.ManageSellerService;
import application.domain.services.user.ManageUserService;
import application.domain.services.warehouse.ManageWarehouseService;
import application.domain.services.returns.ProcessRefundService;
import application.domain.services.buyer.RegisterBuyerService;
import application.domain.services.inventory.RegisterInventoryMovementService;
import application.domain.services.seller.RegisterSellerService;
import application.domain.services.user.RegisterUserService;
import application.domain.services.warehouse.RegisterWarehouseService;
import application.domain.services.returns.ResolveReturnService;
import application.domain.services.order.UpdateOrderStatusService;
import application.domain.services.logistics.UpdateShipmentStatusService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Use Case Adapter implementing AdministratorPort. Delegates to the
 * Domain Services, passing the
 * authenticated administrator as the requester; contains no business rules.
 */
@Service
public class AdministratorUseCaseImpl implements AdministratorPort {

    private final RegisterUserService registerUserService;
    private final ManageUserService manageUserService;
    private final RegisterSellerService registerSellerService;
    private final ManageSellerService manageSellerService;
    private final RegisterBuyerService registerBuyerService;
    private final ManageBuyerService manageBuyerService;
    private final RegisterWarehouseService registerWarehouseService;
    private final ManageWarehouseService manageWarehouseService;
    private final ManageProductService manageProductService;
    private final ChangeProductStatusService changeProductStatusService;
    private final RegisterInventoryMovementService registerInventoryMovementService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultOrderService consultOrderService;
    private final UpdateOrderStatusService updateOrderStatusService;
    private final GenerateInvoiceService generateInvoiceService;
    private final ConsultInvoiceService consultInvoiceService;
    private final CreateShipmentService createShipmentService;
    private final UpdateShipmentStatusService updateShipmentStatusService;
    private final ConsultShipmentService consultShipmentService;
    private final ResolveReturnService resolveReturnService;
    private final ProcessRefundService processRefundService;
    private final ConsultCommercialReportService consultCommercialReportService;
    private final ConsultSellerPerformanceReportService consultSellerPerformanceReportService;

    public AdministratorUseCaseImpl(RegisterUserService registerUserService,
                                    ManageUserService manageUserService,
                                    RegisterSellerService registerSellerService,
                                    ManageSellerService manageSellerService,
                                    RegisterBuyerService registerBuyerService,
                                    ManageBuyerService manageBuyerService,
                                    RegisterWarehouseService registerWarehouseService,
                                    ManageWarehouseService manageWarehouseService,
                                    ManageProductService manageProductService,
                                    ChangeProductStatusService changeProductStatusService,
                                    RegisterInventoryMovementService registerInventoryMovementService,
                                    ConsultInventoryService consultInventoryService,
                                    ConsultOrderService consultOrderService,
                                    UpdateOrderStatusService updateOrderStatusService,
                                    GenerateInvoiceService generateInvoiceService,
                                    ConsultInvoiceService consultInvoiceService,
                                    CreateShipmentService createShipmentService,
                                    UpdateShipmentStatusService updateShipmentStatusService,
                                    ConsultShipmentService consultShipmentService,
                                    ResolveReturnService resolveReturnService,
                                    ProcessRefundService processRefundService,
                                    ConsultCommercialReportService consultCommercialReportService,
                                    ConsultSellerPerformanceReportService consultSellerPerformanceReportService) {
        this.registerUserService = registerUserService;
        this.manageUserService = manageUserService;
        this.registerSellerService = registerSellerService;
        this.manageSellerService = manageSellerService;
        this.registerBuyerService = registerBuyerService;
        this.manageBuyerService = manageBuyerService;
        this.registerWarehouseService = registerWarehouseService;
        this.manageWarehouseService = manageWarehouseService;
        this.manageProductService = manageProductService;
        this.changeProductStatusService = changeProductStatusService;
        this.registerInventoryMovementService = registerInventoryMovementService;
        this.consultInventoryService = consultInventoryService;
        this.consultOrderService = consultOrderService;
        this.updateOrderStatusService = updateOrderStatusService;
        this.generateInvoiceService = generateInvoiceService;
        this.consultInvoiceService = consultInvoiceService;
        this.createShipmentService = createShipmentService;
        this.updateShipmentStatusService = updateShipmentStatusService;
        this.consultShipmentService = consultShipmentService;
        this.resolveReturnService = resolveReturnService;
        this.processRefundService = processRefundService;
        this.consultCommercialReportService = consultCommercialReportService;
        this.consultSellerPerformanceReportService = consultSellerPerformanceReportService;
    }

    // ---------------------------------------------------------------- Users

    @Override
    public Person registerUser(User user, String identifier, String fullName, String email, SystemRole role,
                               String password) {
        return registerUserService.registerUser(user.getIdentifier(), identifier, fullName, email, role, password);
    }

    @Override
    public Person consultUser(User user, String userId) {
        return manageUserService.consultUser(user.getIdentifier(), userId);
    }

    @Override
    public Person updateUser(User user, String userId, String fullName, String email) {
        return manageUserService.updateUser(user.getIdentifier(), userId, fullName, email);
    }

    @Override
    public Person changeUserStatus(User user, String userId, UserStatus status) {
        return manageUserService.changeUserStatus(user.getIdentifier(), userId, status);
    }

    // ---------------------------------------------------------------- Sellers

    @Override
    public Seller registerSeller(User user, String identifier, String fullName, String email, String password) {
        return registerSellerService.registerSeller(user.getIdentifier(), identifier, fullName, email, password);
    }

    @Override
    public Seller consultSeller(User user, String sellerId) {
        return manageSellerService.consultSeller(user.getIdentifier(), sellerId);
    }

    @Override
    public Seller updateSeller(User user, String sellerId, String fullName, String email) {
        return manageSellerService.updateSeller(user.getIdentifier(), sellerId, fullName, email);
    }

    @Override
    public Seller changeSellerStatus(User user, String sellerId, UserStatus status) {
        return manageSellerService.changeSellerStatus(user.getIdentifier(), sellerId, status);
    }

    // ---------------------------------------------------------------- Buyers

    @Override
    public Buyer registerBuyer(User user, String identifier, String fullName, String email,
                               Address primaryAddress, String password) {
        return registerBuyerService.registerBuyer(user.getIdentifier(), identifier, fullName, email,
                primaryAddress, password);
    }

    @Override
    public Buyer consultBuyer(User user, String buyerId) {
        return manageBuyerService.consultBuyer(user.getIdentifier(), buyerId);
    }

    @Override
    public Buyer updateBuyer(User user, String buyerId, String fullName, String email, Address primaryAddress,
                             List<Address> additionalAddresses) {
        return manageBuyerService.updateBuyer(user.getIdentifier(), buyerId, fullName, email, primaryAddress,
                additionalAddresses);
    }

    @Override
    public Buyer changeBuyerCommercialStatus(User user, String buyerId, BuyerCommercialStatus status) {
        return manageBuyerService.changeCommercialStatus(user.getIdentifier(), buyerId, status);
    }

    @Override
    public List<Order> consultBuyerOrders(User user, String buyerId) {
        return manageBuyerService.consultBuyerOrders(user.getIdentifier(), buyerId);
    }

    @Override
    public List<Return> consultBuyerReturns(User user, String buyerId) {
        return manageBuyerService.consultBuyerReturns(user.getIdentifier(), buyerId);
    }

    @Override
    public List<Refund> consultBuyerRefunds(User user, String buyerId) {
        return manageBuyerService.consultBuyerRefunds(user.getIdentifier(), buyerId);
    }

    // ---------------------------------------------------------------- Warehouses

    @Override
    public Warehouse registerWarehouse(User user, String identifier, String name, Address address,
                                       String ownerSellerId) {
        return registerWarehouseService.registerWarehouse(user.getIdentifier(), identifier, name, address,
                ownerSellerId);
    }

    @Override
    public Warehouse consultWarehouse(User user, String warehouseId) {
        return manageWarehouseService.consultWarehouse(user.getIdentifier(), warehouseId);
    }

    @Override
    public Warehouse updateWarehouse(User user, String warehouseId, String name, Address address) {
        return manageWarehouseService.updateWarehouse(user.getIdentifier(), warehouseId, name, address);
    }

    // ---------------------------------------------------------------- Catalog and inventory

    @Override
    public Product consultProduct(User user, String productId) {
        return manageProductService.consultProduct(user.getIdentifier(), productId);
    }

    @Override
    public Product changeProductStatus(User user, String productId, ProductStatus status) {
        return changeProductStatusService.changeProductStatus(user.getIdentifier(), productId, status);
    }

    @Override
    public Inventory registerInventoryMovement(User user, String productId, String warehouseId,
                                               InventoryMovementType type, int quantity) {
        return registerInventoryMovementService.registerMovement(user.getIdentifier(), productId, warehouseId,
                type, quantity);
    }

    @Override
    public Inventory registerDamagedStock(User user, String productId, String warehouseId, int quantity) {
        return registerInventoryMovementService.registerDamagedStock(user.getIdentifier(), productId,
                warehouseId, quantity);
    }

    @Override
    public Inventory consultInventory(User user, String productId, String warehouseId) {
        return consultInventoryService.consultInventory(user.getIdentifier(), productId, warehouseId);
    }

    // ---------------------------------------------------------------- Orders and billing

    @Override
    public Order consultOrder(User user, String orderId) {
        return consultOrderService.consultOrder(user.getIdentifier(), orderId);
    }

    @Override
    public Order confirmOrderPayment(User user, String orderId) {
        return updateOrderStatusService.confirmPayment(user.getIdentifier(), orderId);
    }

    @Override
    public Order finalizeOrder(User user, String orderId) {
        return updateOrderStatusService.finalizeOrder(user.getIdentifier(), orderId);
    }

    @Override
    public Invoice generateInvoice(User user, String orderId) {
        return generateInvoiceService.generateInvoice(user.getIdentifier(), orderId);
    }

    @Override
    public Invoice consultInvoice(User user, String orderId) {
        return consultInvoiceService.consultInvoice(user.getIdentifier(), orderId);
    }

    // ---------------------------------------------------------------- Logistics

    @Override
    public Shipment createShipment(User user, String orderId, String originWarehouseId,
                                   String logisticsOperatorId) {
        return createShipmentService.createShipment(user.getIdentifier(), orderId, originWarehouseId,
                logisticsOperatorId);
    }

    @Override
    public Shipment dispatchShipment(User user, String orderId) {
        return updateShipmentStatusService.dispatchShipment(user.getIdentifier(), orderId);
    }

    @Override
    public Shipment confirmDelivery(User user, String orderId) {
        return updateShipmentStatusService.confirmDelivery(user.getIdentifier(), orderId);
    }

    @Override
    public Shipment consultShipment(User user, String orderId) {
        return consultShipmentService.consultShipment(user.getIdentifier(), orderId);
    }

    @Override
    public List<ShipmentTrackingEvent> consultTracking(User user, String orderId) {
        return consultShipmentService.consultTracking(user.getIdentifier(), orderId);
    }

    // ---------------------------------------------------------------- Returns and refunds

    @Override
    public Return approveReturn(User user, String returnId) {
        return resolveReturnService.approveReturn(user.getIdentifier(), returnId);
    }

    @Override
    public Return rejectReturn(User user, String returnId) {
        return resolveReturnService.rejectReturn(user.getIdentifier(), returnId);
    }

    @Override
    public Return completeReturn(User user, String returnId) {
        return resolveReturnService.completeReturn(user.getIdentifier(), returnId);
    }

    @Override
    public Return consultReturn(User user, String returnId) {
        return resolveReturnService.consultReturn(user.getIdentifier(), returnId);
    }

    @Override
    public Refund processRefund(User user, String returnId, BigDecimal amount) {
        return processRefundService.processRefund(user.getIdentifier(), returnId, amount);
    }

    @Override
    public Refund consultRefund(User user, String returnId) {
        return processRefundService.consultRefund(user.getIdentifier(), returnId);
    }

    // ---------------------------------------------------------------- Reporting

    @Override
    public CommercialReport consultCommercialReport(User user) {
        return consultCommercialReportService.consultCommercialReport(user.getIdentifier());
    }

    @Override
    public SellerPerformanceReport consultSellerPerformanceReport(User user, String sellerId) {
        return consultSellerPerformanceReportService.consultSellerPerformanceReport(user.getIdentifier(), sellerId);
    }

    // ---------------------------------------------------------------- Listings and history

    @Override
    public List<Person> listUsers(User user, SystemRole role) {
        return manageUserService.listUsers(user.getIdentifier(), role);
    }

    @Override
    public List<Warehouse> listWarehouses(User user) {
        return manageWarehouseService.listWarehouses(user.getIdentifier());
    }

    @Override
    public List<Product> consultSellerProducts(User user, String sellerId) {
        return manageProductService.consultSellerProducts(user.getIdentifier(), sellerId);
    }

    @Override
    public List<Order> listOrders(User user, OrderStatus status) {
        return consultOrderService.listOrders(user.getIdentifier(), status);
    }

    @Override
    public List<Return> listReturns(User user, ReturnStatus status) {
        return resolveReturnService.listReturns(user.getIdentifier(), status);
    }

    @Override
    public List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId) {
        return consultInventoryService.consultMovementHistory(user.getIdentifier(), productId, warehouseId);
    }

    // ---------------------------------------------------------------- Failed deliveries and refund decisions

    @Override
    public Shipment registerShipmentReturn(User user, String orderId, String reason) {
        return updateShipmentStatusService.registerShipmentReturn(user.getIdentifier(), orderId, reason);
    }

    @Override
    public Refund rejectRefund(User user, String returnId) {
        return processRefundService.rejectRefund(user.getIdentifier(), returnId);
    }
}
