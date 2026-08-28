package Application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for any Person of the platform (relational persistence).
 * Persistence entities never leak to the API (per SDD constraint 4).
 */
@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class PersonEntity {

    @Id
    @Column(name = "identifier", length = 64)
    private String identifier;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "role_code", nullable = false, length = 32)
    private String roleCode;

    @Column(name = "status_code", nullable = false, length = 32)
    private String statusCode;
}
