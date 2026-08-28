package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.ProductDtoMapper;
import Application.adapters.in.rest.requests.PublishProductRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.ProductResponse;
import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.ports.in.PublishProductUseCase;
import Application.domain.ports.out.SellerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for catalog publication. The mapper builds the product
 * domain model; ownership binding and business validation happen in the
 * domain service.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final PublishProductUseCase publishProductUseCase;
    private final SellerRepository sellerRepository;

    public ProductController(PublishProductUseCase publishProductUseCase,
                             SellerRepository sellerRepository) {
        this.publishProductUseCase = publishProductUseCase;
        this.sellerRepository = sellerRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> publish(@RequestBody PublishProductRequest request) {
        Seller seller = sellerRepository.findById(request.sellerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Seller '" + request.sellerId() + "' does not exist"));
        Product product = ProductDtoMapper.toDomain(request, seller);
        Product published = publishProductUseCase.publishProduct(request.sellerId(), product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product published", ProductDtoMapper.toResponse(published)));
    }
}
