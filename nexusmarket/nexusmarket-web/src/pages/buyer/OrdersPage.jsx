import { useCallback } from "react";
import { Link } from "react-router-dom";
import { listOrders } from "../../api/orders.js";
import LoadState from "../../components/LoadState.jsx";
import OrderList from "../../components/OrderList.jsx";
import { useLoad } from "../../utils/useLoad.js";

export default function OrdersPage() {
  const { data, loading, error, reload } = useLoad(useCallback(() => listOrders("buyer"), []));
  return (
    <>
      <h1>Pedidos</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data?.length === 0 && (
        <p>Todavía no tienes pedidos. <Link to="/comprador/catalogo">Ver el catálogo</Link></p>
      )}
      {data?.length > 0 && <OrderList orders={data} base="/comprador/pedidos" />}
    </>
  );
}
