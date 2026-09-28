package Application.domain.ports.out;

import Application.domain.models.Order;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence contract for Orders.
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(String orderId);

    /** All orders placed by the given buyer (Manage Buyer / reports). */
    List<Order> findByBuyerId(String buyerId);

    /** Every order in the platform (Administrative Reporting). */
    List<Order> findAll();
}
