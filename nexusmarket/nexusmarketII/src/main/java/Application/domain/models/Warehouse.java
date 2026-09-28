package Application.domain.models;

import Application.domain.valueobjects.Address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Warehouse (Abstract)
 *
 * Represents a physical location where inventory is stored and managed.
 * The system distinguishes between warehouses owned by the Marketplace
 * and warehouses owned by a Seller (genuine specializations, not a
 * generic type field).
 */
public abstract class Warehouse {

    private final String identifier;
    private String name;
    private Address address;
    private final List<Inventory> inventoryRecords = new ArrayList<>();

    protected Warehouse(String identifier, String name, Address address) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Warehouse identifier must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name must not be null or blank");
        }
        if (address == null) {
            throw new IllegalArgumentException("Warehouse address must not be null");
        }
        this.identifier = identifier;
        this.name = name;
        this.address = address;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Updates the descriptive information of the warehouse. Ownership
     * (Marketplace or Seller) never changes.
     */
    public void updateInformation(String name, Address address) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name must not be null or blank");
        }
        if (address == null) {
            throw new IllegalArgumentException("Warehouse address must not be null");
        }
        this.name = name;
        this.address = address;
    }

    public List<Inventory> getInventoryRecords() {
        return Collections.unmodifiableList(inventoryRecords);
    }

    public void addInventoryRecord(Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory record must not be null");
        }
        if (!this.equals(inventory.getWarehouse())) {
            throw new IllegalArgumentException("Inventory record does not belong to this warehouse");
        }
        this.inventoryRecords.add(inventory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Warehouse warehouse = (Warehouse) o;
        return identifier.equals(warehouse.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
