package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.AfterSalesDtoMapper;
import Application.adapters.in.rest.mappers.OrderDtoMapper;
import Application.adapters.in.rest.mappers.PersonDtoMapper;
import Application.adapters.in.rest.requests.ChangeStatusRequest;
import Application.adapters.in.rest.requests.RegisterBuyerRequest;
import Application.adapters.in.rest.requests.UpdateBuyerRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.BuyerResponse;
import Application.adapters.in.rest.responses.OrderResponse;
import Application.adapters.in.rest.responses.RefundResponse;
import Application.adapters.in.rest.responses.ReturnResponse;
import Application.domain.models.Buyer;
import Application.domain.ports.in.ManageBuyerUseCase;
import Application.domain.ports.in.RegisterBuyerUseCase;
import Application.domain.valueobjects.BuyerCommercialStatus;
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

import java.util.List;

/**
 * REST adapter for buyer management. The requesting user is identified by
 * the X-User-Id header (optional on registration: buyer self-registration).
 */
@RestController
@RequestMapping("/api/buyers")
public class BuyerController {

    private final RegisterBuyerUseCase registerBuyerUseCase;
    private final ManageBuyerUseCase manageBuyerUseCase;

    public BuyerController(RegisterBuyerUseCase registerBuyerUseCase, ManageBuyerUseCase manageBuyerUseCase) {
        this.registerBuyerUseCase = registerBuyerUseCase;
        this.manageBuyerUseCase = manageBuyerUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BuyerResponse>> register(
            @RequestHeader(value = "X-User-Id", required = false) String requesterId,
            @RequestBody RegisterBuyerRequest request) {
        Buyer buyer = registerBuyerUseCase.registerBuyer(requesterId, request.identifier(),
                request.fullName(), request.email(), PersonDtoMapper.toAddress(request.primaryAddress()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Buyer registered", PersonDtoMapper.toResponse(buyer)));
    }

    @GetMapping("/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> consult(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String buyerId) {
        Buyer buyer = manageBuyerUseCase.consultBuyer(requesterId, buyerId);
        return ResponseEntity.ok(ApiResponse.ok("Buyer found", PersonDtoMapper.toResponse(buyer)));
    }

    @PutMapping("/{buyerId}")
    public ResponseEntity<ApiResponse<BuyerResponse>> update(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String buyerId,
            @RequestBody UpdateBuyerRequest request) {
        Buyer buyer = manageBuyerUseCase.updateBuyer(requesterId, buyerId, request.fullName(),
                request.email(), PersonDtoMapper.toAddress(request.primaryAddress()),
                PersonDtoMapper.toAddresses(request.additionalAddresses()));
        return ResponseEntity.ok(ApiResponse.ok("Buyer updated", PersonDtoMapper.toResponse(buyer)));
    }

    @PatchMapping("/{buyerId}/commercial-status")
    public ResponseEntity<ApiResponse<BuyerResponse>> changeCommercialStatus(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String buyerId,
            @RequestBody ChangeStatusRequest request) {
        Buyer buyer = manageBuyerUseCase.changeCommercialStatus(requesterId, buyerId,
                request.status() == null ? null : BuyerCommercialStatus.fromCode(request.status()));
        return ResponseEntity.ok(ApiResponse.ok("Commercial status changed", PersonDtoMapper.toResponse(buyer)));
    }

    @GetMapping("/{buyerId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> orders(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String buyerId) {
        List<OrderResponse> orders = manageBuyerUseCase.consultBuyerOrders(requesterId, buyerId).stream()
                .map(OrderDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Buyer orders", orders));
    }

    @GetMapping("/{buyerId}/returns")
    public ResponseEntity<ApiResponse<List<ReturnResponse>>> returns(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String buyerId) {
        List<ReturnResponse> returns = manageBuyerUseCase.consultBuyerReturns(requesterId, buyerId).stream()
                .map(AfterSalesDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Buyer returns", returns));
    }

    @GetMapping("/{buyerId}/refunds")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> refunds(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable String buyerId) {
        List<RefundResponse> refunds = manageBuyerUseCase.consultBuyerRefunds(requesterId, buyerId).stream()
                .map(AfterSalesDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Buyer refunds", refunds));
    }
}
