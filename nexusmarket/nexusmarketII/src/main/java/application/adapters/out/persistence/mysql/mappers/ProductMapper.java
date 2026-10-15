package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.DigitalProductEntity;
import application.adapters.out.persistence.mysql.entities.PhysicalProductEntity;
import application.adapters.out.persistence.mysql.entities.ProductEntity;
import application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import application.domain.models.DigitalProduct;
import application.domain.models.PhysicalProduct;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.valueobjects.ProductStatus;

import java.math.BigDecimal;

/**
 * Mapper: Product domain models to JPA entities and back.
 */
public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductEntity toEntity(Product product) {
        ProductEntity entity;
        if (product.requiresPhysicalDispatch()) {
            entity = new PhysicalProductEntity();
        } else {
            DigitalProduct digital = (DigitalProduct) product;
            DigitalProductEntity digitalEntity = new DigitalProductEntity();
            digitalEntity.setDigitalDeliveryDetails(digital.getDigitalDeliveryDetails());
            entity = digitalEntity;
        }
        entity.setIdentifier(product.getIdentifier());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice());
        entity.setStatusCode(product.getStatus().getCode());
        entity.setSellerId(product.getSeller().getIdentifier());
        for (application.domain.models.ProductVariant variant : product.getVariants()) {
            ProductVariantEntity variantEntity = new ProductVariantEntity();
            variantEntity.setName(variant.getName());
            variantEntity.setValue(variant.getValue());
            variantEntity.setProduct(entity);
            entity.getVariants().add(variantEntity);
        }
        return entity;
    }

    public static Product toDomain(ProductEntity entity, Seller seller) {
        ProductStatus status = ProductStatus.fromCode(entity.getStatusCode());
        // Rows stored before the price column existed have no price yet.
        BigDecimal price = entity.getPrice() == null ? BigDecimal.ZERO : entity.getPrice();
        Product product;
        if (entity instanceof DigitalProductEntity digitalEntity) {
            product = new DigitalProduct(entity.getIdentifier(), entity.getName(),
                    entity.getDescription(), price, status, seller, digitalEntity.getDigitalDeliveryDetails());
        } else {
            product = new PhysicalProduct(entity.getIdentifier(), entity.getName(),
                    entity.getDescription(), price, status, seller);
        }
        for (ProductVariantEntity variantEntity : entity.getVariants()) {
            product.addVariant(variantEntity.getName(), variantEntity.getValue());
        }
        return product;
    }
}
