package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.AddressEmbeddable;
import application.adapters.out.persistence.mysql.entities.MarketplaceWarehouseEntity;
import application.adapters.out.persistence.mysql.entities.SellerWarehouseEntity;
import application.adapters.out.persistence.mysql.entities.WarehouseEntity;
import application.domain.models.Administrator;
import application.domain.models.MarketplaceWarehouse;
import application.domain.models.Person;
import application.domain.models.Seller;
import application.domain.models.SellerWarehouse;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;

import java.util.function.Function;

/**
 * Mapper: Warehouse domain models to JPA entities and back.
 */
public final class WarehouseMapper {

    private WarehouseMapper() {
    }

    public static WarehouseEntity toEntity(Warehouse warehouse) {
        WarehouseEntity entity;
        if (warehouse instanceof MarketplaceWarehouse marketplace) {
            MarketplaceWarehouseEntity marketplaceEntity = new MarketplaceWarehouseEntity();
            marketplaceEntity.setManagedById(marketplace.getManagedBy().getIdentifier());
            entity = marketplaceEntity;
        } else if (warehouse instanceof SellerWarehouse sellerWarehouse) {
            SellerWarehouseEntity sellerEntity = new SellerWarehouseEntity();
            sellerEntity.setOwnerId(sellerWarehouse.getOwner().getIdentifier());
            entity = sellerEntity;
        } else {
            throw new IllegalArgumentException("Unsupported warehouse type: "
                    + warehouse.getClass().getSimpleName());
        }
        entity.setIdentifier(warehouse.getIdentifier());
        entity.setName(warehouse.getName());
        entity.setAddress(PersonMapper.toEmbeddable(warehouse.getAddress()));
        return entity;
    }

    public static Warehouse toDomain(WarehouseEntity entity, Function<String, Person> personResolver) {
        Address address = PersonMapper.toAddress(entity.getAddress());
        if (entity instanceof MarketplaceWarehouseEntity marketplaceEntity) {
            Person manager = personResolver.apply(marketplaceEntity.getManagedById());
            if (!(manager instanceof Administrator administrator)) {
                throw new IllegalStateException("Marketplace warehouse '" + entity.getIdentifier()
                        + "' must be managed by an Administrator");
            }
            return new MarketplaceWarehouse(entity.getIdentifier(), entity.getName(),
                    address, administrator);
        }
        if (entity instanceof SellerWarehouseEntity sellerEntity) {
            Person owner = personResolver.apply(sellerEntity.getOwnerId());
            if (!(owner instanceof Seller seller)) {
                throw new IllegalStateException("Seller warehouse '" + entity.getIdentifier()
                        + "' must be owned by a Seller");
            }
            return new SellerWarehouse(entity.getIdentifier(), entity.getName(), address, seller);
        }
        throw new IllegalStateException("Unsupported stored warehouse type: "
                + entity.getClass().getSimpleName());
    }
}
