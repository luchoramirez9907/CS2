package application.adapters.in.rest;

import application.adapters.in.rest.mappers.AuthDtoMapper;
import application.adapters.in.rest.mappers.PersonDtoMapper;
import application.adapters.in.rest.mappers.ProductDtoMapper;
import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.requests.PublishProductRequest;
import application.adapters.in.rest.responses.LoginResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.models.Administrator;
import application.domain.models.AuthenticationResult;
import application.domain.models.DigitalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of the REST mappers (Request DTO → Domain Model → Response DTO).
 */
class RestMappersTest {

    private final Seller seller = new Seller("seller-1", "TechStore", "seller@x.com",
            new Administrator("admin-1", "Ada Admin", "admin@x.com", UserStatus.ACTIVE), UserStatus.ACTIVE);

    @Test
    void publishRequestBuildsTheRightProductSpecialization() {
        PublishProductRequest request = new PublishProductRequest("ebook-1", "Ebook", new BigDecimal("10.00"),
                "Guide", true, "https://download.example/ebook-1",
                List.of(new PublishProductRequest.VariantRequest("Format", "PDF")));

        Product product = ProductDtoMapper.toDomain(request, seller);
        assertInstanceOf(DigitalProduct.class, product);
        assertEquals(seller, product.getSeller());
        assertEquals(new BigDecimal("10.00"), product.getPrice());
        assertEquals(1, product.getVariants().size());

        ProductResponse response = ProductDtoMapper.toResponse(product);
        assertTrue(response.digital());
        assertEquals("PUBLISHED", response.status());
        assertEquals("seller-1", response.sellerId());
        assertEquals(new BigDecimal("10.00"), response.price());
    }

    @Test
    void addressesMapBothWaysAndNullMeansKeep() {
        Address address = PersonDtoMapper.toAddress(
                new AddressRequest("Calle 1", "Bogota", "CUND", "Colombia", "110111"));
        assertEquals(new Address("Calle 1", "Bogota", "CUND", "Colombia", "110111"), address);
        assertEquals("Bogota", PersonDtoMapper.toResponse(address).city());
        assertNull(PersonDtoMapper.toAddress(null));
        assertNull(PersonDtoMapper.toAddresses(null));
        assertNull(ProductDtoMapper.toVariants(null));
    }

    @Test
    void loginResponseExposesTheTokenButNotThePasswordHash() {
        User user = new User(seller, "hashed:secret");
        LoginResponse response = AuthDtoMapper.toResponse(new AuthenticationResult(user, "signed.jwt.token", 3600));
        assertEquals("signed.jwt.token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("SELLER", response.user().role());
        assertFalse(response.toString().contains("signed.jwt.token"));
        assertFalse(response.user().toString().contains("hashed"));
    }
}
