import Alert from "./Alert.jsx";

/** Shared "loading" and "failed" states of a page. */
export default function LoadState({ loading, error, onRetry }) {
  if (error) {
    return (
      <>
        <Alert type="error">{error}</Alert>
        <button type="button" className="secondary" onClick={onRetry}>Reintentar</button>
      </>
    );
  }
  return loading ? <p className="muted" role="status">Cargando…</p> : null;
}
