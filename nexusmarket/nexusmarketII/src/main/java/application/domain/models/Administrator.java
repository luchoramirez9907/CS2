package application.domain.models;

import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

/**
 * Administrator
 *
 * Represents the participant responsible for administering sellers and
 * warehouses within the Marketplace. Registers Sellers and
 * MarketplaceWarehouses.
 */
public class Administrator extends Person {

    public Administrator(String identifier, String fullName, String email, UserStatus status) {
        super(identifier, fullName, email, SystemRole.ADMINISTRATOR, status);
    }
}
