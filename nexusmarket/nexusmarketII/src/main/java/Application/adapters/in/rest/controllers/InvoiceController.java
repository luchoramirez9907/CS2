package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.InvoiceDtoMapper;
import Application.adapters.in.rest.requests.GenerateInvoiceRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.InvoiceResponse;
import Application.domain.models.Invoice;
import Application.domain.ports.in.ConsultInvoiceUseCase;
import Application.domain.ports.in.GenerateInvoiceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for Billing Management (Generate / Consult Invoice).
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final GenerateInvoiceUseCase generateInvoiceUseCase;
    private final ConsultInvoiceUseCase consultInvoiceUseCase;

    public InvoiceController(GenerateInvoiceUseCase generateInvoiceUseCase,
                             ConsultInvoiceUseCase consultInvoiceUseCase) {
        this.generateInvoiceUseCase = generateInvoiceUseCase;
        this.consultInvoiceUseCase = consultInvoiceUseCase;
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generate(
            @PathVariable String orderId,
            @RequestBody GenerateInvoiceRequest request) {
        Invoice invoice = generateInvoiceUseCase.generateInvoice(
                request.performerId(), orderId, request.taxRate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Invoice generated", InvoiceDtoMapper.toResponse(invoice)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> consult(
            @PathVariable String orderId,
            @RequestParam String requesterId) {
        Invoice invoice = consultInvoiceUseCase.consultInvoice(requesterId, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Invoice found", InvoiceDtoMapper.toResponse(invoice)));
    }
}
