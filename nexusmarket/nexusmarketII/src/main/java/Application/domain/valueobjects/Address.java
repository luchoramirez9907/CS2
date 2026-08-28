package Application.domain.valueobjects;

import java.util.Objects;

/**
 * Address
 *
 * Represents a physical location used for buyer deliveries and warehouse
 * placement. It is a Value Object (not a DomainCatalog): a structured,
 * immutable description of a physical location, compared by value.
 */
public final class Address {

    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String postalCode;

    public Address(String street, String city, String state, String country, String postalCode) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Address street must not be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("Address city must not be null or blank");
        }
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("Address state must not be null or blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Address country must not be null or blank");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Address postal code must not be null or blank");
        }
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address)) {
            return false;
        }
        Address address = (Address) o;
        return street.equals(address.street)
                && city.equals(address.city)
                && state.equals(address.state)
                && country.equals(address.country)
                && postalCode.equals(address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, country, postalCode);
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + state + ", " + country + " (" + postalCode + ")";
    }
}
