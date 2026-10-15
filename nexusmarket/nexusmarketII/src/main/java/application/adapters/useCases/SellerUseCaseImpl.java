package application.adapters.useCases;

import application.domain.enums.ReturnStatus;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Invoice;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.models.Return;
import application.domain.models.Seller;
import application.domain.models.SellerPerformanceReport;
import application.domain.models.Shipment;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.services.catalog.ChangeProductStatusService;
import application.domain.services.inventory.ConsultInventoryService;
import application.domain.services.billing.ConsultInvoiceService;
import application.domain.services.order.ConsultOrderService;
import application.domain.services.reporting.ConsultSellerPerformanceReportService;
import application.domain.services.logistics.ConsultShipmentService;
import application.domain.services.catalog.ManageProductService;
import application.domain.services.seller.ManageSellerService;
import application.domain.services.warehouse.ManageWarehouseService;
import application.domain.services.catalog.PublishProductService;
import application.domain.services.inventory.RegisterInventoryMovementService;
import application.domain.services.warehouse.RegisterWarehouseService;
import application.domain.services.returns.ResolveReturnService;
import application.domain.ports.in.SellerPort;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.InventoryMovementType;
import application.domain.valueobjects.OrderStatus;
import application.domain.valueobjects.ProductStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Use Case Adapter implementing SellerPort. The authenticated User is the
 * seller that owns the profile, warehouses and products; delegates to the
 * Domain Services and contains no
 * business rules.
 */
@Service
public class SellerUseCaseImpl implements SellerPort {

    private final ManageSellerService manageSellerService;
    private final RegisterWarehouseService registerWarehouseService;
    private final ManageWarehouseService manageWarehouseService;
    private final PublishProductService publishProductService;
    private final ManageProductService manageProductService;
    private final ChangeProductStatusService changeProductStatusService;
    private final RegisterInventoryMovementService registerInventoryMovementService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultOrderService consultOrderService;
    private final ConsultInvoiceService consultInvoiceService;
    private final ConsultShipmentService consultShipmentService;
    private final ResolveReturnService resolveReturnService;
    private final ConsultSellerPerformanceReportService consultSellerPerformanceReportService;

    public SellerUseCaseImpl(ManageSellerService manageSellerService,
                             RegisterWarehouseService registerWarehouseService,
                             ManageWarehouseService manageWarehouseService,
                             PublishProductService publishProductService,
                             ManageProductService manageProductService,
                             ChangeProductStatusService changeProductStatusService,
                             RegisterInventoryMovementService registerInventoryMovementService,
                             ConsultInventoryService consultInventoryService,
                             ConsultOrderService consultOrderService,
                             ConsultInvoiceService consultInvoiceService,
                             ConsultShipmentService consultShipmentService,
                             ResolveReturnService resolveReturnService,
                             ConsultSellerPerformanceReportService consultSellerPerformanceReportService) {
        this.manageSellerService = manageSellerService;
        this.registerWarehouseService = registerWarehouseService;
        this.manageWarehouseService = manageWarehouseService;
        this.publishProductService = publishProductService;
        this.manageProductService = manageProductService;
        this.changeProductStatusService = changeProductStatusService;
        this.registerInventoryMovementService = registerInventoryMovementService;
        this.consultInventoryService = consultInventoryService;
        this.consultOrderService = consultOrderService;
        this.consultInvoiceService = consultInvoiceService;
        this.consultShipmentService = consultShipmentService;
        this.resolveReturnService = resolveReturnService;
        this.consultSellerPerformanceReportService = consultSellerPerformanceReportService;
    }

    // ---------------------------------------------------------------- Profile

    @Override
    public Seller consultMyProfile(User user) {
        return manageSellerService.consultSeller(user.getIdentifier(), user.getIdentifier());
    }

    @Override
    public Seller updateMyProfile(User user, String fullName, String email) {
        return manageSellerService.updateSeller(user.getIdentifier(), user.getIdentifier(), fullName, email);
    }

    // ---------------------------------------------------------------- Warehouses

    @Override
    public Warehouse registerWarehouse(User user, String identifier, String name, Address address) {
        // A seller always registers warehouses it owns.
        return registerWarehouseService.registerWarehouse(user.getIdentifier(), identifier, name, address,
                user.getIdentifier());
    }

    @Override
    public Warehouse consultWarehouse(User user, String warehouseId) {
        return manageWarehouseService.consultWarehouse(user.getIdentifier(), warehouseId);
    }

    @Override
    public Warehouse updateWarehouse(User user, String warehouseId, String name, Address address) {
        return manageWarehouseService.updateWarehouse(user.getIdentifier(), warehouseId, name, address);
    }

    // ---------------------------------------------------------------- Catalog

    @Override
    public Product publishProduct(User user, Product product) {
        return publishProductService.publishProduct(user.getIdentifier(), product);
    }

    @Override
    public Product consultProduct(User user, String productId) {
        return manageProductService.consultProduct(user.getIdentifier(), productId);
    }

    @Override
    public Product updateProduct(User user, String productId, String name, String description,
                                 BigDecimal price, List<ProductVariant> variants) {
        return manageProductService.updateProduct(user.getIdentifier(), productId, name, description,
                price, variants);
    }

    @Override
    public Product changeProductStatus(User user, String productId, ProductStatus status) {
        return changeProductStatusService.changeProductStatus(user.getIdentifier(), productId, status);
    }

    // ---------------------------------------------------------------- Inventory

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

    // ---------------------------------------------------------------- Orders and returns

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

    // ---------------------------------------------------------------- Reporting

    @Override
    public SellerPerformanceReport consultMyPerformanceReport(User user) {
        return consultSellerPerformanceReportService.consultSellerPerformanceReport(user.getIdentifier(),
                user.getIdentifier());
    }

    // ---------------------------------------------------------------- Listings and history

    @Override
    public List<Product> consultMyProducts(User user) {
        return manageProductService.consultSellerProducts(user.getIdentifier(), user.getIdentifier());
    }

    @Override
    public List<Warehouse> consultMyWarehouses(User user) {
        return manageWarehouseService.listWarehouses(user.getIdentifier());
    }

    @Override
    public List<Order> consultMyOrders(User user, OrderStatus status) {
        return consultOrderService.listOrders(user.getIdentifier(), status);
    }

    @Override
    public List<Return> consultMyReturns(User user, ReturnStatus status) {
        return resolveReturnService.listReturns(user.getIdentifier(), status);
    }

    @Override
    public List<InventoryMovement> consultInventoryMovements(User user, String productId, String warehouseId) {
        return consultInventoryService.consultMovementHistory(user.getIdentifier(), productId, warehouseId);
    }
}
