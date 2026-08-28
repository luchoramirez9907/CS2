package Application.domain.ports.out;

import Application.domain.models.Person;

/**
 * Output port: notifies participants about relevant business events
 * (e.g. order confirmation, shipment status, refund processing).
 */
public interface NotificationService {

    void notify(Person recipient, String subject, String message);
}
