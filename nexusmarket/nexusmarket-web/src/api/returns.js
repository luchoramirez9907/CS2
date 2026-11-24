import { request } from "./client.js";

const id = encodeURIComponent;
const post = (path, body) => request(path, { method: "POST", body });

export const listReturns = (role) => request(`/${role}/returns`);

async function loadRefund(role, returnId) {
  try {
    return await request(`/${role}/returns/${id(returnId)}/refund`);
  } catch (failure) {
    if (failure.detail?.endsWith("has no refund")) return null;
    throw failure;
  }
}

/** Return with its order and its refund (the seller has no access to the refund). */
export async function loadReturn(role, returnId) {
  const data = await request(`/${role}/returns/${id(returnId)}`);
  const [order, refund] = await Promise.all([
    request(`/${role}/orders/${id(data.orderId)}`),
    role === "seller" ? null : loadRefund(role, returnId)
  ]);
  return { data, order, refund };
}

export const requestReturn = (orderId, reason) => post(`/buyer/orders/${id(orderId)}/returns`, { reason });

const decide = (returnId, action, body) => post(`/admin/returns/${id(returnId)}/${action}`, body);
export const approveReturn = (returnId) => decide(returnId, "approval");
export const rejectReturn = (returnId) => decide(returnId, "rejection");
export const completeReturn = (returnId) => decide(returnId, "completion");
export const processRefund = (returnId, amount) => decide(returnId, "refund", { amount });
export const rejectRefund = (returnId) => decide(returnId, "refund/rejection");
