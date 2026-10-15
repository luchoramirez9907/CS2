package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InventoryEntity;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Warehouse;

/**
 * Mapper: Inventory domain models to JPA entities and back.
 */
public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static InventoryEntity toEntity(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.setIdentifier(inventory.getIdentifier());
        entity.setProductId(inventory.getProduct().getIdentifier());
        entity.setWarehouseId(inventory.getWarehouse().getIdentifier());
        applyQuantities(entity, inventory);
        return entity;
    }

    public static void updateEntity(InventoryEntity entity, Inventory inventory) {
        applyQuantities(entity, inventory);
    }

    public static Inventory toDomain(InventoryEntity entity, Product product, Warehouse warehouse) {
        return new Inventory(entity.getIdentifier(), product, warehouse,
                entity.getAvailableQuantity(), entity.getReservedQuantity(), entity.getDamagedQuantity());
    }

    private static void applyQuantities(InventoryEntity entity, Inventory inventory) {
        entity.setAvailableQuantity(inventory.getAvailableQuantity());
        entity.setReservedQuantity(inventory.getReservedQuantity());
        entity.setDamagedQuantity(inventory.getDamagedQuantity());
    }
}
