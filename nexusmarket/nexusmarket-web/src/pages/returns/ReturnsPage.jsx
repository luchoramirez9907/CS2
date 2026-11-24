import { useCallback } from "react";
import { Link } from "react-router-dom";
import { listReturns } from "../../api/returns.js";
import Filter, { useFilter } from "../../components/Filter.jsx";
import LoadState from "../../components/LoadState.jsx";
import { date, RETURN_STATUS, shortId } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

/** Returns each role can see, from newest to oldest. */
export default function ReturnsPage({ role, base }) {
  const [status, setStatus] = useFilter("estado", RETURN_STATUS);
  const { data, loading, error, reload } = useLoad(useCallback(() => listReturns(role), [role]));
  const returns = (data ?? [])
    .filter((item) => !status || item.returnStatus === status)
    .sort((a, b) => b.requestDate.localeCompare(a.requestDate));
  return (
    <>
      <h1>Devoluciones</h1>
      {role === "buyer" && <p className="muted">Para pedir una, abre un pedido entregado.</p>}
      <Filter id="status" label="Estado" options={RETURN_STATUS} value={status} onChange={setStatus} />
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && returns.length === 0 && <p>{status ? "No hay devoluciones en este estado." : "No hay devoluciones."}</p>}
      {returns.length > 0 && (
        <ul className="card rows">
          {returns.map((item) => (
            <li key={item.returnId}>
              <Link className="row row-link" to={`${base}/${item.returnId}`}>
                <span className="row-main">
                  <strong>{item.reason}</strong>
                  <span className="muted">Pedido {shortId(item.orderId)} · {date(item.requestDate)}</span>
                </span>
                <span className="badge">{RETURN_STATUS[item.returnStatus] ?? item.returnStatus}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
