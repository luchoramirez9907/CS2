package application.adapters.out.persistence.memory;

import application.domain.enums.TrackingEventType;
import application.domain.models.ShipmentTrackingEvent;
import application.domain.ports.out.ShipmentTrackingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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
    public void record(String shipmentId, String orderId, TrackingEventType event,
                       LocalDateTime occurredAt, String details) {
        store.trackingEvents.add(new ShipmentTrackingEvent(shipmentId, orderId, event, occurredAt, details));
    }

    @Override
    public List<ShipmentTrackingEvent> findByShipmentId(String shipmentId) {
        synchronized (store.trackingEvents) {
            return store.trackingEvents.stream()
                    .filter(event -> event.shipmentId().equals(shipmentId))
                    .sorted(Comparator.comparing(ShipmentTrackingEvent::occurredAt))
                    .toList();
        }
    }
}
