import { useCallback } from "react";
import { getShipment, listOrders } from "../../api/orders.js";
import LoadState from "../../components/LoadState.jsx";
import OrderList from "../../components/OrderList.jsx";
import { SHIPMENT_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

async function loadAssigned() {
  const orders = await listOrders("operator");
  const open = orders.filter((order) => order.orderStatus !== "DELIVERED");
  const shipments = await Promise.all(open.map((order) => getShipment("operator", order.orderId)));
  return {
    open,
    closed: orders.filter((order) => order.orderStatus === "DELIVERED"),
    status: new Map(shipments.map((shipment) => [shipment.orderId, shipment.shipmentStatus]))
  };
}

// A delivered shipment whose order is still open only needs to be finalized.
const label = (status) => (status === "DELIVERED" ? "Falta finalizar" : SHIPMENT_STATUS[status]);

export default function AssignedOrdersPage() {
  const { data, loading, error, reload } = useLoad(useCallback(loadAssigned, []));
  return (
    <>
      <h1>Envíos asignados</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <>
          <h2>Por hacer</h2>
          {data.open.length === 0
            ? <p className="muted">No tienes envíos pendientes.</p>
            : <OrderList orders={data.open} base="/operador/envios"
                         label={(order) => label(data.status.get(order.orderId))} />}
          {data.closed.length > 0 && (
            <>
              <h2>Finalizados</h2>
              <OrderList orders={data.closed} base="/operador/envios" />
            </>
          )}
        </>
      )}
    </>
  );
}
