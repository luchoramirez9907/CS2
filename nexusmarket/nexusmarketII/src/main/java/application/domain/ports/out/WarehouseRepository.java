package application.domain.ports.out;

import application.domain.models.Warehouse;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence contract for Warehouses.
 */
public interface WarehouseRepository {

    void save(Warehouse warehouse);

    Optional<Warehouse> findById(String identifier);

    List<Warehouse> findAll();
}
