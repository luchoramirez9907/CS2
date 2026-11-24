import { useCallback, useEffect, useMemo, useState } from "react";
import { request, setSessionExpiredHandler } from "../api/client.js";
import { clearSession, readSession, saveSession } from "../api/session.js";
import { ROLES } from "./roles.js";
import { AuthContext } from "./useAuth.js";

const EXPIRED = "Tu sesión terminó. Vuelve a ingresar.";

export function AuthProvider({ children }) {
  const [session, setSession] = useState(readSession);
  const [notice, setNotice] = useState("");

  const endSession = useCallback((message) => {
    clearSession();
    setSession(null);
    setNotice(message);
  }, []);

  useEffect(() => {
    setSessionExpiredHandler(() => endSession(EXPIRED));
    return () => setSessionExpiredHandler(() => {});
  }, [endSession]);

  // The token lasts one hour and cannot be renewed.
  useEffect(() => {
    if (!session) return undefined;
    const timer = setTimeout(() => endSession(EXPIRED), session.expiresAt - Date.now());
    return () => clearTimeout(timer);
  }, [session, endSession]);

  const login = useCallback(async (email, password) => {
    const data = await request("/auth/login", { method: "POST", body: { email, password } });
    const next = { token: data.token, user: data.user, expiresAt: Date.now() + data.expiresIn * 1000 };
    saveSession(next);
    setNotice("");
    setSession(next);
  }, []);

  const updateUser = useCallback((changes) => {
    setSession((previous) => {
      const next = { ...previous, user: { ...previous.user, ...changes } };
      saveSession(next);
      return next;
    });
  }, []);

  const logout = useCallback(async () => {
    let message = "Cerraste sesión.";
    try {
      await request("/auth/logout", { method: "POST" });
    } catch (error) {
      // A 401 means the token was already invalid: the session is closed anyway.
      if (error.status !== 401) {
        message = "Cerraste sesión en este navegador, pero no pudimos avisar al servidor.";
      }
    }
    endSession(message);
  }, [endSession]);

  // A blocked user or a logout from another tab is noticed here (the API answers 401).
  const checkSession = useCallback(async () => {
    const user = readSession()?.user;
    const role = user && ROLES[user.role];
    if (role) await request(role.sessionPath(user.identifier)).catch(() => {});
  }, []);

  const value = useMemo(
    () => ({
      user: session?.user ?? null, notice, showNotice: setNotice, login, logout, checkSession, endSession, updateUser
    }),
    [session, notice, login, logout, checkSession, endSession, updateUser]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
