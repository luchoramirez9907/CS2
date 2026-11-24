import { request } from "./client.js";

const id = encodeURIComponent;

/** Warehouses and inventory share the same routes under the prefix of each role (seller, operator, admin). */
export const listWarehouses = (role) => request(`/${role}/warehouses`);
export const getWarehouse = (role, warehouseId) => request(`/${role}/warehouses/${id(warehouseId)}`);
export const registerWarehouse = (role, warehouse) =>
  request(`/${role}/warehouses`, { method: "POST", body: warehouse });
export const updateWarehouse = (role, warehouseId, warehouse) =>
  request(`/${role}/warehouses/${id(warehouseId)}`, { method: "PUT", body: warehouse });

/** Stock and movements of a product in a warehouse; null when it has no record there yet. */
export async function loadStock(role, productId, warehouseId) {
  const query = `productId=${id(productId)}&warehouseId=${id(warehouseId)}`;
  try {
    const [stock, movements] = await Promise.all([
      request(`/${role}/inventory?${query}`),
      request(`/${role}/inventory/movements?${query}`)
    ]);
    return { stock, movements };
  } catch (failure) {
    if (failure.detail?.startsWith("No inventory record exists")) return { stock: null, movements: [] };
    throw failure;
  }
}

export const registerMovement = (role, movement) =>
  request(`/${role}/inventory/movements`, { method: "POST", body: movement });
