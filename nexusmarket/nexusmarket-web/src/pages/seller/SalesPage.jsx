import { useCallback } from "react";
import { listOrders } from "../../api/orders.js";
import LoadState from "../../components/LoadState.jsx";
import OrderList from "../../components/OrderList.jsx";
import { useLoad } from "../../utils/useLoad.js";

export default function SalesPage() {
  const { data, loading, error, reload } = useLoad(useCallback(() => listOrders("seller"), []));
  return (
    <>
      <h1>Ventas</h1>
      <p className="muted">Pedidos que incluyen tus productos. El total es el de tus productos.</p>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data?.length === 0 && <p>Todavía no tienes ventas.</p>}
      {data?.length > 0 && <OrderList orders={data} base="/vendedor/ventas" />}
    </>
  );
}
