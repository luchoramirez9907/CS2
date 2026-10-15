package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * Entry point of NexusMarket. Authentication is stateless JWT
 * (infrastructure/security), so Spring Boot's default in-memory user with a
 * generated password is disabled.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class NexusmarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusmarketApplication.class, args);
	}

}
