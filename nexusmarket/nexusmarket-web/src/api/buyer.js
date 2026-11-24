import { request } from "./client.js";

const id = encodeURIComponent;

export const registerBuyer = (buyer) => request("/public/buyers", { method: "POST", body: buyer });

export const searchCatalog = (text) => request(`/public/products${text ? `?q=${id(text)}` : ""}`);
export const getProduct = (productId) => request(`/public/products/${id(productId)}`);

export const getCart = () => request("/buyer/cart");
export const addToCart = (productId, quantity) =>
  request("/buyer/cart/items", { method: "POST", body: { productId, quantity } });
export const changeQuantity = (productId, quantity) =>
  request(`/buyer/cart/items/${id(productId)}`, { method: "PUT", body: { quantity } });
export const removeFromCart = (productId) => request(`/buyer/cart/items/${id(productId)}`, { method: "DELETE" });
export const clearCart = () => request("/buyer/cart", { method: "DELETE" });
export const checkout = () => request("/buyer/orders", { method: "POST" });
