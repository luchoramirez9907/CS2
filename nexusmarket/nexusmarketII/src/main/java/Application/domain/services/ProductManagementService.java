package Application.domain.services;

import Application.domain.enums.BusinessOperation;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.ProductVariant;
import Application.domain.ports.in.ChangeProductStatusUseCase;
import Application.domain.ports.in.ManageProductUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.ProductRepository;
import Application.domain.valueobjects.ProductStatus;

import java.util.List;

/**
 * ProductManagementService
 *
 * Implements ManageProductUseCase and ChangeProductStatusUseCase.
 * Published products are part of the public catalog; suspended and
 * discontinued products are hidden from it and only visible to their
 * seller, Administrators and Supervisors. Only the owner seller updates a
 * product; the owner seller or an Administrator may suspend, re-publish
 * or permanently discontinue it. Publication is handled by
 * ProductCatalogService.
 */
public class ProductManagementService implements ManageProductUseCase, ChangeProductStatusUseCase {

    private final ProductRepository productRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public ProductManagementService(ProductRepository productRepository,
                                    AuthorizationService authorizationService,
                                    NotificationService notificationService) {
        if (productRepository == null || authorizationService == null || notificationService == null) {
            throw new IllegalArgumentException("ProductManagementService requires its dependencies");
        }
        this.productRepository = productRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Override
    public Product consultProduct(String requesterId, String productId) {
        Product product = findProduct(productId);
        if (product.isAvailable()) {
            return product;
        }
        // Hidden from the public catalog: only its seller and administrative roles see it.
        if (requesterId == null || requesterId.isBlank()) {
            throw new IllegalArgumentException("Product '" + productId + "' is not available in the catalog");
        }
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.CONSULT_PRODUCT);
        if (!authorizationService.canAccessProduct(requester, product)) {
            throw new IllegalArgumentException("Product '" + productId + "' is not available in the catalog");
        }
        return product;
    }

    @Override
    public Product updateProduct(String requesterId, String productId, String name, String description,
                                 List<ProductVariant> variants) {
        Person requester = authorizationService.requirePermission(requesterId, BusinessOperation.UPDATE_PRODUCT);
        Product product = findProduct(productId);
        authorizationService.requireProductAccess(requester, product);

        product.updateInformation(name, description);
        if (variants != null) {
            product.replaceVariants(variants);
        }
        productRepository.save(product);
        return product;
    }

    @Override
    public Product changeProductStatus(String requesterId, String productId,
                                       ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Product status must not be null");
        }
        Person requester = authorizationService.requirePermission(requesterId,
                BusinessOperation.CHANGE_PRODUCT_STATUS);
        Product product = findProduct(productId);
        authorizationService.requireProductAccess(requester, product);
        if (product.getStatus().equals(status)) {
            throw new IllegalArgumentException("Product '" + productId + "' is already "
                    + status.getCode());
        }

        product.changeStatus(status);
        productRepository.save(product);
        notificationService.notify(product.getSeller(), "Product status changed",
                "Product '" + product.getName() + "' is now " + status.getName());
        return product;
    }

    private Product findProduct(String productId) {
        ServiceValidations.requireText(productId, "product id");
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product '" + productId + "' does not exist"));
    }
}
