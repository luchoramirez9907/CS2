package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.PersonDtoMapper;
import application.adapters.in.rest.mappers.ProductDtoMapper;
import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.responses.ApiResponse;
import application.adapters.in.rest.responses.BuyerResponse;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.ports.in.PublicAccessPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST adapter for the public operations of PublicAccessPort (no
 * authentication): buyer self-registration and the public catalog.
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicAccessRestController {

    private final PublicAccessPort publicAccessPort;

    public PublicAccessRestController(PublicAccessPort publicAccessPort) {
        this.publicAccessPort = publicAccessPort;
    }

    @PostMapping("/buyers")
    public ResponseEntity<ApiResponse<BuyerResponse>> registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
        BuyerResponse buyer = PersonDtoMapper.toResponse(publicAccessPort.registerBuyer(request.identifier(),
                request.fullName(), request.email(), PersonDtoMapper.toAddress(request.primaryAddress()),
                request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Buyer registered", buyer));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consultProduct(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.ok("Product found",
                ProductDtoMapper.toResponse(publicAccessPort.consultCatalogProduct(productId))));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchCatalog(
            @RequestParam(required = false) String q, @RequestParam(required = false) String sellerId) {
        return ResponseEntity.ok(ApiResponse.ok("Catalog results", publicAccessPort.searchCatalog(q, sellerId).stream()
                .map(ProductDtoMapper::toResponse).toList()));
    }
}
