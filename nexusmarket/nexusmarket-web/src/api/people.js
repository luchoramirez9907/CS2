import { request } from "./client.js";

const id = encodeURIComponent;
const send = (method, path, body) => request(path, { method, body });

// `staff` is "admin" or "supervisor": both consult the same routes under their prefix.
export const listUsers = (staff, role) => request(`/${staff}/users${role ? `?role=${id(role)}` : ""}`);
export const getUser = (staff, userId) => request(`/${staff}/users/${id(userId)}`);
export const getBuyer = (staff, buyerId) => request(`/${staff}/buyers/${id(buyerId)}`);

// Sellers and buyers have their own use cases; the other roles share the one of users.
const COLLECTION = { SELLER: "sellers", BUYER: "buyers" };
const path = (role, userId) => `/admin/${COLLECTION[role] ?? "users"}${userId ? `/${id(userId)}` : ""}`;

export const registerPerson = ({ role, ...person }) =>
  send("POST", path(role), COLLECTION[role] ? person : { ...person, role });
export const updatePerson = (role, userId, person) => send("PUT", path(role, userId), person);
export const changeUserStatus = (role, userId, status) =>
  send("PATCH", `${role === "SELLER" ? path(role, userId) : path("", userId)}/status`, { status });
export const changeCommercialStatus = (buyerId, status) =>
  send("PATCH", `${path("BUYER", buyerId)}/commercial-status`, { status });

export const getProfile = (role) => request(`/${role}/profile`);
export const updateProfile = (role, person) => send("PUT", `/${role}/profile`, person);
export const changePassword = (currentPassword, newPassword) =>
  send("PUT", "/auth/password", { currentPassword, newPassword });
