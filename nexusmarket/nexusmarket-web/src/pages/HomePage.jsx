import { Link } from "react-router-dom";
import { useAuth } from "../auth/useAuth.js";
import { ROLES } from "../auth/roles.js";
import Icon from "../components/Icon.jsx";

export default function HomePage() {
  const { user } = useAuth();
  const menu = ROLES[user.role]?.menu ?? [];
  return (
    <>
      <h1>Hola, {user.fullName}</h1>
      <p className="muted">Elige qué quieres hacer.</p>
      <ul className="tiles">
        {menu.map((option) => (
          <li key={option.path}>
            <Link className="card tile" to={option.path}>
              <Icon name={option.icon} />
              <strong>{option.label}</strong>
              <span className="muted">{option.hint}</span>
            </Link>
          </li>
        ))}
      </ul>
    </>
  );
}
