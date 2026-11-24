import { useCallback, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { changeCommercialStatus, changeUserStatus, getBuyer, getUser, updatePerson } from "../../api/people.js";
import { ROLES } from "../../auth/roles.js";
import { useAuth } from "../../auth/useAuth.js";
import Alert from "../../components/Alert.jsx";
import FieldList from "../../components/FieldList.jsx";
import LoadState from "../../components/LoadState.jsx";
import { ADDRESS_FIELDS, PERSON_FIELDS, toAddress } from "../../utils/fields.js";
import { address, COMMERCIAL_STATUS, USER_STATUS } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useForm } from "../../utils/useForm.js";
import { useLoad } from "../../utils/useLoad.js";

async function loadPerson(staff, userId) {
  const user = await getUser(staff, userId);
  return { user, buyer: user.role === "BUYER" ? await getBuyer(staff, userId) : null };
}

function EditForm({ user, buyer, busy, onSave }) {
  const fields = buyer ? [...PERSON_FIELDS, ...ADDRESS_FIELDS] : PERSON_FIELDS;
  const form = useForm(fields, { ...user, ...buyer?.primaryAddress });

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.check(event.currentTarget)) return;
    const { text } = form;
    onSave({
      fullName: text("fullName"),
      email: text("email"),
      ...(buyer && { primaryAddress: toAddress(text) })
    });
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <h2>Datos</h2>
      <FieldList fields={fields} form={form} />
      <button type="submit" className="primary" disabled={busy}>Guardar cambios</button>
    </form>
  );
}

function StatusForm({ id, title, hint, options, current, busy, onSave }) {
  const [value, setValue] = useState(current);
  return (
    <form className="card panel" onSubmit={(event) => { event.preventDefault(); onSave(value); }}>
      <h2>{title}</h2>
      <p className="muted">{hint}</p>
      <div className="search compact">
        <select id={id} aria-label={title} value={value} onChange={(event) => setValue(event.target.value)}>
          {Object.entries(options).map(([code, label]) => <option key={code} value={code}>{label}</option>)}
        </select>
        <button type="submit" className="secondary" disabled={busy || value === current}>Cambiar</button>
      </div>
    </form>
  );
}

function UserView({ staff, base, userId }) {
  const { user: me } = useAuth();
  const { state } = useLocation();
  const { data, loading, error, reload } = useLoad(useCallback(() => loadPerson(staff, userId), [staff, userId]));
  const { busy, failure, run } = useAction();
  const [done, setDone] = useState(state?.created ? "Usuario creado." : "");

  async function act(action, message) {
    setDone("");
    const problem = await run(action);
    if (!problem) setDone(message);
    if (!problem || problem.status) await reload();
  }

  const { user, buyer } = data ?? {};
  return (
    <>
      <p><Link to={base}>← Usuarios</Link></p>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <>
          <div className="heading">
            <div>
              <h1>{user.fullName}</h1>
              <p className="muted">{user.identifier} · {user.email} · {ROLES[user.role]?.label ?? user.role}</p>
            </div>
            <span className={user.status === "ACTIVE" ? "badge" : "badge badge-off"}>
              {USER_STATUS[user.status] ?? user.status}
            </span>
          </div>
          <Alert type="error">{failure}</Alert>
          <Alert>{done}</Alert>
          {buyer && staff === "supervisor" && (
            <dl className="pairs">
              <div><dt>Dirección</dt><dd>{address(buyer.primaryAddress)}</dd></div>
              <div><dt>Compras</dt><dd>{COMMERCIAL_STATUS[buyer.commercialStatus]}</dd></div>
            </dl>
          )}
          {staff === "admin" && (
            <>
              <EditForm user={user} buyer={buyer} busy={busy} onSave={(person) =>
                act(() => updatePerson(user.role, userId, person), "Datos guardados.")} />
              {user.identifier !== me.identifier && (
                <StatusForm key={user.status} id="user-status" title="Estado de la cuenta" options={USER_STATUS}
                            hint="Una cuenta que no está activa no puede entrar, y su sesión se cierra."
                            current={user.status} busy={busy} onSave={(status) =>
                              act(() => changeUserStatus(user.role, userId, status), "Estado de la cuenta cambiado.")} />
              )}
              {buyer && (
                <StatusForm key={buyer.commercialStatus} id="commercial-status" title="Permiso para comprar"
                            options={COMMERCIAL_STATUS} hint="Sin permiso puede entrar, pero no comprar."
                            current={buyer.commercialStatus} busy={busy} onSave={(status) =>
                              act(() => changeCommercialStatus(userId, status), "Permiso para comprar cambiado.")} />
              )}
            </>
          )}
        </>
      )}
    </>
  );
}

// The key starts each user from scratch.
export default function UserPage(props) {
  const { userId } = useParams();
  return <UserView key={userId} userId={userId} {...props} />;
}
