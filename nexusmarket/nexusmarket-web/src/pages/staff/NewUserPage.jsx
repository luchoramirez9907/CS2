import { Link, useNavigate } from "react-router-dom";
import { registerPerson } from "../../api/people.js";
import { ROLES } from "../../auth/roles.js";
import Alert from "../../components/Alert.jsx";
import FieldList from "../../components/FieldList.jsx";
import { ADDRESS_FIELDS, IDENTIFIER, PASSWORD, PERSON_FIELDS, toAddress } from "../../utils/fields.js";
import { useAction } from "../../utils/useAction.js";
import { useForm } from "../../utils/useForm.js";

const ROLE = { name: "role", label: "Rol", type: "select",
  options: Object.entries(ROLES).map(([code, { label }]) => [code, label]) };
const COMMON = [ROLE, IDENTIFIER, ...PERSON_FIELDS, PASSWORD];

export default function NewUserPage({ base }) {
  const navigate = useNavigate();
  const form = useForm([...COMMON, ...ADDRESS_FIELDS]);
  const { busy, failure, run } = useAction();
  const buyer = form.values.role === "BUYER";
  const fields = buyer ? [...COMMON, ...ADDRESS_FIELDS] : COMMON;

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.check(event.currentTarget, fields)) return;
    const { text, values } = form;
    run(async () => {
      const { identifier } = await registerPerson({
        role: values.role,
        identifier: text("identifier"),
        fullName: text("fullName"),
        email: text("email"),
        password: values.password,
        ...(buyer && { primaryAddress: toAddress(text) })
      });
      navigate(`${base}/${encodeURIComponent(identifier)}`, { replace: true, state: { created: true } });
    });
  }

  return (
    <>
      <p><Link to={base}>← Usuarios</Link></p>
      <h1>Crear usuario</h1>
      <form className="card panel" onSubmit={handleSubmit} noValidate>
        <FieldList fields={fields} form={form} />
        <Alert type="error">{failure}</Alert>
        <button type="submit" className="primary" disabled={busy}>{busy ? "Creando…" : "Crear usuario"}</button>
      </form>
    </>
  );
}
