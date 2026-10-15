package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.AddressRequest;
import application.adapters.in.rest.responses.AddressResponse;
import application.adapters.in.rest.responses.BuyerResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.Buyer;
import application.domain.models.Person;
import application.domain.valueobjects.Address;

import java.util.List;

/**
 * Mapper: Request DTOs to Domain Models (addresses) and Domain Models
 * (Person, Buyer) to Response DTOs.
 */
public final class PersonDtoMapper {

    private PersonDtoMapper() {
    }

    public static Address toAddress(AddressRequest request) {
        if (request == null) {
            return null;
        }
        return new Address(request.street(), request.city(), request.state(), request.country(),
                request.postalCode());
    }

    public static List<Address> toAddresses(List<AddressRequest> requests) {
        return requests == null ? null : requests.stream().map(PersonDtoMapper::toAddress).toList();
    }

    public static AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(address.getStreet(), address.getCity(), address.getState(),
                address.getCountry(), address.getPostalCode());
    }

    public static UserResponse toResponse(Person person) {
        return new UserResponse(
                person.getIdentifier(),
                person.getFullName(),
                person.getEmail(),
                person.getRole().getCode(),
                person.getStatus().getCode());
    }

    public static BuyerResponse toResponse(Buyer buyer) {
        return new BuyerResponse(
                buyer.getIdentifier(),
                buyer.getFullName(),
                buyer.getEmail(),
                buyer.getStatus().getCode(),
                buyer.getCommercialStatus().getCode(),
                toResponse(buyer.getPrimaryAddress()),
                buyer.getAdditionalAddresses().stream().map(PersonDtoMapper::toResponse).toList());
    }
}
