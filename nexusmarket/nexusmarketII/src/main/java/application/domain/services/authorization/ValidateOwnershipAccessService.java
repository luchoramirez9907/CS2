package application.domain.services.authorization;

import application.domain.exceptions.OwnershipAccessDeniedException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.SellerWarehouse;
import application.domain.models.Warehouse;
import application.domain.valueobjects.SystemRole;

/**
 * ValidateOwnershipAccessService
 *
 * Catalog service "Validate Ownership Access". Decides whether a person may
 * access information that belongs to a specific buyer, seller, warehouse,
 * product or order (RG-03):
 *
 * - Administrators and Supervisors may access every entity (supervisors
 *   only for consultation, enforced by the permission matrix).
 * - Buyers only access their own information and orders.
 * - Sellers only access their own information, products and warehouses,
 *   and the orders that contain their products.
 * - Logistics operators access warehouses and the orders whose shipment
 *   is assigned to them.
 */
public class ValidateOwnershipAccessService {

    public boolean canAccessPerson(Person user, Person target) {
        if (user == null || target == null) {
            return false;
        }
        return isAdministrativeRole(user) || sameIdentity(user, target);
    }

    public boolean canAccessBuyer(Person user, Buyer buyer) {
        return canAccessPerson(user, buyer);
    }

    public boolean canAccessSeller(Person user, Seller seller) {
        return canAccessPerson(user, seller);
    }

    public boolean canAccessWarehouse(Person user, Warehouse warehouse) {
        if (user == null || warehouse == null) {
            return false;
        }
        if (isAdministrativeRole(user) || user.getRole() == SystemRole.LOGISTICS_OPERATOR) {
            return true;
        }
        if (user.getRole() == SystemRole.SELLER) {
            return warehouse instanceof SellerWarehouse sellerWarehouse
                    && sameIdentity(user, sellerWarehouse.getOwner());
        }
        return false;
    }

    public boolean canAccessProduct(Person user, Product product) {
        if (user == null || product == null) {
            return false;
        }
        return isAdministrativeRole(user) || sameIdentity(user, product.getSeller());
    }

    public boolean canAccessOrder(Person user, Order order) {
        if (user == null || order == null) {
            return false;
        }
        if (isAdministrativeRole(user)) {
            return true;
        }
        if (user.getRole() == SystemRole.BUYER) {
            return sameIdentity(user, order.getBuyer());
        }
        if (user.getRole() == SystemRole.SELLER) {
            return order.containsProductsOf(user.getIdentifier());
        }
        if (user.getRole() == SystemRole.LOGISTICS_OPERATOR) {
            return order.getShipment() != null
                    && sameIdentity(user, order.getShipment().getLogisticsOperator());
        }
        return false;
    }

    public void requirePersonAccess(Person user, Person target) {
        if (!canAccessPerson(user, target)) {
            throw denied(user, "user '" + identifierOf(target) + "'");
        }
    }

    public void requireBuyerAccess(Person user, Buyer buyer) {
        if (!canAccessBuyer(user, buyer)) {
            throw denied(user, "buyer '" + identifierOf(buyer) + "'");
        }
    }

    public void requireSellerAccess(Person user, Seller seller) {
        if (!canAccessSeller(user, seller)) {
            throw denied(user, "seller '" + identifierOf(seller) + "'");
        }
    }

    public void requireWarehouseAccess(Person user, Warehouse warehouse) {
        if (!canAccessWarehouse(user, warehouse)) {
            throw denied(user, "warehouse '" + (warehouse == null ? null : warehouse.getIdentifier()) + "'");
        }
    }

    public void requireProductAccess(Person user, Product product) {
        if (!canAccessProduct(user, product)) {
            throw denied(user, "product '" + (product == null ? null : product.getIdentifier()) + "'");
        }
    }

    public void requireOrderAccess(Person user, Order order) {
        if (!canAccessOrder(user, order)) {
            throw denied(user, "order '" + (order == null ? null : order.getOrderId()) + "'");
        }
    }

    private boolean isAdministrativeRole(Person user) {
        return user.getRole() == SystemRole.ADMINISTRATOR || user.getRole() == SystemRole.SUPERVISOR;
    }

    private boolean sameIdentity(Person first, Person second) {
        return first != null && second != null
                && first.getIdentifier().equals(second.getIdentifier());
    }

    private String identifierOf(Person person) {
        return person == null ? null : person.getIdentifier();
    }

    private OwnershipAccessDeniedException denied(Person user, String resource) {
        return new OwnershipAccessDeniedException(identifierOf(user), resource);
    }
}
