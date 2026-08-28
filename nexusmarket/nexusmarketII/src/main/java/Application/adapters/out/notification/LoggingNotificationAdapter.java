package Application.adapters.out.notification;

import Application.domain.models.Person;
import Application.domain.ports.out.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Output adapter that fulfills the NotificationService port using the
 * application log. Replace with an email/push implementation without
 * touching the domain.
 */
@Component
public class LoggingNotificationAdapter implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationAdapter.class);

    @Override
    public void notify(Person recipient, String subject, String message) {
        log.info("[NOTIFICATION] to={} ({}) | subject='{}' | message='{}'",
                recipient.getIdentifier(), recipient.getEmail(), subject, message);
    }
}
