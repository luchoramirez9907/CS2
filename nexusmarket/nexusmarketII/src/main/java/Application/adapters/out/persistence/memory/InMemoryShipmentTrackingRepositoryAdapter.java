package Application.adapters.out.persistence.memory;

import Application.domain.ports.out.ShipmentTrackingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * In-memory adapter for the ShipmentTrackingRepository output port
 * (memory mode).
 */
@Repository
@Profile("memory")
public class InMemoryShipmentTrackingRepositoryAdapter implements ShipmentTrackingRepository {

    private final InMemoryDataStore store;

    public InMemoryShipmentTrackingRepositoryAdapter(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public void record(String shipmentId, String orderId, String event,
                       LocalDateTime occurredAt, String details) {
        store.trackingEvents.add(occurredAt + " | " + shipmentId + " | " + orderId
                + " | " + event + " | " + details);
    }
}
