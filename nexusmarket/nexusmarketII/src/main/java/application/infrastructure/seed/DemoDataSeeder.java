package application.infrastructure.seed;

import application.domain.models.Administrator;
import application.domain.models.Buyer;
import application.domain.models.Inventory;
import application.domain.models.LogisticsOperator;
import application.domain.models.MarketplaceWarehouse;
import application.domain.models.PhysicalProduct;
import application.domain.models.Seller;
import application.domain.models.Supervisor;
import application.domain.ports.out.BuyerRepository;
import application.domain.ports.out.InventoryMovementRepository;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.PersonRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.SellerRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.authentication.UserAccountService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.BuyerCommercialStatus;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds reference data when the application runs with the "demo" profile
 * so the full happy path can be exercised through the REST API:
 *
 *   administrator-1, seller-1, buyer-1, supervisor-1, operator-1,
 *   warehouse-1, product-1, inv-1.
 *
 * Every role (administrator, supervisor, seller, buyer and logistics
 * operator) receives an access account whose password is configured by the "nexusmarket.demo.password" property.
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
    private final UserAccountService userAccountService;
    private final String demoPassword;

    public DemoDataSeeder(PersonRepository personRepository,
                          SellerRepository sellerRepository,
                          BuyerRepository buyerRepository,
                          WarehouseRepository warehouseRepository,
                          ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          InventoryMovementRepository movementRepository,
                          UserAccountService userAccountService,
                          @Value("${nexusmarket.demo.password}") String demoPassword) {
        this.personRepository = personRepository;
        this.sellerRepository = sellerRepository;
        this.buyerRepository = buyerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.userAccountService = userAccountService;
        this.demoPassword = demoPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (personRepository.existsByIdentifier("administrator-1")) {
            log.info("Demo catalog data already present; skipping it");
        } else {
            seedCatalog();
        }
        seedStaff();
    }

    private void seedCatalog() {

        Administrator administrator = new Administrator("administrator-1", "Ada Administradora",
                "admin@nexusmarket.com", UserStatus.ACTIVE);
        personRepository.save(administrator);
        userAccountService.createAccount(administrator, demoPassword);

        Address address = new Address("Calle 123", "Bogota", "Cundinamarca", "Colombia", "110111");

        Seller seller = new Seller("seller-1", "TechStore SAS", "seller@nexusmarket.com",
                administrator, UserStatus.ACTIVE);
        sellerRepository.save(seller);
        userAccountService.createAccount(seller, demoPassword);

        Buyer buyer = new Buyer("buyer-1", "Bruno Comprador", "buyer@nexusmarket.com",
                address, BuyerCommercialStatus.ACTIVE, UserStatus.ACTIVE);
        buyerRepository.save(buyer);
        userAccountService.createAccount(buyer, demoPassword);

        MarketplaceWarehouse warehouse = new MarketplaceWarehouse("warehouse-1",
                "Main Marketplace Warehouse", address, administrator);
        warehouseRepository.save(warehouse);

        PhysicalProduct product = new PhysicalProduct("product-1", "Mechanical Keyboard",
                "RGB mechanical keyboard", new BigDecimal("249900.00"), ProductStatus.PUBLISHED, seller);
        product.addVariant("Color", "Black");
        seller.addProduct(product);
        productRepository.save(product);

        Inventory inventory = new Inventory("inv-1", product, warehouse, 0, 0);
        inventory.stockIn(50, administrator);
        inventoryRepository.save(inventory);
        inventory.drainPendingMovements().forEach(movementRepository::save);

        log.info("Demo data seeded: administrator-1, seller-1, buyer-1, warehouse-1, product-1, inv-1");
    }

    /**
     * Supervisor and logistics operator accounts, so that every role of the
     * API can be tried right after startup. Idempotent per identifier.
     */
    private void seedStaff() {
        if (!personRepository.existsByIdentifier("supervisor-1")) {
            Supervisor supervisor = new Supervisor("supervisor-1", "Sara Supervisora",
                    "supervisor@nexusmarket.com", UserStatus.ACTIVE);
            personRepository.save(supervisor);
            userAccountService.createAccount(supervisor, demoPassword);
            log.info("Demo data seeded: supervisor-1");
        }
        if (!personRepository.existsByIdentifier("operator-1")) {
            LogisticsOperator operator = new LogisticsOperator("operator-1", "Oscar Operador",
                    "operator@nexusmarket.com", UserStatus.ACTIVE);
            personRepository.save(operator);
            userAccountService.createAccount(operator, demoPassword);
            log.info("Demo data seeded: operator-1");
        }
    }
}
