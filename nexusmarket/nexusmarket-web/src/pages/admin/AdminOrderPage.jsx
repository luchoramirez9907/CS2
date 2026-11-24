import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getBuyer, listUsers } from "../../api/people.js";
import { confirmPayment, createShipment, loadOrder } from "../../api/orders.js";
import { listWarehouses } from "../../api/stock.js";
import Alert from "../../components/Alert.jsx";
import ConfirmButton from "../../components/ConfirmButton.jsx";
import LoadState from "../../components/LoadState.jsx";
import OrderDetail from "../../components/OrderDetail.jsx";
import ShipmentActions from "../../components/ShipmentActions.jsx";
import { money } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useLoad } from "../../utils/useLoad.js";

async function loadOptions() {
  const [users, warehouses] = await Promise.all([listUsers("admin", "LOGISTICS_OPERATOR"), listWarehouses("admin")]);
  return { operators: users.filter((user) => user.status === "ACTIVE"), warehouses };
}

function AssignShipment({ busy, onSubmit }) {
  const { data, loading, error, reload } = useLoad(loadOptions);
  const [operator, setOperator] = useState("");
  const [warehouse, setWarehouse] = useState("");
  const [missing, setMissing] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    if (!operator || !warehouse) {
      setMissing("Elige el operador y la bodega de origen.");
      return;
    }
    setMissing("");
    onSubmit(warehouse, operator);
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <h2>Asignar envío</h2>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="operator">Operador logístico</label>
              <select id="operator" value={operator} onChange={(event) => setOperator(event.target.value)}>
                <option value="">Elige un operador</option>
                {data.operators.map((user) => (
                  <option key={user.identifier} value={user.identifier}>{user.fullName}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="warehouse">Bodega de origen</label>
              <select id="warehouse" value={warehouse} onChange={(event) => setWarehouse(event.target.value)}>
                <option value="">Elige una bodega</option>
                {data.warehouses.map(({ identifier, name, address }) => (
                  <option key={identifier} value={identifier}>{name} ({address.city})</option>
                ))}
              </select>
            </div>
          </div>
          <Alert type="error">{missing}</Alert>
          <button type="submit" className="primary" disabled={busy}>Crear envío</button>
        </>
      )}
    </form>
  );
}

function OrderView({ orderId }) {
  const { data, loading, error, reload } = useLoad(useCallback(async () => {
    const detail = await loadOrder("admin", orderId);
    return { ...detail, buyer: await getBuyer("admin", detail.order.buyerId) };
  }, [orderId]));
  const { busy, failure, run } = useAction();

  /** After each action (also when the order had changed) the page shows its current state. */
  async function act(action) {
    const problem = await run(action);
    if (!problem || problem.status) await reload();
  }

  const { order, shipment, buyer } = data ?? {};
  return (
    <>
      <p><Link to="/admin/pedidos">← Pedidos</Link></p>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <OrderDetail data={data} loading={loading} onReload={reload} subtitle={`${buyer.fullName} (${buyer.email})`}
                     notice={<Alert type="error">{failure}</Alert>}>
          {order.orderStatus === "PENDING_PAYMENT" && (
            <div className="actions">
              <ConfirmButton label="Confirmar pago" question={`¿Recibiste el pago de ${money(order.totalAmount)}?`}
                             confirmLabel="Sí, confirmar" disabled={busy}
                             onConfirm={() => act(() => confirmPayment(orderId))} />
            </div>
          )}
          {order.orderStatus === "PAID" && !shipment && (
            <AssignShipment busy={busy} onSubmit={(warehouse, operator) =>
              act(() => createShipment(orderId, warehouse, operator))} />
          )}
          <ShipmentActions key={shipment?.shipmentStatus} role="admin" order={order} shipment={shipment} busy={busy}
                           onAction={act} />
        </OrderDetail>
      )}
    </>
  );
}

// The key starts each order from scratch.
export default function AdminOrderPage() {
  const { orderId } = useParams();
  return <OrderView key={orderId} orderId={orderId} />;
}
