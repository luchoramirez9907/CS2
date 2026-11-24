import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { registerBuyer } from "../api/buyer.js";
import { useAuth } from "../auth/useAuth.js";
import Alert from "../components/Alert.jsx";
import FieldList from "../components/FieldList.jsx";
import { ADDRESS_FIELDS, IDENTIFIER, PASSWORD, PERSON_FIELDS, toAddress } from "../utils/fields.js";
import { useForm } from "../utils/useForm.js";

const FIELDS = [IDENTIFIER, ...PERSON_FIELDS, PASSWORD, ...ADDRESS_FIELDS];

export default function RegisterPage() {
  const { user, login, showNotice } = useAuth();
  const navigate = useNavigate();
  const form = useForm(FIELDS);
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  if (user) return <Navigate to="/" replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    if (!form.check(event.currentTarget)) return;
    setSending(true);
    const { text, values } = form;
    try {
      await registerBuyer({
        identifier: text("identifier"),
        fullName: text("fullName"),
        email: text("email"),
        password: values.password,
        primaryAddress: toAddress(text)
      });
    } catch (failure) {
      setError(failure.message);
      setSending(false);
      return;
    }
    try {
      await login(text("email"), values.password);
    } catch {
      showNotice("Tu cuenta quedó creada. Ingresa con tu correo y tu contraseña.");
      navigate("/login", { replace: true });
    }
  }

  return (
    <main className="login">
      <form className="card login-card wide" onSubmit={handleSubmit} noValidate>
        <h1 className="brand">NexusMarket</h1>
        <p className="muted">Crea tu cuenta de comprador</p>
        <FieldList fields={FIELDS} form={form} />
        <Alert type="error">{error}</Alert>
        <button type="submit" className="primary block" disabled={sending}>
          {sending ? "Creando…" : "Crear cuenta"}
        </button>
        <p className="center">¿Ya tienes cuenta? <Link to="/login">Ingresa</Link></p>
      </form>
    </main>
  );
}
