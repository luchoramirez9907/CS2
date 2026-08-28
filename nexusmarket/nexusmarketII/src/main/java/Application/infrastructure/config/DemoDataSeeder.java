package Application.infrastructure.config;

import Application.domain.models.Administrator;
import Application.domain.models.Buyer;
import Application.domain.models.Inventory;
import Application.domain.models.MarketplaceWarehouse;
import Application.domain.models.PhysicalProduct;
import Application.domain.models.Seller;
import Application.domain.ports.out.BuyerRepository;
import Application.domain.ports.out.InventoryMovementRepository;
import Application.domain.ports.out.InventoryRepository;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.ports.out.WarehouseRepository;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;
import Application.domain.valueobjects.ProductStatus;
import Application.domain.valueobjects.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds reference data when the application runs with the "demo" profile
 * so the full happy path can be exercised through the REST API:
 *
 *   administrator-1, seller-1, buyer-1, warehouse-1, product-1, inv-1.
 */
@Component
@Profile("demo")
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final PersonRepository personRepository;
    private final SellerRepository sellerRepository;
    private final BuyerRepository buyerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;

    public DemoDataSeeder(PersonRepository personRepository,
                          SellerRepository sellerRepository,
                          BuyerRepository buyerRepository,
                          WarehouseRepository warehouseRepository,
                          ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          InventoryMovementRepository movementRepository) {
        this.personRepository = personRepository;
        this.sellerRepository = sellerRepository;
        this.buyerRepository = buyerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (personRepository.existsByIdentifier("administrator-1")) {
            log.info("Demo data already present; skipping seed");
            return;
        }

        Administrator administrator = new Administrator("administrator-1", "Ada Administradora",
                "admin@nexusmarket.com", UserStatus.ACTIVE);
        personRepository.save(administrator);

        Address address = new Address("Calle 123", "Bogota", "Cundinamarca", "Colombia", "110111");

        Seller seller = new Seller("seller-1", "TechStore SAS", "seller@nexusmarket.com",
                administrator, UserStatus.ACTIVE);
        sellerRepository.save(seller);

        Buyer buyer = new Buyer("buyer-1", "Bruno Comprador", "buyer@nexusmarket.com",
                address, BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        buyerRepository.save(buyer);

        MarketplaceWarehouse warehouse = new MarketplaceWarehouse("warehouse-1",
                "Main Marketplace Warehouse", address, administrator);
        warehouseRepository.save(warehouse);

        PhysicalProduct product = new PhysicalProduct("product-1", "Mechanical Keyboard",
                "RGB mechanical keyboard", ProductStatus.PUBLISHED, seller);
        product.addVariant("Color", "Black");
        seller.addProduct(product);
        productRepository.save(product);

        Inventory inventory = new Inventory("inv-1", product, warehouse, 0, 0);
        inventory.stockIn(50, administrator);
        inventoryRepository.save(inventory);
        inventory.drainPendingMovements().forEach(movementRepository::save);

        log.info("Demo data seeded: administrator-1, seller-1, buyer-1, warehouse-1, product-1, inv-1");
    }
}
