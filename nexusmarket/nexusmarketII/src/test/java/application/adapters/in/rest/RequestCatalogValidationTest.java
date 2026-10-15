package application.adapters.in.rest;

import application.adapters.in.rest.requests.AddCartItemRequest;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.InventoryMovementRequest;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.requests.RegisterUserRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bean Validation of the request DTOs: catalog codes must be exact
 * catalog values and quantities and amounts must stay within range,
 * before the request ever reaches the domain.
 */
class RequestCatalogValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void statusMustBeAnExactCatalogCode() {
        assertFalse(validator.validate(new ChangeStatusRequest("active")).isEmpty());
        assertFalse(validator.validate(new ChangeStatusRequest("CLOSED")).isEmpty());
        assertTrue(validator.validate(new ChangeStatusRequest("SUSPENDED")).isEmpty());
        assertTrue(validator.validate(new ChangeStatusRequest("BLOCKED")).isEmpty());
    }

    @Test
    void roleMustBeASystemRoleCode() {
        assertFalse(validator.validate(new RegisterUserRequest("op-9", "Olga", "op9@x.com",
                "OPERATOR", "Passw0rd1")).isEmpty());
        assertTrue(validator.validate(new RegisterUserRequest("op-9", "Olga", "op9@x.com",
                "LOGISTICS_OPERATOR", "Passw0rd1")).isEmpty());
    }

    @Test
    void movementTypeAndQuantityAreValidated() {
        assertFalse(validator.validate(new InventoryMovementRequest("product-1", "warehouse-1",
                "TRANSFER", 5)).isEmpty());
        assertFalse(validator.validate(new InventoryMovementRequest("product-1", "warehouse-1",
                "STOCK_IN", 1_000_000)).isEmpty());
        assertTrue(validator.validate(new InventoryMovementRequest("product-1", "warehouse-1",
                "ADJUSTMENT", -3)).isEmpty());
    }

    @Test
    void cartQuantityAndProductPriceStayWithinRange() {
        assertFalse(validator.validate(new AddCartItemRequest("product-1", 0)).isEmpty());
        assertFalse(validator.validate(new AddCartItemRequest("product-1", 5000)).isEmpty());
        assertTrue(validator.validate(new AddCartItemRequest("product-1", 2)).isEmpty());

        assertFalse(validator.validate(new PublishProductRequest("product-9", "Mouse", new BigDecimal("10.999"),
                null, false, null, List.of())).isEmpty());
        assertTrue(validator.validate(new PublishProductRequest("product-9", "Mouse", new BigDecimal("89900.00"),
                null, false, null, List.of())).isEmpty());
    }
}
