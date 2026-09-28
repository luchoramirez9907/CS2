package Application.domain.services;

import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.ports.in.ChangeProductStatusUseCase;
import Application.domain.ports.in.ManageProductUseCase;
import Application.domain.ports.in.PublishProductUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.PersonRepository;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.SellerRepository;
import Application.domain.valueobjects.ProductStatus;
import Application.domain.valueobjects.SystemRole;

/**
 * ProductCatalogService
 *
 * Implements the Catalog Management services (per SDD - Services):
 * - Publish Product: a product is always published by a Seller, must be
 *   unique and enters the catalog as PUBLISHED.
 * - Manage Product: consults a product and updates its description
 *   (owning seller only).
 * - Change Product Status: suspends or permanently discontinues a
 *   product, hiding it from the public catalog.
 */
public class ProductCatalogService implements PublishProductUseCase, ManageProductUseCase, ChangeProductStatusUseCase {

    private final PersonRepository personRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public ProductCatalogService(PersonRepository personRepository,
                                 SellerRepository sellerRepository,
                                 ProductRepository productRepository,
                                 NotificationService notificationService) {
        if (personRepository == null || sellerRepository == null
                || productRepository == null || notificationService == null) {
            throw new IllegalArgumentException("ProductCatalogService requires its dependencies");
        }
        this.personRepository = personRepository;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Product publishProduct(String sellerId, Product product) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller id must not be null or blank");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller '" + sellerId + "' does not exist"));
        seller.requireActive();

        if (!seller.equals(product.getSeller())) {
            throw new IllegalArgumentException("Product must be published by the given seller");
        }
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("A published product must enter the catalog as PUBLISHED");
        }
        if (productRepository.findById(product.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("A product with identifier '"
                    + product.getIdentifier() + "' already exists");
        }

        seller.addProduct(product);
        productRepository.save(product);
        notificationService.notify(seller, "Product published",
                "Product '" + product.getName() + "' is now visible in the catalog");
        return product;
    }

    @Override
    public Product consultProduct(String requesterId, String productId) {
        requireText(requesterId, "requester id");
        requireText(productId, "product id");

        Person requester = personRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Requester '" + requesterId + "' does not exist"));
        requester.requireActive();
        return requireProduct(productId);
    }

    @Override
    public Product updateProductDescription(String performerId, String productId, String newDescription) {
        requireText(performerId, "performer id");
        requireText(productId, "product id");
        requireText(newDescription, "new description");

        Seller performer = requireOwningSeller(performerId, productId);
        Product product = requireProduct(productId);
        product.updateDescription(newDescription);
        productRepository.save(product);
        notificationService.notify(performer, "Product updated",
                "Product '" + product.getName() + "' description was updated");
        return product;
    }

    @Override
    public Product changeProductStatus(String performerId, String productId, String statusCode) {
        requireText(performerId, "performer id");
        requireText(productId, "product id");
        requireText(statusCode, "status code");

        Seller performer = requireOwningSeller(performerId, productId);
        Product product = requireProduct(productId);
        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("Product '" + productId
                    + "' is discontinued; the change is permanent and cannot be reverted");
        }
        product.changeStatus(ProductStatus.fromCode(statusCode));
        productRepository.save(product);
        notificationService.notify(performer, "Product status changed",
                "Product '" + product.getName() + "' is now " + product.getStatus().getCode());
        return product;
    }

    private Product requireProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product '" + productId + "' does not exist"));
    }

    private Seller requireOwningSeller(String performerId, String productId) {
        Seller seller = sellerRepository.findById(performerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller '" + performerId + "' does not exist"));
        seller.requireActive();
        if (!seller.getIdentifier().equals(performerId)) {
            throw new Application.domain.exceptions.InvalidRoleAssignmentException(
                    "manage a product", seller.getRole(), "the owning SELLER");
        }
        return seller;
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}
