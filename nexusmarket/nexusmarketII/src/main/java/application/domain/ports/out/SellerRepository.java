package application.domain.ports.out;

import application.domain.models.Seller;

import java.util.Optional;

/**
 * Output port: persistence contract for Seller aggregates.
 */
public interface SellerRepository {

    void save(Seller seller);

    Optional<Seller> findById(String identifier);
}
