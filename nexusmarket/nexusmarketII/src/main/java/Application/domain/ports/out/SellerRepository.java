package Application.domain.ports.out;

import Application.domain.models.Seller;

import java.util.Optional;

/**
 * Output port: persistence contract for Seller aggregates.
 */
public interface SellerRepository {

    void save(Seller seller);

    Optional<Seller> findById(String identifier);
}
