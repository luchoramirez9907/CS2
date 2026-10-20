package application.adapters.in.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Role isolation over HTTP: a user logged in with one role can neither enter
 * the routes nor run the processes of another role (403), and within the same
 * role nobody accesses data owned by someone else.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"memory", "demo"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RoleIsolationIntegrationTest {

    /** Demo account of each role and one read route that belongs to it. */
    private static final Map<String, String> ACCOUNTS = Map.of(
            "buyer", "buyer@nexusmarket.com",
            "seller", "seller@nexusmarket.com",
            "operator", "operator@nexusmarket.com",
            "admin", "admin@nexusmarket.com",
            "supervisor", "supervisor@nexusmarket.com");

    private static final Map<String, String> OWN_ROUTE = Map.of(
            "buyer", "/api/v1/buyer/profile",
            "seller", "/api/v1/seller/profile",
            "operator", "/api/v1/operator/profile",
            "admin", "/api/v1/admin/users",
            "supervisor", "/api/v1/supervisor/users");

    private static final List<String> ROLES = List.of("buyer", "seller", "operator", "admin", "supervisor");

    private static final String CART_ITEM = "{\"productId\":\"product-1\",\"quantity\":1}";

    @Autowired
    private MockMvc mockMvc;

    @Value("${nexusmarket.demo.password}")
    private String demoPassword;

    private final Map<String, String> tokens = new HashMap<>();

    @BeforeAll
    void loginEveryRole() throws Exception {
        for (String role : ROLES) {
            tokens.put(role, login(ACCOUNTS.get(role), demoPassword));
        }
    }

    /** 5 x 5 matrix: 200 on the role's own routes, 403 on the routes of the other four roles. */
    static Stream<Arguments> roleRouteMatrix() {
        return ROLES.stream().flatMap(role -> ROLES.stream().map(routeOwner -> Arguments.of(role, routeOwner)));
    }

    @ParameterizedTest(name = "{0} en rutas de {1}")
    @MethodSource("roleRouteMatrix")
    void eachRoleOnlyEntersItsOwnRoutes(String role, String routeOwner) throws Exception {
        int expected = role.equals(routeOwner) ? 200 : 403;
        mockMvc.perform(get(OWN_ROUTE.get(routeOwner)).header(HttpHeaders.AUTHORIZATION, bearer(tokens.get(role))))
                .andExpect(status().is(expected));
    }

    /** Processes (state changes) of one role attempted with another role. */
    static Stream<Arguments> crossRoleProcesses() {
        return Stream.of(
                Arguments.of("buyer", "POST", "/api/v1/admin/orders/any-order/payment", "{}"),
                Arguments.of("buyer", "POST", "/api/v1/seller/products",
                        "{\"identifier\":\"p-x\",\"name\":\"X\",\"price\":10,\"digital\":false}"),
                Arguments.of("seller", "POST", "/api/v1/buyer/orders", "{}"),
                Arguments.of("seller", "POST", "/api/v1/admin/returns/any-return/approval", "{}"),
                Arguments.of("operator", "POST", "/api/v1/admin/users",
                        "{\"identifier\":\"u-x\",\"fullName\":\"X\",\"email\":\"x@x.com\","
                                + "\"role\":\"SUPERVISOR\",\"password\":\"Passw0rd1\"}"),
                Arguments.of("operator", "POST", "/api/v1/buyer/cart/items", CART_ITEM),
                Arguments.of("supervisor", "POST", "/api/v1/admin/orders/any-order/payment", "{}"),
                Arguments.of("supervisor", "PATCH", "/api/v1/admin/users/buyer-1/status", "{\"status\":\"BLOCKED\"}"),
                Arguments.of("admin", "POST", "/api/v1/buyer/orders", "{}"),
                Arguments.of("admin", "POST", "/api/v1/seller/products",
                        "{\"identifier\":\"p-y\",\"name\":\"Y\",\"price\":10,\"digital\":false}"),
                Arguments.of("admin", "POST", "/api/v1/operator/orders/any-order/shipment/dispatch", "{}"));
    }

    @ParameterizedTest(name = "{0} intenta {1} {2}")
    @MethodSource("crossRoleProcesses")
    void processesOfAnotherRoleAreForbidden(String role, String method, String path, String body) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokens.get(role)))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void aBuyerCannotSeeTheOrderOfAnotherBuyer() throws Exception {
        String buyer = tokens.get("buyer");
        mockMvc.perform(post("/api/v1/buyer/cart/items").header(HttpHeaders.AUTHORIZATION, bearer(buyer))
                        .contentType(MediaType.APPLICATION_JSON).content(CART_ITEM))
                .andExpect(status().isCreated());
        String checkout = mockMvc.perform(post("/api/v1/buyer/orders").header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(checkout, "$.data.orderId");

        mockMvc.perform(post("/api/v1/public/buyers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"buyer-other\",\"fullName\":\"Otro Comprador\","
                                + "\"email\":\"other@x.com\",\"password\":\"Passw0rd1\",\"primaryAddress\":{"
                                + "\"street\":\"Calle 1\",\"city\":\"Medellin\",\"state\":\"Antioquia\","
                                + "\"country\":\"Colombia\",\"postalCode\":\"050001\"}}"))
                .andExpect(status().isCreated());
        String otherBuyer = login("other@x.com", "Passw0rd1");

        mockMvc.perform(get("/api/v1/buyer/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(otherBuyer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/buyer/orders/" + orderId).header(HttpHeaders.AUTHORIZATION, bearer(buyer)))
                .andExpect(status().isOk());
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.data.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
