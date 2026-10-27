package application.adapters.in.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level tests of the REST adapters and the security boundary, on the
 * real Spring context (in-memory persistence and demo data): JWT
 * authentication, role-based access, the error envelope, validation, CORS
 * and request correlation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"memory", "demo"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RestApiIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Value("${nexusmarket.demo.password}")
    private String demoPassword;

    // ---------------------------------------------------------------- Public access

    @Test
    void healthAndCatalogRequireAToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        mockMvc.perform(get("/api/v1/public/products/product-1"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/public/products").param("q", "keyboard"))
                .andExpect(status().isUnauthorized());

        String buyerToken = login("buyer@nexusmarket.com");
        mockMvc.perform(get("/actuator/health").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/products/product-1").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.identifier").value("product-1"))
                .andExpect(jsonPath("$.data.price").value(249900.00));
    }

    @Test
    void systemMovementTypesCannotBeRegisteredManually() throws Exception {
        String operatorToken = login("operator@nexusmarket.com");
        for (String type : List.of("RESERVATION", "RESERVATION_RELEASE", "RETURN", "SALE_EXIT")) {
            mockMvc.perform(post("/api/v1/operator/inventory/movements")
                            .header(HttpHeaders.AUTHORIZATION, bearer(operatorToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"productId\":\"product-1\",\"warehouseId\":\"warehouse-1\","
                                    + "\"movementType\":\"" + type + "\",\"quantity\":1}"))
                    .andExpect(status().isBadRequest());
        }
        // Manual movements keep working.
        mockMvc.perform(post("/api/v1/operator/inventory/movements")
                        .header(HttpHeaders.AUTHORIZATION, bearer(operatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"product-1\",\"warehouseId\":\"warehouse-1\","
                                + "\"movementType\":\"STOCK_IN\",\"quantity\":5}"))
                .andExpect(status().isCreated());
    }

    @Test
    void duplicateProductAndWarehouseReturn409() throws Exception {
        String sellerToken = login("seller@nexusmarket.com");
        mockMvc.perform(post("/api/v1/seller/products").header(HttpHeaders.AUTHORIZATION, bearer(sellerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"product-1\",\"name\":\"Copia\",\"price\":10,\"digital\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));

        String adminToken = login("admin@nexusmarket.com");
        mockMvc.perform(post("/api/v1/admin/warehouses").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"warehouse-1\",\"name\":\"Copia\",\"address\":{\"street\":\"Calle 1\","
                                + "\"city\":\"Medellin\",\"state\":\"Antioquia\",\"country\":\"Colombia\","
                                + "\"postalCode\":\"050001\"}}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));
    }

    @Test
    void unsupportedContentTypeReturns415InsteadOf500() throws Exception {
        String adminToken = login("admin@nexusmarket.com");
        mockMvc.perform(post("/api/v1/admin/returns/any-return/refund")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Content-Type must be application/json"));
    }

    @Test
    void onlyLoginAndBuyerRegistrationWorkWithoutAToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("seller@nexusmarket.com")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/public/buyers").contentType(MediaType.APPLICATION_JSON)
                        .content(buyerRegistration("buyer-open", "open@x.com")))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/public/products/product-1").header(HttpHeaders.AUTHORIZATION, "Bearer bad.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoutesWithoutTokenReturn401WithTheErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/buyer/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/api/v1/buyer/profile"))
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = login("buyer@nexusmarket.com");
        mockMvc.perform(get("/api/v1/buyer/profile").header(HttpHeaders.AUTHORIZATION, "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void catalogSearchAndListingsOverHttp() throws Exception {
        String adminToken = login("admin@nexusmarket.com");
        mockMvc.perform(get("/api/v1/public/products").param("q", "keyboard")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].identifier").value("product-1"));
        mockMvc.perform(get("/api/v1/admin/users").param("role", "SELLER")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
        mockMvc.perform(get("/api/v1/admin/inventory/movements").param("productId", "product-1")
                        .param("warehouseId", "warehouse-1").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].movementType").value("STOCK_IN"));
        mockMvc.perform(get("/api/v1/admin/orders").param("status", "NOT_A_STATUS")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    // ---------------------------------------------------------------- Authentication

    @Test
    void loginWithWrongPasswordReturns401InvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"buyer@nexusmarket.com\",\"password\":\"WrongPass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@nexusmarket.com\",\"password\":\"WrongPass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void loginReturnsABearerTokenWithoutPersonalDataInTheClaims() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("seller@nexusmarket.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600))
                .andExpect(jsonPath("$.data.user.role").value("SELLER"))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.data.token");
        String claims = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        org.junit.jupiter.api.Assertions.assertFalse(claims.contains("seller@nexusmarket.com"));
        org.junit.jupiter.api.Assertions.assertFalse(claims.contains("SELLER"));
    }

    @Test
    void logoutInvalidatesThePreviousToken() throws Exception {
        String token = login("buyer@nexusmarket.com");
        mockMvc.perform(get("/api/v1/buyer/profile").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/buyer/profile").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- Role-based access

    @Test
    void aRoleCannotUseTheRoutesOfAnotherRole() throws Exception {
        String buyerToken = login("buyer@nexusmarket.com");
        mockMvc.perform(get("/api/v1/admin/users/administrator-1").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(get("/api/v1/seller/profile").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken)))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- Validation and errors

    @Test
    void invalidAndMalformedBodiesReturn400WithoutReachingTheDomain() throws Exception {
        String buyerToken = login("buyer@nexusmarket.com");
        mockMvc.perform(post("/api/v1/buyer/cart/items").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details", hasSize(2)));
        mockMvc.perform(post("/api/v1/buyer/cart/items").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        // Nothing reached the domain: the buyer still has no cart.
        mockMvc.perform(get("/api/v1/buyer/cart").header(HttpHeaders.AUTHORIZATION, bearer(buyerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void missingResourcesReturn404AndDuplicatesReturn409() throws Exception {
        String adminToken = login("admin@nexusmarket.com");
        mockMvc.perform(get("/api/v1/admin/orders/missing-order").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(post("/api/v1/public/buyers").contentType(MediaType.APPLICATION_JSON)
                        .content(buyerRegistration("buyer-dup", "buyer@nexusmarket.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));
    }

    // ---------------------------------------------------------------- Complete flow over HTTP

    @Test
    void buyerPurchasesThroughTheRestApi() throws Exception {
        mockMvc.perform(post("/api/v1/public/buyers").contentType(MediaType.APPLICATION_JSON)
                        .content(buyerRegistration("buyer-http", "buyer.http@nexusmarket.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.identifier").value("buyer-http"));
        String token = loginWith("buyer.http@nexusmarket.com", "Passw0rd1");

        mockMvc.perform(post("/api/v1/buyer/cart/items").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"product-1\",\"quantity\":1}"))
                .andExpect(status().isCreated());
        String order = mockMvc.perform(post("/api/v1/buyer/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING_PAYMENT"))
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(order, "$.data.orderId");

        mockMvc.perform(get("/api/v1/buyer/orders/" + orderId + "/invoice").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taxAmount").value(39900.00));
        String adminToken = login("admin@nexusmarket.com");
        mockMvc.perform(post("/api/v1/admin/orders/" + orderId + "/payment")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"));
        mockMvc.perform(get("/api/v1/buyer/orders").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
        // The checkout consumed the cart: there is no active cart left to clear.
        mockMvc.perform(delete("/api/v1/buyer/cart").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- CORS and correlation

    @Test
    void corsPreflightIsAllowedOnlyForTheConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/buyer/profile")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
        mockMvc.perform(options("/api/v1/buyer/profile")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void validRequestIdIsEchoedAndInvalidOneIsReplaced() throws Exception {
        mockMvc.perform(get("/api/v1/buyer/profile").header("X-Request-Id", "client-123"))
                .andExpect(header().string("X-Request-Id", "client-123"))
                .andExpect(jsonPath("$.requestId").value("client-123"));
        mockMvc.perform(get("/api/v1/buyer/profile").header("X-Request-Id", "bad id with spaces"))
                .andExpect(header().string("X-Request-Id", startsWith("req-")));
    }

    // ----------------------------------------------------------------

    private String login(String email) throws Exception {
        return loginWith(email, demoPassword);
    }

    private String loginWith(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }

    private String credentials(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + demoPassword + "\"}";
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String buyerRegistration(String identifier, String email) {
        return "{\"identifier\":\"" + identifier + "\",\"fullName\":\"HTTP Buyer\",\"email\":\"" + email + "\","
                + "\"password\":\"Passw0rd1\",\"primaryAddress\":{\"street\":\"Calle 1\",\"city\":\"Bogota\","
                + "\"state\":\"CUND\",\"country\":\"Colombia\",\"postalCode\":\"110111\"}}";
    }
}
