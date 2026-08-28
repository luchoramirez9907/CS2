package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.DigitalProductEntity;
import Application.adapters.out.persistence.mysql.entities.PhysicalProductEntity;
import Application.adapters.out.persistence.mysql.entities.ProductEntity;
import Application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import Application.domain.models.DigitalProduct;
import Application.domain.models.PhysicalProduct;
import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.valueobjects.ProductStatus;

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
        entity.setStatusCode(product.getStatus().getCode());
        entity.setSellerId(product.getSeller().getIdentifier());
        for (Application.domain.models.ProductVariant variant : product.getVariants()) {
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
        Product product;
        if (entity instanceof DigitalProductEntity digitalEntity) {
            product = new DigitalProduct(entity.getIdentifier(), entity.getName(),
                    entity.getDescription(), status, seller, digitalEntity.getDigitalDeliveryDetails());
        } else {
            product = new PhysicalProduct(entity.getIdentifier(), entity.getName(),
                    entity.getDescription(), status, seller);
        }
        for (ProductVariantEntity variantEntity : entity.getVariants()) {
            product.addVariant(variantEntity.getName(), variantEntity.getValue());
        }
        return product;
    }
}
