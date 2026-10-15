package application.adapters.out.persistence.memory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Return;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.models.ShoppingCart;
import application.domain.models.User;
import application.domain.models.Warehouse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared in-memory state for the "memory" persistence mode. Enables the
 * full application to run without external databases while keeping the
 * MySQL/MongoDB adapters intact for production mode.
 */
@Component
@Profile("memory")
public class InMemoryDataStore {

    public final Map<String, Person> persons = new HashMap<>();
    public final Map<String, Product> products = new HashMap<>();
    public final Map<String, Warehouse> warehouses = new HashMap<>();
    public final Map<String, Inventory> inventories = new HashMap<>();
    public final Map<String, ShoppingCart> carts = new HashMap<>();
    public final Map<String, Order> orders = new HashMap<>();
    public final Map<String, Return> returns = new HashMap<>();
    public final Map<String, User> users = new HashMap<>();
    public final List<InventoryMovement> movements = Collections.synchronizedList(new ArrayList<>());
    public final List<ShipmentTrackingEvent> trackingEvents = Collections.synchronizedList(new ArrayList<>());
}
