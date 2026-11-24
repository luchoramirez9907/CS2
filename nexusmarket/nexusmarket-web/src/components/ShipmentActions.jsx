import { useState } from "react";
import { deliverShipment, dispatchShipment, failDelivery, finalizeOrder } from "../api/orders.js";
import ConfirmButton from "./ConfirmButton.jsx";

const MAX_REASON = 500;

function FailedDelivery({ busy, onSubmit }) {
  const [open, setOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  function handleSubmit(event) {
    event.preventDefault();
    if (!reason.trim()) {
      setError("Escribe por qué no se pudo entregar.");
      return;
    }
    onSubmit(reason.trim());
  }

  if (!open) {
    return (
      <button type="button" className="secondary" disabled={busy} onClick={() => setOpen(true)}>
        No se pudo entregar
      </button>
    );
  }
  return (
    <form className="inline-form" onSubmit={handleSubmit} noValidate>
      <label htmlFor="reason">¿Por qué no se pudo entregar?</label>
      <input id="reason" value={reason} maxLength={MAX_REASON} autoFocus placeholder="Cliente ausente"
             aria-invalid={Boolean(error)} aria-describedby={error ? "reason-error" : undefined}
             onChange={(event) => { setReason(event.target.value); setError(""); }} />
      {error && <span id="reason-error" className="field-error">{error}</span>}
      <div className="actions">
        <button type="submit" className="primary" disabled={busy}>Guardar</button>
        <button type="button" className="secondary" onClick={() => setOpen(false)}>Cancelar</button>
      </div>
    </form>
  );
}

/** Next step of the shipment, for the operator and the administrator. `onAction` runs it and reloads the order. */
export default function ShipmentActions({ role, order, shipment, busy, onAction }) {
  const { orderId } = order;
  if (!shipment || order.orderStatus === "DELIVERED") return null;

  const dispatch = () => onAction(() => dispatchShipment(role, orderId));
  switch (shipment.shipmentStatus) {
    case "PENDING":
      return (
        <div className="actions">
          <ConfirmButton label="Despachar" question="El pedido sale de la bodega y se descuenta del inventario."
                         confirmLabel="Sí, despachar" disabled={busy} onConfirm={dispatch} />
        </div>
      );
    case "RETURNED":
      return (
        <div className="actions">
          <ConfirmButton label="Despachar de nuevo" question="El pedido vuelve a salir hacia el comprador."
                         confirmLabel="Sí, despachar" disabled={busy} onConfirm={dispatch} />
        </div>
      );
    case "IN_TRANSIT":
      return (
        <div className="actions">
          <ConfirmButton label="Registrar entrega" question="¿El comprador recibió el pedido?"
                         confirmLabel="Sí, entregado" disabled={busy}
                         onConfirm={() => onAction(() => deliverShipment(role, orderId))} />
          <FailedDelivery busy={busy} onSubmit={(reason) => onAction(() => failDelivery(role, orderId, reason))} />
        </div>
      );
    case "DELIVERED":
      return (
        <div className="actions">
          <ConfirmButton label="Finalizar pedido" question="El pedido queda cerrado y no se podrá cambiar."
                         confirmLabel="Sí, finalizar" disabled={busy}
                         onConfirm={() => onAction(() => finalizeOrder(role, orderId))} />
        </div>
      );
    default:
      return null;
  }
}
