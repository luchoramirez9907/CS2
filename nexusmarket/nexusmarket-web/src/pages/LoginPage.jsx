import { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth.js";
import Alert from "../components/Alert.jsx";

export default function LoginPage() {
  const { user, notice, login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  if (user) return <Navigate to="/" replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    if (!email.trim() || !password) {
      setError("Escribe tu correo y tu contraseña.");
      return;
    }
    setError("");
    setSending(true);
    try {
      await login(email.trim(), password);
    } catch (failure) {
      setError(failure.status === 403
        ? "Tu cuenta no está activa. Comunícate con un administrador."
        : failure.message);
      setPassword("");
      setSending(false);
    }
  }

  return (
    <main className="login">
      <form className="card login-card" onSubmit={handleSubmit} noValidate>
        <h1 className="brand">NexusMarket</h1>
        <p className="muted">Ingresa a tu cuenta</p>
        {!error && <Alert>{notice}</Alert>}
        <label htmlFor="email">Correo</label>
        <input id="email" type="email" autoComplete="username" autoFocus value={email}
               onChange={(event) => setEmail(event.target.value)} />
        <label htmlFor="password">Contraseña</label>
        <input id="password" type="password" autoComplete="current-password" value={password}
               onChange={(event) => setPassword(event.target.value)} />
        <Alert type="error">{error}</Alert>
        <button type="submit" className="primary block" disabled={sending}>
          {sending ? "Ingresando…" : "Ingresar"}
        </button>
        <p className="center">¿No tienes cuenta? <Link to="/registro">Regístrate como comprador</Link></p>
      </form>
    </main>
  );
}
