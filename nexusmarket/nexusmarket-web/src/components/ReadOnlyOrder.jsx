import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { loadOrder } from "../api/orders.js";
import { useLoad } from "../utils/useLoad.js";
import LoadState from "./LoadState.jsx";
import OrderDetail from "./OrderDetail.jsx";

function OrderView({ role, base, backLabel, orderId }) {
  const { data, loading, error, reload } = useLoad(useCallback(() => loadOrder(role, orderId), [role, orderId]));
  return (
    <>
      <p><Link to={base}>← {backLabel}</Link></p>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && <OrderDetail data={data} loading={loading} onReload={reload} />}
    </>
  );
}

/** Order without actions, for the seller and the supervisor. The key starts each order from scratch. */
export default function ReadOnlyOrder(props) {
  const { orderId } = useParams();
  return <OrderView key={orderId} orderId={orderId} {...props} />;
}
