import { request } from "./client.js";

const id = encodeURIComponent;

/** Order operations share the same routes under the prefix of each role (buyer, admin, operator). */
const order = (role, orderId, rest = "") => `/${role}/orders/${id(orderId)}${rest}`;
const post = (path, body) => request(path, { method: "POST", body });

export const listOrders = (role, status) => request(`/${role}/orders${status ? `?status=${id(status)}` : ""}`);
export const getShipment = (role, orderId) => request(order(role, orderId, "/shipment"));

/** Order with its invoice (the operator has no access to it), shipment and tracking. */
export async function loadOrder(role, orderId) {
  const data = await request(order(role, orderId));
  const [invoice, shipment, tracking] = await Promise.all([
    data.invoiceId && role !== "operator" ? request(order(role, orderId, "/invoice")) : null,
    data.hasShipment ? getShipment(role, orderId) : null,
    data.hasShipment ? request(order(role, orderId, "/tracking")) : []
  ]);
  return { order: data, invoice, shipment, tracking };
}

export const confirmPayment = (orderId) => post(order("admin", orderId, "/payment"));
export const createShipment = (orderId, originWarehouseId, logisticsOperatorId) =>
  post(order("admin", orderId, "/shipment"), { originWarehouseId, logisticsOperatorId });

export const dispatchShipment = (role, orderId) => post(order(role, orderId, "/shipment/dispatch"));
export const deliverShipment = (role, orderId) => post(order(role, orderId, "/shipment/delivery"));
export const failDelivery = (role, orderId, reason) => post(order(role, orderId, "/shipment/return"), { reason });
export const finalizeOrder = (role, orderId) => post(order(role, orderId, "/finalization"));
