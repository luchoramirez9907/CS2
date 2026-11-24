import { ApiError } from "./errors.js";
import { readSession } from "./session.js";

const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";

let onSessionExpired = () => {};

export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler;
}

/** Single entry point to the API: adds the token and normalizes every error. */
export async function request(path, { method = "GET", body } = {}) {
  const token = readSession()?.token;
  let response;
  try {
    response = await fetch(API_URL + path, {
      method,
      headers: {
        Accept: "application/json",
        ...(body !== undefined && { "Content-Type": "application/json" }),
        ...(token && { Authorization: `Bearer ${token}` })
      },
      body: body === undefined ? undefined : JSON.stringify(body)
    });
  } catch {
    throw new ApiError(0, "CONNECTION_FAILED");
  }
  const json = await response.json().catch(() => null);
  if (!response.ok) {
    // Only the token still in use ends the session: not a late answer to an old one, nor a wrong password.
    const rejected = response.status === 401 && json?.code !== "INVALID_CREDENTIALS";
    if (rejected && token && token === readSession()?.token) onSessionExpired();
    throw new ApiError(response.status, json?.code, json?.message);
  }
  return json?.data;
}
