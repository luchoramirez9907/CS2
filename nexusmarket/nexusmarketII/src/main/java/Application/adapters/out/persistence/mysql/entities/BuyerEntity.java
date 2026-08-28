package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for Buyer specializations of Person.
 */
@Entity
@Table(name = "buyers")
@Getter
@Setter
@NoArgsConstructor
public class BuyerEntity extends PersonEntity {

    @Column(name = "commercial_status_code", nullable = false, length = 32)
    private String commercialStatusCode;

    @Embedded
    private AddressEmbeddable primaryAddress;

    @ElementCollection
    @CollectionTable(name = "buyer_additional_addresses", joinColumns = @JoinColumn(name = "buyer_id"))
    private List<AddressEmbeddable> additionalAddresses = new ArrayList<>();
}
