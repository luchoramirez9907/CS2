import { useCallback } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { loadOrder } from "../../api/orders.js";
import { listReturns, requestReturn } from "../../api/returns.js";
import Alert from "../../components/Alert.jsx";
import FieldList from "../../components/FieldList.jsx";
import LoadState from "../../components/LoadState.jsx";
import OrderDetail from "../../components/OrderDetail.jsx";
import { date, RETURN_STATUS } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useForm } from "../../utils/useForm.js";
import { useLoad } from "../../utils/useLoad.js";

const REASON = [{ name: "reason", label: "¿Por qué quieres devolver este pedido?", max: 500, wide: true,
  missing: "Escribe por qué quieres devolverlo." }];

/** Only delivered orders can be returned, and they accept one active return. */
async function loadWithReturns(orderId) {
  const detail = await loadOrder("buyer", orderId);
  const returns = detail.order.orderStatus === "DELIVERED" ? await listReturns("buyer") : [];
  return { ...detail, returns: returns.filter((item) => item.orderId === orderId) };
}

function Returns({ orderId, returns }) {
  const navigate = useNavigate();
  const form = useForm(REASON);
  const { busy, failure, run } = useAction();
  const active = returns.some((item) => item.returnStatus !== "REJECTED");

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.check(event.currentTarget)) return;
    run(async () => {
      const { returnId } = await requestReturn(orderId, form.text("reason"));
      navigate(`/comprador/devoluciones/${returnId}`);
    });
  }

  return (
    <section aria-label="Devoluciones">
      <h2>Devoluciones</h2>
      {returns.map((item) => (
        <p key={item.returnId}>
          <Link to={`/comprador/devoluciones/${item.returnId}`}>{item.reason}</Link>{" "}
          <span className="badge">{RETURN_STATUS[item.returnStatus]}</span>{" "}
          <span className="muted">{date(item.requestDate)}</span>
        </p>
      ))}
      {!active && (
        <form className="card panel" onSubmit={handleSubmit} noValidate>
          <FieldList fields={REASON} form={form} />
          <Alert type="error">{failure}</Alert>
          <button type="submit" className="primary" disabled={busy}>
            {busy ? "Enviando…" : "Solicitar devolución"}
          </button>
        </form>
      )}
    </section>
  );
}

function OrderView({ orderId }) {
  const { state } = useLocation();
  const { data, loading, error, reload } = useLoad(useCallback(() => loadWithReturns(orderId), [orderId]));
  const status = data?.order.orderStatus;
  return (
    <>
      <p><Link to="/comprador/pedidos">← Pedidos</Link></p>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <OrderDetail data={data} loading={loading} onReload={reload} notice={status === "PENDING_PAYMENT" && (
          <Alert>{state?.created && "Tu pedido quedó creado. "}Un administrador confirmará tu pago.</Alert>
        )} />
      )}
      {status === "DELIVERED" && <Returns orderId={orderId} returns={data.returns} />}
    </>
  );
}

// The key starts each order from scratch.
export default function OrderPage() {
  const { orderId } = useParams();
  return <OrderView key={orderId} orderId={orderId} />;
}
