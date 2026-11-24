// The session lives in sessionStorage: it is cleared when the tab is closed.
const KEY = "nexusmarket.session";

export function readSession() {
  try {
    const session = JSON.parse(sessionStorage.getItem(KEY));
    return session && session.expiresAt > Date.now() ? session : null;
  } catch {
    return null;
  }
}

export function saveSession(session) {
  sessionStorage.setItem(KEY, JSON.stringify(session));
}

export function clearSession() {
  sessionStorage.removeItem(KEY);
}
