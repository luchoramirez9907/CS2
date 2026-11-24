import { Link } from "react-router-dom";
import { date, money, ORDER_STATUS, shortId } from "../utils/format.js";

/** Orders from newest to oldest. `base` is the page that opens each one. */
export default function OrderList({ orders, base, label = (order) => ORDER_STATUS[order.orderStatus], note }) {
  const sorted = [...orders].sort((a, b) => b.creationDate.localeCompare(a.creationDate));
  return (
    <ul className="card rows">
      {sorted.map((order) => (
        <li key={order.orderId}>
          <Link className="row row-link" to={`${base}/${order.orderId}`}>
            <span className="row-main">
              <strong>Pedido {shortId(order.orderId)}</strong>
              <span className="muted">{note && `${note(order)} · `}{date(order.creationDate)}</span>
            </span>
            <span className="badge">{label(order)}</span>
            <span className="row-total">{money(order.totalAmount)}</span>
          </Link>
        </li>
      ))}
    </ul>
  );
}
