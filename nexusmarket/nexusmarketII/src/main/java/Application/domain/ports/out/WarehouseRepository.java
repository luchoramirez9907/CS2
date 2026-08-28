package Application.domain.ports.out;

import Application.domain.models.Warehouse;

import java.util.Optional;

/**
 * Output port: persistence contract for Warehouses.
 */
public interface WarehouseRepository {

    void save(Warehouse warehouse);

    Optional<Warehouse> findById(String identifier);
}
