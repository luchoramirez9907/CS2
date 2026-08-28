package Application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Requires local MySQL and MongoDB instances; disabled so the build can
 * run without infrastructure. Remove @Disabled when the databases are up.
 */
@Disabled("Requires local MySQL and MongoDB instances")
@SpringBootTest
class NexusmarketApplicationTests {

	@Test
	void contextLoads() {
	}

}
