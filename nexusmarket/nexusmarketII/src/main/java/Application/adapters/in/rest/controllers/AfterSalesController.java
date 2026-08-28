package Application.adapters.in.rest.controllers;

import Application.adapters.in.rest.mappers.AfterSalesDtoMapper;
import Application.adapters.in.rest.requests.ApproveReturnRequest;
import Application.adapters.in.rest.requests.ProcessRefundRequest;
import Application.adapters.in.rest.requests.RequestReturnRequest;
import Application.adapters.in.rest.responses.ApiResponse;
import Application.adapters.in.rest.responses.RefundResponse;
import Application.adapters.in.rest.responses.ReturnResponse;
import Application.domain.models.Refund;
import Application.domain.models.Return;
import Application.domain.ports.in.ApproveReturnUseCase;
import Application.domain.ports.in.ProcessRefundUseCase;
import Application.domain.ports.in.RequestReturnUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for post-sale processes: returns and refunds.
 */
@RestController
@RequestMapping("/api")
public class AfterSalesController {

    private final RequestReturnUseCase requestReturnUseCase;
    private final ApproveReturnUseCase approveReturnUseCase;
    private final ProcessRefundUseCase processRefundUseCase;

    public AfterSalesController(RequestReturnUseCase requestReturnUseCase,
                                ApproveReturnUseCase approveReturnUseCase,
                                ProcessRefundUseCase processRefundUseCase) {
        this.requestReturnUseCase = requestReturnUseCase;
        this.approveReturnUseCase = approveReturnUseCase;
        this.processRefundUseCase = processRefundUseCase;
    }

    @PostMapping("/returns")
    public ResponseEntity<ApiResponse<ReturnResponse>> requestReturn(
            @RequestBody RequestReturnRequest request) {
        Return returnRequest = requestReturnUseCase.requestReturn(
                request.buyerId(), request.orderId(), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Return requested", AfterSalesDtoMapper.toResponse(returnRequest)));
    }

    @PostMapping("/returns/{returnId}/approval")
    public ResponseEntity<ApiResponse<ReturnResponse>> approveReturn(
            @PathVariable String returnId,
            @RequestBody ApproveReturnRequest request) {
        Return returnRequest = approveReturnUseCase.approveReturn(request.administratorId(), returnId);
        return ResponseEntity.ok()
                .body(ApiResponse.ok("Return approved", AfterSalesDtoMapper.toResponse(returnRequest)));
    }

    @PostMapping("/refunds")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @RequestBody ProcessRefundRequest request) {
        Refund refund = processRefundUseCase.processRefund(
                request.approverId(), request.returnId(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Refund processed", AfterSalesDtoMapper.toResponse(refund)));
    }
}
