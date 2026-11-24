import { useState } from "react";

/** Button that asks before acting. */
export default function ConfirmButton({ label, question, confirmLabel, className = "primary", disabled, onConfirm }) {
  const [asking, setAsking] = useState(false);
  if (!asking) {
    return (
      <button type="button" className={className} disabled={disabled} onClick={() => setAsking(true)}>{label}</button>
    );
  }
  return (
    <div className="confirm">
      <span>{question}</span>
      <button type="button" className="primary" disabled={disabled} onClick={() => { setAsking(false); onConfirm(); }}>
        {confirmLabel}
      </button>
      <button type="button" className="secondary" onClick={() => setAsking(false)}>Cancelar</button>
    </div>
  );
}
