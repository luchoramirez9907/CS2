package Application.domain.ports.out;

import Application.domain.models.Return;

import java.util.Optional;

/**
 * Output port: persistence contract for post-sale Return requests.
 */
public interface ReturnRepository {

    void save(Return returnRequest);

    Optional<Return> findById(String returnId);
}
