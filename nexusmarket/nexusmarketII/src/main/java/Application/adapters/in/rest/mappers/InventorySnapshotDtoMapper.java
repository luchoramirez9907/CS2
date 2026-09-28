package Application.adapters.in.rest.mappers;

import Application.adapters.in.rest.responses.InventoryResponse;
import Application.domain.ports.in.ConsultInventoryUseCase;

/**
 * Mapper: Inventory stock snapshot (use case output) to Response DTO.
 */
public final class InventorySnapshotDtoMapper {

    private InventorySnapshotDtoMapper() {
    }

    public static InventoryResponse toResponse(ConsultInventoryUseCase.StockSnapshot snapshot) {
        return new InventoryResponse(
                snapshot.inventoryId(),
                snapshot.productId(),
                snapshot.warehouseId(),
                snapshot.availableQuantity(),
                snapshot.reservedQuantity(),
                snapshot.availableQuantity() + snapshot.reservedQuantity());
    }
}
