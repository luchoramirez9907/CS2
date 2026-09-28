package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.ProductDtoMapper;
import Application.adapters.in.rest.requests.ChangeProductStatusRequest;
import Application.adapters.in.rest.requests.PublishProductRequest;
import Application.adapters.in.rest.requests.UpdateProductRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.ProductResponse;
import Application.domain.models.Product;
import Application.domain.models.Seller;
import Application.domain.ports.in.ChangeProductStatusUseCase;
import Application.domain.ports.in.ManageProductUseCase;
import Application.domain.ports.in.PublishProductUseCase;
import Application.domain.ports.out.SellerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Catalog Management (Publish Product / Manage Product
 * / Change Product Status). The mapper builds the product domain model;
 * ownership binding and business validation happen in the domain service.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final PublishProductUseCase publishProductUseCase;
    private final ManageProductUseCase manageProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final SellerRepository sellerRepository;

    public ProductController(PublishProductUseCase publishProductUseCase,
                             ManageProductUseCase manageProductUseCase,
                             ChangeProductStatusUseCase changeProductStatusUseCase,
                             SellerRepository sellerRepository) {
        this.publishProductUseCase = publishProductUseCase;
        this.manageProductUseCase = manageProductUseCase;
        this.changeProductStatusUseCase = changeProductStatusUseCase;
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

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> consult(
            @PathVariable String productId,
            @RequestParam String requesterId) {
        Product product = manageProductUseCase.consultProduct(requesterId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Product found", ProductDtoMapper.toResponse(product)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateDescription(
            @PathVariable String productId,
            @RequestBody UpdateProductRequest request) {
        Product product = manageProductUseCase.updateProductDescription(
                request.performerId(), productId, request.newDescription());
        return ResponseEntity.ok(ApiResponse.ok("Product updated", ProductDtoMapper.toResponse(product)));
    }

    @PostMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> changeStatus(
            @PathVariable String productId,
            @RequestBody ChangeProductStatusRequest request) {
        Product product = changeProductStatusUseCase.changeProductStatus(
                request.performerId(), productId, request.statusCode());
        return ResponseEntity.ok(ApiResponse.ok("Product status changed",
                ProductDtoMapper.toResponse(product)));
    }
}
