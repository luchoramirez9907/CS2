import { address, date, money, ORDER_STATUS, SHIPMENT_STATUS, shortId, TRACKING_EVENT } from "../utils/format.js";

function Steps({ status }) {
  const codes = Object.keys(ORDER_STATUS);
  const reached = codes.indexOf(status);
  return (
    <ol className="steps" aria-label="Estado del pedido">
      {codes.map((code, index) => (
        <li key={code} className={index <= reached ? "done" : ""} aria-current={index === reached ? "step" : undefined}>
          {ORDER_STATUS[code]}
        </li>
      ))}
    </ol>
  );
}

function Invoice({ invoice }) {
  return (
    <section className="card summary" aria-label="Factura">
      <h2>Factura {shortId(invoice.invoiceId)}</h2>
      <p className="total"><span>Valor sin IVA</span><span>{money(invoice.totalAmount - invoice.taxAmount)}</span></p>
      <p className="total"><span>IVA</span><span>{money(invoice.taxAmount)}</span></p>
      <p className="total"><span>Total</span><strong>{money(invoice.totalAmount)}</strong></p>
    </section>
  );
}

// The API describes the events in English; only the reason of a failed delivery is free text.
function reason({ event, details }) {
  return event === "RETURNED" ? details.replace(/^Shipment returned to the warehouse:\s*/, "") : "";
}

function Shipment({ order, shipment, tracking }) {
  if (!shipment) {
    return (
      <p className="muted">
        {order.orderStatus === "DELIVERED" ? "Este pedido no necesitó envío." : "Todavía no hay un envío."}
      </p>
    );
  }
  return (
    <>
      <p>
        <span className="badge">{SHIPMENT_STATUS[shipment.shipmentStatus] ?? shipment.shipmentStatus}</span>{" "}
        {address(shipment.shippingAddress)}
      </p>
      <ol className="timeline">
        {tracking.map((event) => (
          <li key={`${event.event}-${event.occurredAt}`}>
            <strong>{TRACKING_EVENT[event.event] ?? event.event}</strong>
            {reason(event) && <span>: {reason(event)}</span>}
            <span className="muted">{date(event.occurredAt)}</span>
          </li>
        ))}
      </ol>
    </>
  );
}

/** Order as every role sees it. `notice` and `children` hold what each role can do with it. */
export default function OrderDetail({ data, loading, onReload, subtitle, notice, children }) {
  const { order, invoice, shipment, tracking } = data;
  return (
    <>
      <div className="heading">
        <div>
          <h1>Pedido {shortId(order.orderId)}</h1>
          <p className="muted">{subtitle && `${subtitle} · `}{date(order.creationDate)}</p>
        </div>
        <button type="button" className="secondary" disabled={loading} onClick={onReload}>
          {loading ? "Actualizando…" : "Actualizar"}
        </button>
      </div>
      {notice}
      <Steps status={order.orderStatus} />
      {children}
      <div className="columns">
        <ul className="card rows" aria-label="Productos">
          {order.items.map((item) => (
            <li key={item.productId} className="row">
              <span className="row-main">
                <strong>{item.productName}</strong>
                <span className="muted">{item.quantity} × {money(item.unitPrice)}</span>
              </span>
              <span className="row-total">{money(item.subtotal)}</span>
            </li>
          ))}
        </ul>
        {invoice && <Invoice invoice={invoice} />}
      </div>
      <section aria-label="Envío">
        <h2>Envío</h2>
        <Shipment order={order} shipment={shipment} tracking={tracking} />
      </section>
    </>
  );
}
