package application.domain.services.catalog;

import application.domain.enums.BusinessOperation;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Person;
import application.domain.models.Product;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.ProductRepository;
import application.domain.services.ServiceValidations;
import application.domain.services.authorization.ValidateOwnershipAccessService;
import application.domain.services.authorization.ValidatePermissionsService;
import application.domain.valueobjects.ProductStatus;

/**
 * ChangeProductStatusService
 *
 * Catalog service "Change Product Status". The
 * owner seller or an Administrator may suspend, re-publish or permanently
 * discontinue a product; a discontinued product can never change its
 * status again.
 */
public class ChangeProductStatusService {

    private final ProductRepository productRepository;
    private final ValidatePermissionsService permissionsService;
    private final ValidateOwnershipAccessService ownershipService;
    private final NotificationService notificationService;

    public ChangeProductStatusService(ProductRepository productRepository,
                                      ValidatePermissionsService permissionsService,
                                      ValidateOwnershipAccessService ownershipService,
                                      NotificationService notificationService) {
        if (productRepository == null || permissionsService == null || ownershipService == null
                || notificationService == null) {
            throw new IllegalArgumentException("ChangeProductStatusService requires its dependencies");
        }
        this.productRepository = productRepository;
        this.permissionsService = permissionsService;
        this.ownershipService = ownershipService;
        this.notificationService = notificationService;
    }

    public Product changeProductStatus(String requesterId, String productId, ProductStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Product status must not be null");
        }
        Person requester = permissionsService.requirePermission(requesterId,
                BusinessOperation.CHANGE_PRODUCT_STATUS);
        ServiceValidations.requireText(productId, "product id");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
        ownershipService.requireProductAccess(requester, product);
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
}
