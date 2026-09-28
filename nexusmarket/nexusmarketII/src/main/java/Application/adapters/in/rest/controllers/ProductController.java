package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.ProductDtoMapper;
import Application.adapters.in.rest.requests.ChangeStatusRequest;
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
import Application.domain.valueobjects.ProductStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the catalog. The mapper builds the product domain
 * model; ownership binding and business validation happen in the domain
 * services. The requesting user of the management endpoints is identified
 * by the X-User-Id header (optional when consulting the public catalog).
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
            @RequestHeader(value = "X-User-Id", required = false) String requesterId,
            @PathVariable String productId) {
        Product product = manageProductUseCase.consultProduct(requesterId, productId);
        return ResponseEntity.ok(ApiResponse.ok("Product found", ProductDtoMapper.toResponse(product)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String productId,
            @RequestBody UpdateProductRequest request) {
        Product product = manageProductUseCase.updateProduct(requesterId, productId, request.name(),
                request.description(), ProductDtoMapper.toVariants(request.variants()));
        return ResponseEntity.ok(ApiResponse.ok("Product updated", ProductDtoMapper.toResponse(product)));
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> changeStatus(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String productId,
            @RequestBody ChangeStatusRequest request) {
        Product product = changeProductStatusUseCase.changeProductStatus(requesterId, productId,
                request.status() == null ? null : ProductStatus.fromCode(request.status()));
        return ResponseEntity.ok(ApiResponse.ok("Product status changed", ProductDtoMapper.toResponse(product)));
    }
}
