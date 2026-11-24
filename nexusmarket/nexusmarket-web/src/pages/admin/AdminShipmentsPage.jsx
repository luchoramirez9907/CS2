import { useCallback } from "react";
import { getShipment, listOrders } from "../../api/orders.js";
import LoadState from "../../components/LoadState.jsx";
import OrderList from "../../components/OrderList.jsx";
import { SHIPMENT_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

async function loadShipments() {
  const [paid, shipped] = await Promise.all([listOrders("admin", "PAID"), listOrders("admin", "SHIPPED")]);
  const orders = [...paid, ...shipped];
  const active = orders.filter((order) => order.hasShipment);
  const shipments = await Promise.all(active.map((order) => getShipment("admin", order.orderId)));
  return {
    waiting: orders.filter((order) => !order.hasShipment),
    active,
    shipments: new Map(shipments.map((shipment) => [shipment.orderId, shipment]))
  };
}

export default function AdminShipmentsPage() {
  const { data, loading, error, reload } = useLoad(useCallback(loadShipments, []));
  return (
    <>
      <h1>Envíos</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <>
          <h2>Por asignar</h2>
          {data.waiting.length === 0
            ? <p className="muted">Todos los pedidos pagados ya tienen envío.</p>
            : <OrderList orders={data.waiting} base="/admin/pedidos" label={() => "Sin envío"} />}
          <h2>En proceso</h2>
          {data.active.length === 0
            ? <p className="muted">No hay envíos en proceso.</p>
            : <OrderList orders={data.active} base="/admin/pedidos"
                         label={(order) => SHIPMENT_STATUS[data.shipments.get(order.orderId).shipmentStatus]}
                         note={(order) => `Operador ${data.shipments.get(order.orderId).logisticsOperatorId}`} />}
        </>
      )}
    </>
  );
}
