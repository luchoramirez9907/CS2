import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { loadOrder } from "../../api/orders.js";
import Alert from "../../components/Alert.jsx";
import LoadState from "../../components/LoadState.jsx";
import OrderDetail from "../../components/OrderDetail.jsx";
import ShipmentActions from "../../components/ShipmentActions.jsx";
import { useAction } from "../../utils/useAction.js";
import { useLoad } from "../../utils/useLoad.js";

function OrderView({ orderId }) {
  const { data, loading, error, reload } = useLoad(useCallback(() => loadOrder("operator", orderId), [orderId]));
  const { busy, failure, run } = useAction();

  /** After each action (also when the order had changed) the page shows its current state. */
  async function act(action) {
    const problem = await run(action);
    if (!problem || problem.status) await reload();
  }

  const { order, shipment } = data ?? {};
  return (
    <>
      <p><Link to="/operador/envios">← Envíos asignados</Link></p>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <OrderDetail data={data} loading={loading} onReload={reload} notice={<Alert type="error">{failure}</Alert>}>
          <ShipmentActions key={shipment?.shipmentStatus} role="operator" order={order} shipment={shipment} busy={busy}
                           onAction={act} />
        </OrderDetail>
      )}
    </>
  );
}

// The key starts each order from scratch.
export default function AssignedOrderPage() {
  const { orderId } = useParams();
  return <OrderView key={orderId} orderId={orderId} />;
}
