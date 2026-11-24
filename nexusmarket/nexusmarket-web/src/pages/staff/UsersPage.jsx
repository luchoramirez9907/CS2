import { useCallback } from "react";
import { Link } from "react-router-dom";
import { listUsers } from "../../api/people.js";
import { ROLES } from "../../auth/roles.js";
import Filter, { useFilter } from "../../components/Filter.jsx";
import LoadState from "../../components/LoadState.jsx";
import { USER_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

const ROLE_LABELS = Object.fromEntries(Object.entries(ROLES).map(([code, { label }]) => [code, label]));

/** Accounts of the platform. Only the administrator can create and change them. */
export default function UsersPage({ staff, base }) {
  const [role, setRole] = useFilter("rol", ROLE_LABELS);
  const { data, loading, error, reload } = useLoad(useCallback(async () =>
    ({ role, users: await listUsers(staff, role) }), [staff, role]));
  const ready = data?.role === role;

  return (
    <>
      <div className="heading">
        <h1>Usuarios</h1>
        {staff === "admin" && <Link className="primary" to={`${base}/nuevo`}>Crear usuario</Link>}
      </div>
      <Filter id="role" label="Rol" options={ROLE_LABELS} value={role} onChange={setRole} />
      <LoadState loading={loading && !ready} error={error} onRetry={reload} />
      {ready && data.users.length === 0 && <p>No hay usuarios con este rol.</p>}
      {ready && data.users.length > 0 && (
        <ul className="card rows">
          {data.users.map((user) => (
            <li key={user.identifier}>
              <Link className="row row-link" to={`${base}/${encodeURIComponent(user.identifier)}`}>
                <span className="row-main">
                  <strong>{user.fullName}</strong>
                  <span className="muted">{user.email}</span>
                </span>
                <span>{ROLE_LABELS[user.role] ?? user.role}</span>
                <span className={user.status === "ACTIVE" ? "badge" : "badge badge-off"}>
                  {USER_STATUS[user.status] ?? user.status}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
