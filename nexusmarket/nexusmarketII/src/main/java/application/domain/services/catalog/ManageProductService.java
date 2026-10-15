package application.domain.services.catalog;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.OwnershipAccessDeniedException;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.models.ProductVariant;
import application.domain.ports.out.ProductRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.SystemRole;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * ManageProductService
 *
 * Catalog service "Manage Product". Published
 * products are part of the public catalog, which can be searched by text
 * and seller; suspended and discontinued products are hidden from it and
 * only visible to their seller, Administrators and Supervisors. Only the
 * owner seller updates a product.
 */
public class ManageProductService {

    private final ProductRepository productRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;

    public ManageProductService(ProductRepository productRepository,
                                ValidatePermissionsService permissionsService,
                                ValidateOwnershipAccessService ownershipService) {
        if (productRepository == null || permissionsService == null || ownershipService == null) {
            throw new IllegalArgumentException("ManageProductService requires its dependencies");
        }
        this.productRepository = productRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
    }

    /**
     * Published products are public. Suspended or discontinued products
     * are only visible to their seller, Administrators and Supervisors.
     *
     * @param requesterId identifier of the requesting user (null for anonymous catalog access)
     */
    public Product consultProduct(String requesterId, String productId) {
        Product product = findProduct(productId);
        if (product.isAvailable()) {
            return product;
        }
        // Hidden from the public catalog: only its seller and administrative roles see it.
        if (requesterId == null || requesterId.isBlank()) {
            throw new IllegalArgumentException("Product '" + productId + "' is not available in the catalog");
        }
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_PRODUCT);
        if (!ownershipService.canAccessProduct(requester, product)) {
            throw new IllegalArgumentException("Product '" + productId + "' is not available in the catalog");
        }
        return product;
    }

    /**
     * @param price    new catalog unit price (null keeps the current price)
     * @param variants replaces the product variants (null keeps them)
     */
    public Product updateProduct(String requesterId, String productId, String name, String description,
                                 BigDecimal price, List<ProductVariant> variants) {
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.UPDATE_PRODUCT);
        Product product = findProduct(productId);
        ownershipService.requireProductAccess(requester, product);

        product.updateInformation(name, description);
        if (price != null) {
            product.changePrice(price);
        }
        if (variants != null) {
            product.replaceVariants(variants);
        }
        productRepository.save(product);
        return product;
    }

    /**
     * Searches the public catalog: only PUBLISHED products, sorted by name.
     *
     * @param text     optional text contained in the name or description (case-insensitive)
     * @param sellerId optional seller filter
     */
    public List<Product> searchCatalog(String text, String sellerId) {
        String normalizedText = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        List<Product> candidates = sellerId == null || sellerId.isBlank()
                ? productRepository.findAll()
                : productRepository.findBySellerId(sellerId);
        return candidates.stream()
                .filter(Product::isAvailable)
                .filter(product -> normalizedText.isEmpty() || matches(product, normalizedText))
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Lists every product of a seller, in any status. A seller only lists
     * its own products; Administrators and Supervisors list any seller.
     */
    public List<Product> consultSellerProducts(String requesterId, String sellerId) {
        ServiceValidations.requireText(sellerId, "seller id");
        Person requester = permissionsService.requirePermission(requesterId, BusinessOperation.CONSULT_SELLER);
        if (requester.getRole() == SystemRole.SELLER && !requester.getIdentifier().equals(sellerId)) {
            throw new OwnershipAccessDeniedException(requester.getIdentifier(),
                    "products of seller '" + sellerId + "'");
        }
        return productRepository.findBySellerId(sellerId).stream()
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static boolean matches(Product product, String normalizedText) {
        return product.getName().toLowerCase(Locale.ROOT).contains(normalizedText)
                || (product.getDescription() != null
                && product.getDescription().toLowerCase(Locale.ROOT).contains(normalizedText));
    }

    private Product findProduct(String productId) {
        ServiceValidations.requireText(productId, "product id");
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
    }
}
