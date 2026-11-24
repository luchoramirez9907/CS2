import { useCallback, useState } from "react";
import { changePassword, getProfile, getUser, updatePerson, updateProfile } from "../api/people.js";
import { useAuth } from "../auth/useAuth.js";
import Alert from "../components/Alert.jsx";
import FieldList from "../components/FieldList.jsx";
import LoadState from "../components/LoadState.jsx";
import { ADDRESS_FIELDS, passwordRule, PERSON_FIELDS, toAddress } from "../utils/fields.js";
import { useAction } from "../utils/useAction.js";
import { useForm } from "../utils/useForm.js";
import { useLoad } from "../utils/useLoad.js";

const CURRENT = { name: "currentPassword", label: "Contraseña actual", type: "password", max: 100,
  autoComplete: "current-password" };
const NEW = { name: "newPassword", label: "Contraseña nueva", type: "password", max: 100, autoComplete: "new-password",
  hint: "Mínimo 8 caracteres, con una letra y un número.", rule: passwordRule };
const REPEAT = { name: "repeatPassword", label: "Repite la contraseña nueva", type: "password", max: 100,
  autoComplete: "new-password", missing: "Escribe otra vez la contraseña nueva." };

function PasswordForm() {
  const { endSession } = useAuth();
  const form = useForm([CURRENT, NEW, REPEAT]);
  const { busy, failure, run } = useAction();
  const { values } = form;
  const fields = [CURRENT, NEW, { ...REPEAT,
    rule: (value) => (value === values.newPassword ? "" : "Las contraseñas no coinciden.") }];

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.check(event.currentTarget, fields)) return;
    run(async () => {
      try {
        await changePassword(values.currentPassword, values.newPassword);
      } catch (problem) {
        throw problem.code === "INVALID_CREDENTIALS" ? new Error("La contraseña actual no es correcta.") : problem;
      }
      // The API invalidates every previous token.
      endSession("Cambiaste tu contraseña. Ingresa con la nueva.");
    });
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <h2>Cambiar contraseña</h2>
      <FieldList fields={fields} form={form} />
      <Alert type="error">{failure}</Alert>
      <button type="submit" className="primary" disabled={busy}>{busy ? "Cambiando…" : "Cambiar contraseña"}</button>
    </form>
  );
}

function DataForm({ person, busy, onSave }) {
  const fields = person.primaryAddress ? [...PERSON_FIELDS, ...ADDRESS_FIELDS] : PERSON_FIELDS;
  const form = useForm(fields, { ...person, ...person.primaryAddress });

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.check(event.currentTarget)) return;
    const { text } = form;
    onSave({
      fullName: text("fullName"),
      email: text("email"),
      ...(person.primaryAddress && { primaryAddress: toAddress(text) })
    });
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <h2>Tus datos</h2>
      <FieldList fields={fields} form={form} />
      <button type="submit" className="primary" disabled={busy}>Guardar cambios</button>
    </form>
  );
}

/** Own account. Administrator and supervisor have no profile route: they use the one of users. */
export default function ProfilePage({ role }) {
  const { user, updateUser } = useAuth();
  const staff = role === "admin" || role === "supervisor";
  const { data, loading, error, reload, setData } = useLoad(useCallback(
    () => (staff ? getUser(role, user.identifier) : getProfile(role)), [staff, role, user.identifier]));
  const { busy, failure, run } = useAction();
  const [done, setDone] = useState("");

  async function handleSave(person) {
    setDone("");
    const problem = await run(async () => {
      const saved = await (staff ? updatePerson(user.role, user.identifier, person) : updateProfile(role, person));
      setData(saved);
      updateUser({ fullName: saved.fullName, email: saved.email });
    });
    if (!problem) setDone("Datos guardados.");
  }

  return (
    <>
      <h1>Perfil</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      <Alert type="error">{failure}</Alert>
      <Alert>{done}</Alert>
      {data && role !== "supervisor" && <DataForm person={data} busy={busy} onSave={handleSave} />}
      {data && role === "supervisor" && (
        <p className="muted">{data.fullName} · {data.email}. Un administrador puede cambiar estos datos.</p>
      )}
      <PasswordForm />
    </>
  );
}
