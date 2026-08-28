package Application.adapters.out.persistence.mysql.adapters;

import Application.adapters.out.persistence.mysql.entities.PersonEntity;
import Application.adapters.out.persistence.mysql.entities.ProductEntity;
import Application.adapters.out.persistence.mysql.entities.ProductVariantEntity;
import Application.adapters.out.persistence.mysql.mappers.PersonMapper;
import Application.adapters.out.persistence.mysql.mappers.ProductMapper;
import Application.adapters.out.persistence.mysql.repositories.PersonJpaRepository;
import Application.adapters.out.persistence.mysql.repositories.ProductJpaRepository;
import Application.domain.models.DigitalProduct;
import Application.domain.models.Person;
import Application.domain.models.Product;
import Application.domain.models.ProductVariant;
import Application.domain.models.Seller;
import Application.domain.ports.out.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * MySQL persistence adapter for the ProductRepository output port.
 */
@Repository
@Profile("!memory")
public class MysqlProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final PersonJpaRepository personJpaRepository;

    public MysqlProductRepositoryAdapter(ProductJpaRepository jpaRepository,
                                         PersonJpaRepository personJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.personJpaRepository = personJpaRepository;
    }

    @Override
    @Transactional
    public void save(Product product) {
        jpaRepository.findById(product.getIdentifier()).ifPresentOrElse(
                existing -> updateEntity(existing, product),
                () -> jpaRepository.save(ProductMapper.toEntity(product)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(String identifier) {
        return jpaRepository.findById(identifier).map(this::toDomain);
    }

    private void updateEntity(ProductEntity existing, Product product) {
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setStatusCode(product.getStatus().getCode());
        if (existing instanceof Application.adapters.out.persistence.mysql.entities.DigitalProductEntity digitalEntity
                && product instanceof DigitalProduct digital) {
            digitalEntity.setDigitalDeliveryDetails(digital.getDigitalDeliveryDetails());
        }
        existing.getVariants().clear();
        for (ProductVariant variant : product.getVariants()) {
            ProductVariantEntity variantEntity = new ProductVariantEntity();
            variantEntity.setName(variant.getName());
            variantEntity.setValue(variant.getValue());
            variantEntity.setProduct(existing);
            existing.getVariants().add(variantEntity);
        }
        jpaRepository.save(existing);
    }

    private Product toDomain(ProductEntity entity) {
        return ProductMapper.toDomain(entity, requireSeller(entity.getSellerId()));
    }

    private Seller requireSeller(String sellerId) {
        Person person = personJpaRepository.findById(sellerId)
                .map(personEntity -> PersonMapper.toDomain(personEntity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Seller '" + sellerId + "' does not exist"));
        if (!(person instanceof Seller seller)) {
            throw new IllegalStateException("Person '" + sellerId + "' is not a Seller");
        }
        return seller;
    }

    private Person requirePerson(String referencedId) {
        return personJpaRepository.findById(referencedId)
                .map(entity -> PersonMapper.toDomain(entity, this::requirePerson))
                .orElseThrow(() -> new IllegalStateException("Referenced person '"
                        + referencedId + "' does not exist"));
    }
}
