package Application.infrastructure.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Logs the connectivity status of MySQL and MongoDB at startup
 * (per SDD - Infrastructure/Database: connection configuration).
 */
@Component
@Profile("!memory")
public class DatabaseConnectionVerifier {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionVerifier.class);

    private final ObjectProvider<DataSource> dataSource;
    private final ObjectProvider<MongoTemplate> mongoTemplate;

    public DatabaseConnectionVerifier(ObjectProvider<DataSource> dataSource,
                                      ObjectProvider<MongoTemplate> mongoTemplate) {
        this.dataSource = dataSource;
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyConnections() {
        dataSource.ifAvailable(ds -> {
            try (Connection connection = ds.getConnection()) {
                log.info("MySQL connection OK: {}", connection.getMetaData().getURL());
            } catch (Exception ex) {
                log.warn("MySQL connection FAILED: {}", ex.getMessage());
            }
        });
        mongoTemplate.ifAvailable(template -> {
            try {
                template.executeCommand("{ ping: 1 }");
                log.info("MongoDB connection OK");
            } catch (Exception ex) {
                log.warn("MongoDB connection FAILED: {}", ex.getMessage());
            }
        });
    }
}
