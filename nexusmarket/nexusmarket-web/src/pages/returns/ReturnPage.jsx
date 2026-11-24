import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import {
  approveReturn, completeReturn, loadReturn, processRefund, rejectRefund, rejectReturn
} from "../../api/returns.js";
import Alert from "../../components/Alert.jsx";
import ConfirmButton from "../../components/ConfirmButton.jsx";
import FieldList from "../../components/FieldList.jsx";
import LoadState from "../../components/LoadState.jsx";
import { date, money, REFUND_STATUS, RETURN_STATUS, shortId } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useForm } from "../../utils/useForm.js";
import { useLoad } from "../../utils/useLoad.js";

function RefundForm({ total, busy, onRefund, onReject }) {
  const fields = [{
    name: "amount", label: "Valor del reembolso", type: "number", hint: `Máximo ${money(total)}, el total del pedido.`,
    rule: (value) => {
      const amount = Number(value);
      if (!(amount > 0 && amount <= total)) return `Escribe un valor mayor a 0 y hasta ${money(total)}.`;
      return /^\d+([.]\d{1,2})?$/.test(value.trim()) ? "" : "Usa máximo dos decimales.";
    }
  }];
  const form = useForm(fields, { amount: total });

  function handleSubmit(event) {
    event.preventDefault();
    if (form.check(event.currentTarget, fields)) onRefund(Number(form.text("amount")));
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <h2>Reembolso</h2>
      <FieldList fields={fields} form={form} />
      <div className="actions">
        <button type="submit" className="primary" disabled={busy}>Reembolsar</button>
        <ConfirmButton label="Rechazar reembolso" className="secondary" disabled={busy}
                       question="No se devuelve dinero y no se podrá cambiar." confirmLabel="Sí, rechazar"
                       onConfirm={onReject} />
      </div>
    </form>
  );
}

/** Decisions of the administrator on a return. */
function Decisions({ data, order, refund, busy, act }) {
  const { returnId, returnStatus } = data;
  if (returnStatus === "REQUESTED") {
    return (
      <div className="actions">
        <ConfirmButton label="Aprobar" question="Después podrás reembolsar y devolver las unidades al inventario."
                       confirmLabel="Sí, aprobar" disabled={busy} onConfirm={() => act(() => approveReturn(returnId))} />
        <ConfirmButton label="Rechazar" className="secondary" question="El comprador podrá pedir otra devolución."
                       confirmLabel="Sí, rechazar" disabled={busy} onConfirm={() => act(() => rejectReturn(returnId))} />
      </div>
    );
  }
  if (returnStatus === "REJECTED") return null;
  return (
    <>
      {returnStatus === "APPROVED" && (
        <div className="actions">
          <ConfirmButton label="Completar devolución" question="¿La mercancía volvió? Las unidades vuelven al inventario."
                         confirmLabel="Sí, completar" disabled={busy}
                         onConfirm={() => act(() => completeReturn(returnId))} />
        </div>
      )}
      {!refund && (
        <RefundForm total={order.totalAmount} busy={busy}
                    onRefund={(amount) => act(() => processRefund(returnId, amount))}
                    onReject={() => act(() => rejectRefund(returnId))} />
      )}
    </>
  );
}

function ReturnView({ role, base, orders, returnId }) {
  const { data: loaded, loading, error, reload } =
    useLoad(useCallback(() => loadReturn(role, returnId), [role, returnId]));
  const { busy, failure, run } = useAction();

  /** After each action (also when the return had changed) the page shows its current state. */
  async function act(action) {
    const problem = await run(action);
    if (!problem || problem.status) await reload();
  }

  const { data, order, refund } = loaded ?? {};
  return (
    <>
      <p><Link to={base}>← Devoluciones</Link></p>
      <LoadState loading={loading && !loaded} error={error} onRetry={reload} />
      {loaded && (
        <>
          <div className="heading">
            <div>
              <h1>Devolución {shortId(data.returnId)}</h1>
              <p className="muted">{date(data.requestDate)}</p>
            </div>
            <span className="badge">{RETURN_STATUS[data.returnStatus] ?? data.returnStatus}</span>
          </div>
          <Alert type="error">{failure}</Alert>
          <dl className="pairs">
            <div><dt>Motivo</dt><dd>{data.reason}</dd></div>
            <div>
              <dt>Pedido</dt>
              <dd><Link to={`${orders}/${data.orderId}`}>{shortId(data.orderId)}</Link> · {money(order.totalAmount)}</dd>
            </div>
            {role !== "seller" && (
              <div>
                <dt>Reembolso</dt>
                <dd>
                  {refund ? REFUND_STATUS[refund.refundStatus] : "Sin decidir"}
                  {refund?.refundStatus === "PROCESSED" && `: ${money(refund.amount)}`}
                </dd>
              </div>
            )}
          </dl>
          {role === "admin" && <Decisions data={data} order={order} refund={refund} busy={busy} act={act} />}
        </>
      )}
    </>
  );
}

// The key starts each return from scratch.
export default function ReturnPage(props) {
  const { returnId } = useParams();
  return <ReturnView key={returnId} returnId={returnId} {...props} />;
}
