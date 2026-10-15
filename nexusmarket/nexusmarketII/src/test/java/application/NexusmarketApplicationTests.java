package application;

import application.domain.ports.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads the full Spring context with the "memory" and "demo" profiles, so
 * the wiring of adapters, domain services and controllers (and the demo
 * data seeding) is verified without requiring local MySQL and MongoDB
 * instances.
 */
@SpringBootTest
@ActiveProfiles({"memory", "demo"})
class NexusmarketApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void demoAccountsAreSeededWithHashedPasswords() {
		assertTrue(userRepository.findByPersonId("administrator-1").isPresent());
		assertTrue(userRepository.findByEmail("buyer@nexusmarket.com").isPresent());
		assertTrue(userRepository.findByPersonId("seller-1")
				.map(user -> user.getPasswordHash().startsWith("$2"))
				.orElse(false));
	}

}
