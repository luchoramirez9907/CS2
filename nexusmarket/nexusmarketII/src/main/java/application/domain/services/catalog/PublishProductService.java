package application.domain.services.catalog;

import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.NotificationService;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.SellerRepository;

/**
 * PublishProductService
 *
 * Catalog service "Publish Product". A product is
 * always published by a Seller, must be unique and must enter the catalog as PUBLISHED.
 */
public class PublishProductService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public PublishProductService(SellerRepository sellerRepository,
                                 ProductRepository productRepository,
                                 NotificationService notificationService) {
        if (sellerRepository == null || productRepository == null || notificationService == null) {
            throw new IllegalArgumentException("PublishProductService requires its dependencies");
        }
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    /**
     * @param sellerId  identifier of the Seller publishing the product
     * @param product   product to publish (built from the request by the adapter mapper)
     * @return the published Product
     */
    public Product publishProduct(String sellerId, Product product) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller id must not be null or blank");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        seller.requireActive();

        if (!seller.equals(product.getSeller())) {
            throw new IllegalArgumentException("Product must be published by the given seller");
        }
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("A published product must enter the catalog as PUBLISHED");
        }
        if (productRepository.findById(product.getIdentifier()).isPresent()) {
            throw new DuplicateResourceException("product", product.getIdentifier());
        }

        seller.addProduct(product);
        productRepository.save(product);
        notificationService.notify(seller, "Product published",
                "Product '" + product.getName() + "' is now visible in the catalog");
        return product;
    }
}
