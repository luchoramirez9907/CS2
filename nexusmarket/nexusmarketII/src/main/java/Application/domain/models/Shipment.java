package Application.domain.models;

import Application.domain.enums.ShipmentStatus;
import Application.domain.valueobjects.Address;

import java.time.LocalDateTime;

/**
 * Shipment
 *
 * Represents the logistics process of packing, dispatching, and
 * delivering a physical order. Only PhysicalProduct instances require
 * dispatch; DigitalProduct instances are delivered immediately after
 * payment confirmation.
 */
public class Shipment {

    private final String shipmentId;
    private final Order order;
    private final LogisticsOperator logisticsOperator;
    private final Warehouse originWarehouse;
    private final Address shippingAddress;
    private ShipmentStatus shipmentStatus;
    private LocalDateTime dispatchDate;
    private LocalDateTime deliveryDate;

    public Shipment(String shipmentId, Order order, LogisticsOperator logisticsOperator,
                    Warehouse originWarehouse, Address shippingAddress) {
        if (shipmentId == null || shipmentId.isBlank()) {
            throw new IllegalArgumentException("Shipment id must not be null or blank");
        }
        if (order == null) {
            throw new IllegalArgumentException("A Shipment fulfills exactly one Order");
        }
        if (logisticsOperator == null) {
            throw new IllegalArgumentException("A Shipment is assigned to one LogisticsOperator");
        }
        if (originWarehouse == null) {
            throw new IllegalArgumentException("A Shipment originates from one Warehouse");
        }
        if (shippingAddress == null) {
            throw new IllegalArgumentException("A Shipment requires a destination address");
        }
        this.shipmentId = shipmentId;
        this.order = order;
        this.logisticsOperator = logisticsOperator;
        this.originWarehouse = originWarehouse;
        this.shippingAddress = shippingAddress;
        this.shipmentStatus = ShipmentStatus.PENDING;
    }

    public String getShipmentId() { return shipmentId; }

    public Order getOrder() { return order; }

    public LogisticsOperator getLogisticsOperator() { return logisticsOperator; }

    public Warehouse getOriginWarehouse() { return originWarehouse; }

    public Address getShippingAddress() { return shippingAddress; }

    public ShipmentStatus getShipmentStatus() { return shipmentStatus; }

    public LocalDateTime getDispatchDate() { return dispatchDate; }

    public LocalDateTime getDeliveryDate() { return deliveryDate; }

    /**
     * Dispatches the shipment: the order physically leaves the warehouse.
     * The associated order must be paid before dispatch.
     */
    public void dispatch() {
        if (shipmentStatus != ShipmentStatus.PENDING) {
            throw new IllegalArgumentException("Shipment '" + shipmentId
                    + "' is not pending (status: " + shipmentStatus + ")");
        }
        order.markShipped();
        this.shipmentStatus = ShipmentStatus.IN_TRANSIT;
        this.dispatchDate = LocalDateTime.now();
    }

    /**
     * Confirms the delivery of the shipment to the buyer. The order is
     * finalized afterward through its own lifecycle (Order#finalizeOrder).
     */
    public void markDelivered() {
        if (shipmentStatus != ShipmentStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Shipment '" + shipmentId
                    + "' is not in transit (status: " + shipmentStatus + ")");
        }
        this.shipmentStatus = ShipmentStatus.DELIVERED;
        this.deliveryDate = LocalDateTime.now();
    }

    /** Marks the shipment as returned to the origin warehouse. */
    public void markReturned() {
        if (shipmentStatus != ShipmentStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Shipment '" + shipmentId
                    + "' is not in transit (status: " + shipmentStatus + ")");
        }
        this.shipmentStatus = ShipmentStatus.RETURNED;
    }

    /**
     * Rebuilds a persisted shipment and links it to its order without
     * re-applying lifecycle validations. Used exclusively by persistence
     * mappers.
     */
    public static Shipment reconstruct(String shipmentId, Order order, LogisticsOperator logisticsOperator,
                                       Warehouse originWarehouse, Address shippingAddress,
                                       ShipmentStatus shipmentStatus, LocalDateTime dispatchDate,
                                       LocalDateTime deliveryDate) {
        Shipment shipment = new Shipment(shipmentId, order, logisticsOperator, originWarehouse,
                shippingAddress);
        shipment.shipmentStatus = shipmentStatus;
        shipment.dispatchDate = dispatchDate;
        shipment.deliveryDate = deliveryDate;
        order.restoreShipment(shipment);
        return shipment;
    }
}
