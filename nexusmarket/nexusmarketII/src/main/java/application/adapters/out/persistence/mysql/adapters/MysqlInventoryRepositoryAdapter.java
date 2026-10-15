package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.InventoryMapper;
import application.adapters.out.persistence.mysql.repositories.InventoryJpaRepository;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.WarehouseRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MySQL persistence adapter for the InventoryRepository output port.
 * Quantities are stored relationally; the movement history is stored in
 * MongoDB through a separate output port.
 */
@Repository
@Profile("!memory")
public class MysqlInventoryRepositoryAdapter implements InventoryRepository {

    private final InventoryJpaRepository jpaRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public MysqlInventoryRepositoryAdapter(InventoryJpaRepository jpaRepository,
                                           ProductRepository productRepository,
                                           WarehouseRepository warehouseRepository) {
        this.jpaRepository = jpaRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional
    public void save(Inventory inventory) {
        jpaRepository.findById(inventory.getIdentifier()).ifPresentOrElse(
                existing -> {
                    InventoryMapper.updateEntity(existing, inventory);
                    jpaRepository.save(existing);
                },
                () -> jpaRepository.save(InventoryMapper.toEntity(inventory)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> findById(String identifier) {
        return jpaRepository.findById(identifier).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findByProductId(String productId) {
        return jpaRepository.findByProductId(productId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId) {
        return jpaRepository.findByProductIdAndWarehouseId(productId, warehouseId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Inventory toDomain(application.adapters.out.persistence.mysql.entities.InventoryEntity entity) {
        Product product = productRepository.findById(entity.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product '" + entity.getProductId()
                        + "' referenced by inventory does not exist"));
        Warehouse warehouse = warehouseRepository.findById(entity.getWarehouseId())
                .orElseThrow(() -> new IllegalStateException("Warehouse '" + entity.getWarehouseId()
                        + "' referenced by inventory does not exist"));
        return InventoryMapper.toDomain(entity, product, warehouse);
    }
}
