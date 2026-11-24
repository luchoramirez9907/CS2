import { useCallback, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { changeQuantity, checkout, clearCart, getCart, removeFromCart } from "../../api/buyer.js";
import { getProfile } from "../../api/people.js";
import Alert from "../../components/Alert.jsx";
import ConfirmButton from "../../components/ConfirmButton.jsx";
import LoadState from "../../components/LoadState.jsx";
import { address, money } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useLoad } from "../../utils/useLoad.js";

const MAX_UNITS = 1000;

function CartRow({ item, busy, onChange, onRemove }) {
  const [quantity, setQuantity] = useState(String(item.quantity));

  async function commit() {
    const units = Number(quantity);
    if (units === item.quantity) return;
    const valid = Number.isInteger(units) && units >= 1 && units <= MAX_UNITS;
    if (!valid || (await onChange(item.productId, units))) setQuantity(String(item.quantity));
  }

  return (
    <li className="row">
      <div className="row-main">
        <Link to={`/comprador/catalogo/${encodeURIComponent(item.productId)}`}><strong>{item.productName}</strong></Link>
        <span className="muted">{money(item.unitPrice)} c/u</span>
      </div>
      <input type="number" className="units" min="1" max={MAX_UNITS} value={quantity} disabled={busy}
             aria-label={`Cantidad de ${item.productName}`} onChange={(event) => setQuantity(event.target.value)}
             onBlur={commit} onKeyDown={(event) => event.key === "Enter" && event.currentTarget.blur()} />
      <span className="row-total">{money(item.subtotal)}</span>
      <button type="button" className="link-button" disabled={busy} onClick={() => onRemove(item.productId)}
              aria-label={`Quitar ${item.productName}`}>Quitar</button>
    </li>
  );
}

export default function CartPage() {
  const navigate = useNavigate();
  const { data: cart, loading, error, reload, setData } = useLoad(getCart);
  const { data: profile } = useLoad(useCallback(() => getProfile("buyer"), []));
  const { busy, failure, run } = useAction();

  const update = (action) => run(async () => setData(await action()));

  const handleCheckout = () => run(async () => {
    const order = await checkout();
    navigate(`/comprador/pedidos/${order.orderId}`, { state: { created: true } });
  });

  const items = cart?.items ?? [];
  return (
    <>
      <h1>Carrito</h1>
      <LoadState loading={loading && !cart} error={error} onRetry={reload} />
      <Alert type="error">{failure}</Alert>
      {!loading && !error && items.length === 0 && (
        <p>Tu carrito está vacío. <Link to="/comprador/catalogo">Ver el catálogo</Link></p>
      )}
      {items.length > 0 && (
        <div className="columns">
          <ul className="card rows">
            {items.map((item) => (
              <CartRow key={`${item.productId}-${item.quantity}`} item={item} busy={busy}
                       onChange={(productId, units) => update(() => changeQuantity(productId, units))}
                       onRemove={(productId) => update(() => removeFromCart(productId))} />
            ))}
          </ul>
          <section className="card summary" aria-label="Resumen">
            <p className="total"><span>Total</span><strong>{money(cart.total)}</strong></p>
            <p className="muted">El precio incluye IVA.</p>
            {profile && <p><strong>Tu dirección</strong><br />{address(profile.primaryAddress)}</p>}
            <button type="button" className="primary block" disabled={busy} onClick={handleCheckout}>
              Confirmar pedido
            </button>
            <ConfirmButton label="Vaciar carrito" question="¿Quitar todos los productos?" confirmLabel="Sí, vaciar"
                           className="link-button" disabled={busy} onConfirm={() => update(clearCart)} />
          </section>
        </div>
      )}
    </>
  );
}
