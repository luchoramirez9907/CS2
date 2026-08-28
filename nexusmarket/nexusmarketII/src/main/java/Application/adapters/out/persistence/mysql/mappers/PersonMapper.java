package Application.adapters.out.persistence.mysql.mappers;

import Application.adapters.out.persistence.mysql.entities.AddressEmbeddable;
import Application.adapters.out.persistence.mysql.entities.BuyerEntity;
import Application.adapters.out.persistence.mysql.entities.PersonEntity;
import Application.adapters.out.persistence.mysql.entities.SellerEntity;
import Application.domain.models.Administrator;
import Application.domain.models.Buyer;
import Application.domain.models.LogisticsOperator;
import Application.domain.models.Person;
import Application.domain.models.Seller;
import Application.domain.models.Supervisor;
import Application.domain.valueobjects.Address;
import Application.domain.valueobjects.BuyerCommercialStatus;
import Application.domain.valueobjects.SystemRole;
import Application.domain.valueobjects.UserStatus;

import java.util.function.Function;

/**
 * Mapper: Person domain models to JPA entities and back.
 */
public final class PersonMapper {

    private PersonMapper() {
    }

    public static PersonEntity toEntity(Person person) {
        PersonEntity entity;
        if (person instanceof Buyer buyer) {
            BuyerEntity buyerEntity = new BuyerEntity();
            buyerEntity.setCommercialStatusCode(buyer.getCommercialStatus().getCode());
            if (buyer.getPrimaryAddress() != null) {
                buyerEntity.setPrimaryAddress(toEmbeddable(buyer.getPrimaryAddress()));
            }
            for (Address additional : buyer.getAdditionalAddresses()) {
                buyerEntity.getAdditionalAddresses().add(toEmbeddable(additional));
            }
            entity = buyerEntity;
        } else if (person instanceof Seller seller) {
            SellerEntity sellerEntity = new SellerEntity();
            sellerEntity.setRegisteredById(seller.getRegisteredBy().getIdentifier());
            entity = sellerEntity;
        } else {
            entity = new PersonEntity();
        }
        entity.setIdentifier(person.getIdentifier());
        entity.setFullName(person.getFullName());
        entity.setEmail(person.getEmail());
        entity.setRoleCode(person.getRole().getCode());
        entity.setStatusCode(person.getStatus().getCode());
        return entity;
    }

    /**
     * @param personResolver resolves referenced person identifiers
     *                       (e.g. the administrator who registered a seller)
     */
    public static Person toDomain(PersonEntity entity, Function<String, Person> personResolver) {
        UserStatus status = UserStatus.fromCode(entity.getStatusCode());
        if (entity instanceof BuyerEntity buyerEntity) {
            Address primary = buyerEntity.getPrimaryAddress() == null
                    ? null : toAddress(buyerEntity.getPrimaryAddress());
            Buyer buyer = new Buyer(entity.getIdentifier(), entity.getFullName(), entity.getEmail(),
                    primary, BuyerCommercialStatus.fromCode(buyerEntity.getCommercialStatusCode()), status);
            for (AddressEmbeddable additional : buyerEntity.getAdditionalAddresses()) {
                buyer.addAdditionalAddress(toAddress(additional));
            }
            return buyer;
        }
        if (entity instanceof SellerEntity sellerEntity) {
            Person registrar = personResolver.apply(sellerEntity.getRegisteredById());
            if (!(registrar instanceof Administrator administrator)) {
                throw new IllegalStateException("Seller '" + entity.getIdentifier()
                        + "' must be registered by an Administrator");
            }
            return new Seller(entity.getIdentifier(), entity.getFullName(), entity.getEmail(),
                    administrator, status);
        }
        SystemRole role = SystemRole.fromCode(entity.getRoleCode());
        if (role == SystemRole.ADMINISTRATOR) {
            return new Administrator(entity.getIdentifier(), entity.getFullName(),
                    entity.getEmail(), status);
        }
        if (role == SystemRole.SUPERVISOR) {
            return new Supervisor(entity.getIdentifier(), entity.getFullName(),
                    entity.getEmail(), status);
        }
        if (role == SystemRole.LOGISTICS_OPERATOR) {
            return new LogisticsOperator(entity.getIdentifier(), entity.getFullName(),
                    entity.getEmail(), status);
        }
        throw new IllegalStateException("Unsupported stored role: " + role.getCode());
    }

    public static AddressEmbeddable toEmbeddable(Address address) {
        return new AddressEmbeddable(address.getStreet(), address.getCity(), address.getState(),
                address.getCountry(), address.getPostalCode());
    }

    public static Address toAddress(AddressEmbeddable embeddable) {
        return new Address(embeddable.getStreet(), embeddable.getCity(), embeddable.getState(),
                embeddable.getCountry(), embeddable.getPostalCode());
    }

    /**
     * Updates an existing entity in place from the domain model. The
     * stored specialization must match the domain type (role changes are
     * not supported by the persistence model).
     */
    public static void updateEntity(PersonEntity existing, Person person) {
        boolean typeMatches;
        if (person instanceof Buyer) {
            typeMatches = existing instanceof BuyerEntity;
        } else if (person instanceof Seller) {
            typeMatches = existing instanceof SellerEntity;
        } else {
            typeMatches = existing.getClass() == PersonEntity.class;
        }
        if (!typeMatches) {
            throw new IllegalStateException("Stored person '" + existing.getIdentifier()
                    + "' has a different specialization; role changes are not supported");
        }
        existing.setFullName(person.getFullName());
        existing.setEmail(person.getEmail());
        existing.setStatusCode(person.getStatus().getCode());
        if (person instanceof Buyer buyer && existing instanceof BuyerEntity buyerEntity) {
            buyerEntity.setCommercialStatusCode(buyer.getCommercialStatus().getCode());
            buyerEntity.setPrimaryAddress(buyer.getPrimaryAddress() == null
                    ? null : toEmbeddable(buyer.getPrimaryAddress()));
            buyerEntity.getAdditionalAddresses().clear();
            for (Address additional : buyer.getAdditionalAddresses()) {
                buyerEntity.getAdditionalAddresses().add(toEmbeddable(additional));
            }
        }
        if (person instanceof Seller seller && existing instanceof SellerEntity sellerEntity) {
            sellerEntity.setRegisteredById(seller.getRegisteredBy().getIdentifier());
        }
    }
}
