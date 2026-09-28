package Application.domain.ports.out;

import Application.domain.models.Return;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence contract for post-sale Return requests
 * (including the Refund each one may have generated).
 */
public interface ReturnRepository {

    void save(Return returnRequest);

    Optional<Return> findById(String returnId);

    List<Return> findByBuyerId(String buyerId);

    List<Return> findAll();
}
