import { useEffect, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth.js";
import { ROLES } from "../auth/roles.js";
import ErrorBoundary from "./ErrorBoundary.jsx";
import Icon from "./Icon.jsx";

export default function Layout() {
  const { user, logout, checkSession } = useAuth();
  const { pathname } = useLocation();
  const [leaving, setLeaving] = useState(false);
  const role = ROLES[user.role];

  useEffect(() => {
    checkSession();
  }, [pathname, checkSession]);

  useEffect(() => {
    const onVisible = () => document.visibilityState === "visible" && checkSession();
    document.addEventListener("visibilitychange", onVisible);
    return () => document.removeEventListener("visibilitychange", onVisible);
  }, [checkSession]);

  function handleLogout() {
    setLeaving(true);
    logout();
  }

  return (
    <div className="shell">
      <header className="topbar">
        <Link to="/" className="brand">NexusMarket</Link>
        <div className="account">
          <span className="account-name">{user.fullName}</span>
          <span className="badge">{role?.label ?? user.role}</span>
          <button type="button" className="link-button" onClick={handleLogout} disabled={leaving}>
            <Icon name="logout" /> {leaving ? "Cerrando…" : "Cerrar sesión"}
          </button>
        </div>
      </header>
      <nav className="menu" aria-label="Menú principal">
        <NavLink to="/" end><Icon name="home" /> Inicio</NavLink>
        {role?.menu.map((option) => (
          <NavLink key={option.path} to={option.path}><Icon name={option.icon} /> {option.label}</NavLink>
        ))}
      </nav>
      <main className="content">
        <ErrorBoundary key={pathname}><Outlet /></ErrorBoundary>
      </main>
    </div>
  );
}
