import { Link } from "react-router-dom";

export default function ForbiddenPage() {
  return (
    <>
      <h1>No tienes permiso</h1>
      <p className="muted">Esta página es de otro rol.</p>
      <Link to="/">Volver al inicio</Link>
    </>
  );
}
