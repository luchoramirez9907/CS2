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
import application.domain.services.reporting.ConsultCommercialReportService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.billing.ConsultInvoiceService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.reporting.ConsultSellerPerformanceReportService;
import application.domain.services.logistics.ConsultShipmentService;
import application.domain.services.buyer.ManageBuyerService;
import application.domain.services.catalog.ManageProductService;
import application.domain.services.seller.ManageSellerService;
import application.domain.services.user.ManageUserService;
import application.domain.services.warehouse.ManageWarehouseService;
import application.domain.services.returns.ProcessRefundService;
import application.domain.services.returns.ResolveReturnService;
import application.domain.ports.in.SupervisorPort;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use Case Adapter implementing SupervisorPort (read-only). It only
 * delegates to consultation operations of the Domain Services;
 * contains no business rules.
 */
@Service
public class SupervisorUseCaseImpl implements SupervisorPort {

    private final ManageUserService manageUserService;
    private final ManageSellerService manageSellerService;
    private final ManageBuyerService manageBuyerService;
    private final ManageWarehouseService manageWarehouseService;
    private final ManageProductService manageProductService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ConsultShipmentService consultShipmentService;
    private final ResolveReturnService resolveReturnService;
    private final ProcessRefundService processRefundService;
    private final ConsultCommercialReportService consultCommercialReportService;
    private final ConsultSellerPerformanceReportService consultSellerPerformanceReportService;

    public SupervisorUseCaseImpl(ManageUserService manageUserService,
                                 ManageSellerService manageSellerService,
                                 ManageBuyerService manageBuyerService,
                                 ManageWarehouseService manageWarehouseService,
                                 ManageProductService manageProductService,
                                 ConsultInventoryService consultInventoryService,
                                 ConsultOrderService consultOrderService,
                                 ConsultInvoiceService consultInvoiceService,
                                 ConsultShipmentService consultShipmentService,
                                 ResolveReturnService resolveReturnService,
                                 ProcessRefundService processRefundService,
                                 ConsultCommercialReportService consultCommercialReportService,
                                 ConsultSellerPerformanceReportService consultSellerPerformanceReportService) {
        this.manageUserService = manageUserService;
        this.manageSellerService = manageSellerService;
        this.manageBuyerService = manageBuyerService;
        this.manageWarehouseService = manageWarehouseService;
        this.manageProductService = manageProductService;
        this.consultInventoryService = consultInventoryService;
        this.consultOrderService = consultOrderService;
        this.consultInvoiceService = consultInvoiceService;
        this.consultShipmentService = consultShipmentService;
        this.resolveReturnService = resolveReturnService;
        this.processRefundService = processRefundService;
        this.consultCommercialReportService = consultCommercialReportService;
        this.consultSellerPerformanceReportService = consultSellerPerformanceReportService;
    }

    @Override
    public Person consultUser(User user, String userId) {
        return manageUserService.consultUser(user.getIdentifier(), userId);
    }

    @Override
    public Seller consultSeller(User user, String sellerId) {
        return manageSellerService.consultSeller(user.getIdentifier(), sellerId);
    }

    @Override
    public Buyer consultBuyer(User user, String buyerId) {
        return manageBuyerService.consultBuyer(user.getIdentifier(), buyerId);
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

    @Override
    public Warehouse consultWarehouse(User user, String warehouseId) {
        return manageWarehouseService.consultWarehouse(user.getIdentifier(), warehouseId);
    }

    @Override
    public Product consultProduct(User user, String productId) {
        return manageProductService.consultProduct(user.getIdentifier(), productId);
    }

    @Override
    public Inventory consultInventory(User user, String productId, String warehouseId) {
        return consultInventoryService.consultInventory(user.getIdentifier(), productId, warehouseId);
    }

    @Override
    public Order consultOrder(User user, String orderId) {
        return consultOrderService.consultOrder(user.getIdentifier(), orderId);
    }

    @Override
    public Invoice consultInvoice(User user, String orderId) {
        return consultInvoiceService.consultInvoice(user.getIdentifier(), orderId);
    }

    @Override
    public Shipment consultShipment(User user, String orderId) {
        return consultShipmentService.consultShipment(user.getIdentifier(), orderId);
    }

    @Override
    public List<ShipmentTrackingEvent> consultTracking(User user, String orderId) {
        return consultShipmentService.consultTracking(user.getIdentifier(), orderId);
    }

    @Override
    public Return consultReturn(User user, String returnId) {
        return resolveReturnService.consultReturn(user.getIdentifier(), returnId);
    }

    @Override
    public Refund consultRefund(User user, String returnId) {
        return processRefundService.consultRefund(user.getIdentifier(), returnId);
    }

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
}
