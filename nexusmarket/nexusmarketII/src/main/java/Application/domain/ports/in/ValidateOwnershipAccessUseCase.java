package Application.domain.ports.in;

import Application.domain.models.Buyer;
import Application.domain.models.Order;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.models.Warehouse;

/**
 * Input port (use case): determines whether a user may access or operate
 * on information belonging to a buyer, seller, warehouse or order that it
 * owns or manages. The require* variants throw
 * OwnershipAccessDeniedException when access is not allowed.
 */
public interface ValidateOwnershipAccessUseCase {

    boolean canAccessPerson(Person user, Person target);

    boolean canAccessBuyer(Person user, Buyer buyer);

    boolean canAccessSeller(Person user, Seller seller);

    boolean canAccessWarehouse(Person user, Warehouse warehouse);

    boolean canAccessProduct(Person user, Product product);

    boolean canAccessOrder(Person user, Order order);

    void requirePersonAccess(Person user, Person target);

    void requireBuyerAccess(Person user, Buyer buyer);

    void requireSellerAccess(Person user, Seller seller);

    void requireWarehouseAccess(Person user, Warehouse warehouse);

    void requireProductAccess(Person user, Product product);

    void requireOrderAccess(Person user, Order order);
}
