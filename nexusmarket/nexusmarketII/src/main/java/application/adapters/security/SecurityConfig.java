package application.adapters.security;

import application.domain.ports.out.TokenService;
import application.domain.services.authentication.LoadAuthenticatedUserService;
import application.domain.valueobjects.SystemRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration: stateless JWT authentication and role-based
 * access control on the REST routes. Each route prefix belongs to exactly
 * one Role Input Port; the domain still enforces the fine-grained
 * permission and ownership rules (ValidatePermissionsService and
 * ValidateOwnershipAccessService).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenService tokenService,
                                                   LoadAuthenticatedUserService loadAuthenticatedUserService,
                                                   SecurityErrorHandler securityErrorHandler,
                                                   CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()
                        .requestMatchers("/api/v1/buyer/**").hasRole(SystemRole.BUYER.getCode())
                        .requestMatchers("/api/v1/seller/**").hasRole(SystemRole.SELLER.getCode())
                        .requestMatchers("/api/v1/operator/**").hasRole(SystemRole.LOGISTICS_OPERATOR.getCode())
                        .requestMatchers("/api/v1/admin/**").hasRole(SystemRole.ADMINISTRATOR.getCode())
                        .requestMatchers("/api/v1/supervisor/**").hasRole(SystemRole.SUPERVISOR.getCode())
                        .anyRequest().denyAll())
                .addFilterBefore(new RequestIdFilter(), SecurityContextHolderFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(tokenService, loadAuthenticatedUserService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * CORS: only the configured frontend origin, the documented methods and
     * headers; credentials disabled because the JWT travels in the
     * Authorization header, not in cookies.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${nexusmarket.cors.allowed-origin}") String allowedOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", RequestIdFilter.HEADER));
        configuration.setExposedHeaders(List.of(RequestIdFilter.HEADER));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
