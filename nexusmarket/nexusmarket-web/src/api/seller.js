import { request } from "./client.js";

const id = encodeURIComponent;

export const listMyProducts = () => request("/seller/products");
export const getMyProduct = (productId) => request(`/seller/products/${id(productId)}`);
export const publishProduct = (product) => request("/seller/products", { method: "POST", body: product });
export const updateProduct = (productId, product) =>
  request(`/seller/products/${id(productId)}`, { method: "PUT", body: product });
export const changeProductStatus = (productId, status) =>
  request(`/seller/products/${id(productId)}/status`, { method: "PATCH", body: { status } });
