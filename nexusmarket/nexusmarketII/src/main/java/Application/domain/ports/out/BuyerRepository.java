package Application.domain.ports.out;

import Application.domain.models.Buyer;

import java.util.Optional;

/**
 * Output port: persistence contract for Buyer aggregates.
 */
public interface BuyerRepository {

    void save(Buyer buyer);

    Optional<Buyer> findById(String identifier);
}
