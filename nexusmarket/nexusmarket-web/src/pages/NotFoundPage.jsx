import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <>
      <h1>Página no encontrada</h1>
      <p className="muted">La dirección no existe.</p>
      <Link to="/">Volver al inicio</Link>
    </>
  );
}
