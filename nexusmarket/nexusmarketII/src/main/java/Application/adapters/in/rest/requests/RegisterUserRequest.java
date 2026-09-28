package Application.adapters.in.rest.requests;

/**
 * Request DTO: register a system participant (Administrator,
 * LogisticsOperator or Supervisor).
 */
public record RegisterUserRequest(String performerId, String identifier,
                                  String fullName, String email, String roleCode) {
}
