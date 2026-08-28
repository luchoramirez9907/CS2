package Application.domain.services;

import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.ports.in.PublishProductUseCase;
import Application.domain.ports.out.NotificationService;
import Application.domain.ports.out.ProductRepository;
import Application.domain.ports.out.SellerRepository;

/**
 * ProductCatalogService
 *
 * Implements the PublishProductUseCase. A product is always published by
 * a Seller, must be unique and must enter the catalog as PUBLISHED.
 */
public class ProductCatalogService implements PublishProductUseCase {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public ProductCatalogService(SellerRepository sellerRepository,
                                 ProductRepository productRepository,
                                 NotificationService notificationService) {
        if (sellerRepository == null || productRepository == null || notificationService == null) {
            throw new IllegalArgumentException("ProductCatalogService requires its dependencies");
        }
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
}
