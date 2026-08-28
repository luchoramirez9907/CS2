package Application.domain.valueobjects;

/**
 * DomainCatalog (Abstract)
 *
 * Represents a generic business catalog used throughout the NexusMarket domain.
 *
 * Provides a consistent structure for controlled business values that require
 * a code, a human-readable name and a business description.
 *
 * Characteristics (per SDD - Domain Value Objects):
 * - Immutable.
 * - Equality is determined by value rather than object identity.
 * - Catalog values are controlled by the domain.
 * - Each catalog value has a unique code.
 *
 * This class cannot be instantiated directly.
 */
public abstract class DomainCatalog {

    private final String code;
    private final String name;
    private final String description;

    protected DomainCatalog(String code, String name, String description) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Catalog code must not be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Catalog name must not be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Catalog description must not be null or blank");
        }
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DomainCatalog that = (DomainCatalog) o;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 31 + code.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{code='" + code + "', name='" + name + "'}";
    }
}
