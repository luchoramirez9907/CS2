import { useCallback } from "react";
import { getMyPerformance, getSellerPerformance } from "../../api/reports.js";
import LoadState from "../../components/LoadState.jsx";
import { date, money, PRODUCT_STATUS, RETURN_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";
import Counts from "./Counts.jsx";

/** Performance of a seller: its own, or the one the staff chooses. */
export default function SellerReport({ staff, sellerId }) {
  const { data, loading, error, reload } = useLoad(useCallback(
    () => (staff ? getSellerPerformance(staff, sellerId) : getMyPerformance()), [staff, sellerId]));
  if (!data) return <LoadState loading={loading} error={error} onRetry={reload} />;
  return (
    <>
      <p className="muted">{data.sellerName} · {date(data.generatedAt)}</p>
      <dl className="figures">
        <div><dt>Ventas</dt><dd>{money(data.salesAmount)}</dd></div>
        <div><dt>Pedidos</dt><dd>{data.ordersCount}</dd></div>
        <div><dt>Unidades vendidas</dt><dd>{data.unitsSold}</dd></div>
        <div><dt>Devoluciones</dt><dd>{data.returnsCount}</dd></div>
      </dl>
      <Counts title="Productos por estado" counts={data.productsByStatus} labels={PRODUCT_STATUS} />
      <Counts title="Devoluciones por estado" counts={data.returnsByStatus} labels={RETURN_STATUS} />
    </>
  );
}
