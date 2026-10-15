package application.adapters.out.persistence;

import application.adapters.out.persistence.mongodb.documents.InventoryMovementDocument;
import application.adapters.out.persistence.mongodb.documents.ShipmentTrackingDocument;
import application.adapters.out.persistence.mongodb.mappers.InventoryMovementDocumentMapper;
import application.adapters.out.persistence.mongodb.mappers.ShipmentTrackingDocumentMapper;
import application.adapters.out.persistence.mysql.entities.PhysicalProductEntity;
import application.adapters.out.persistence.mysql.entities.ProductEntity;
import application.adapters.out.persistence.mysql.entities.UserEntity;
import application.adapters.out.persistence.mysql.mappers.ProductMapper;
import application.adapters.out.persistence.mysql.mappers.UserMapper;
import application.domain.enums.TrackingEventType;
import application.domain.models.Administrator;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.MarketplaceWarehouse;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.ProductStatus;
import application.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests of the persistence mappers (Domain Model ↔ JPA Entity /
 * MongoDB Document).
 */
class PersistenceMappersTest {

    private final Administrator admin = new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE);
    private final Seller seller = new Seller("seller-1", "TechStore", "seller@x.com", admin, UserStatus.ACTIVE);

    @Test
    void userRoundTripKeepsHashAndTokenVersion() {
        User user = new User(admin, "hashed:x");
        user.invalidateSessions();
        UserEntity entity = UserMapper.toEntity(user);
        assertEquals("admin-1", entity.getPersonId());
        assertEquals(1, entity.getAuthTokenVersion());

        User restored = UserMapper.toDomain(entity, admin);
        assertEquals(user, restored);
        assertEquals("hashed:x", restored.getPasswordHash());
        assertEquals(1, restored.getAuthTokenVersion());
    }

    @Test
    void productRoundTripKeepsSpecializationPriceAndVariants() {
        PhysicalProduct product = new PhysicalProduct("product-1", "Keyboard", "RGB", new BigDecimal("99.90"),
                ProductStatus.PUBLISHED, seller);
        product.addVariant("Color", "Black");

        ProductEntity entity = ProductMapper.toEntity(product);
        assertInstanceOf(PhysicalProductEntity.class, entity);
        assertEquals(new BigDecimal("99.90"), entity.getPrice());

        Product restored = ProductMapper.toDomain(entity, seller);
        assertInstanceOf(PhysicalProduct.class, restored);
        assertEquals(new BigDecimal("99.90"), restored.getPrice());
        assertEquals(1, restored.getVariants().size());
        assertEquals(ProductStatus.PUBLISHED, restored.getStatus());
    }

    @Test
    void legacyProductRowsWithoutPriceLoadWithZeroPrice() {
        ProductEntity entity = ProductMapper.toEntity(new PhysicalProduct("product-1", "Keyboard", null,
                BigDecimal.ONE, ProductStatus.PUBLISHED, seller));
        entity.setPrice(null);
        assertEquals(BigDecimal.ZERO, ProductMapper.toDomain(entity, seller).getPrice());
    }

    @Test
    void inventoryMovementIsStoredWithItsCatalogCode() {
        Inventory inventory = new Inventory("inv-1",
                new PhysicalProduct("product-1", "Keyboard", null, BigDecimal.ONE, ProductStatus.PUBLISHED, seller),
                new MarketplaceWarehouse("wh-1", "Main", new Address("Calle 1", "Bogota", "CUND", "Colombia",
                        "110111"), admin), 0, 0);
        inventory.stockIn(5, admin);
        InventoryMovement movement = inventory.drainPendingMovements().get(0);

        InventoryMovementDocument document = InventoryMovementDocumentMapper.toDocument(movement);
        assertNotNull(document.getId());
        assertEquals("inv-1", document.getInventoryId());
        assertEquals("product-1", document.getProductId());
        assertEquals("STOCK_IN", document.getMovementTypeCode());
        assertEquals("admin-1", document.getPerformedById());
    }

    @Test
    void shipmentTrackingRoundTripRestoresTheEnum() {
        LocalDateTime now = LocalDateTime.now();
        ShipmentTrackingDocument document = ShipmentTrackingDocumentMapper.toDocument("ship-1", "order-1",
                TrackingEventType.DISPATCHED, now, "Left the warehouse");
        assertEquals("DISPATCHED", document.getEvent());

        ShipmentTrackingEvent event = ShipmentTrackingDocumentMapper.toDomain(document);
        assertEquals(new ShipmentTrackingEvent("ship-1", "order-1", TrackingEventType.DISPATCHED, now,
                "Left the warehouse"), event);
    }
}
