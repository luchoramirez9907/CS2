import { useCallback } from "react";
import { listOrders } from "../../api/orders.js";
import { listUsers } from "../../api/people.js";
import Filter, { useFilter } from "../../components/Filter.jsx";
import LoadState from "../../components/LoadState.jsx";
import OrderList from "../../components/OrderList.jsx";
import { ORDER_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

/** Every order of the marketplace, for the administrator and the supervisor. */
export default function AllOrdersPage({ staff, base, title }) {
  const [status, setStatus] = useFilter("estado", ORDER_STATUS);
  const { data, loading, error, reload } = useLoad(useCallback(async () => {
    const [orders, buyers] = await Promise.all([listOrders(staff, status), listUsers(staff, "BUYER")]);
    return { status, orders, names: new Map(buyers.map((buyer) => [buyer.identifier, buyer.fullName])) };
  }, [staff, status]));
  const ready = data?.status === status;

  return (
    <>
      <h1>{title}</h1>
      <Filter id="status" label="Estado" options={ORDER_STATUS} value={status} onChange={setStatus} />
      <LoadState loading={loading && !ready} error={error} onRetry={reload} />
      {ready && (data.orders.length === 0
        ? <p>{status ? "No hay pedidos en este estado." : "Todavía no hay pedidos."}</p>
        : <OrderList orders={data.orders} base={base}
                     note={(order) => data.names.get(order.buyerId) ?? order.buyerId} />)}
    </>
  );
}
