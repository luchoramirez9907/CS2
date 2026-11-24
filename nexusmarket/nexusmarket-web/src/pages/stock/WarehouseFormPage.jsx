import { useCallback } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { listUsers } from "../../api/people.js";
import { getWarehouse, registerWarehouse, updateWarehouse } from "../../api/stock.js";
import Alert from "../../components/Alert.jsx";
import FieldList from "../../components/FieldList.jsx";
import LoadState from "../../components/LoadState.jsx";
import { ADDRESS_FIELDS, noSpaces, toAddress } from "../../utils/fields.js";
import { useAction } from "../../utils/useAction.js";
import { useForm } from "../../utils/useForm.js";
import { useLoad } from "../../utils/useLoad.js";

const IDENTIFIER = { name: "identifier", label: "Código de la bodega", max: 64, rule: noSpaces,
  hint: "No se puede cambiar después." };
const NAME = { name: "name", label: "Nombre", max: 160 };

function WarehouseForm({ role, base, warehouse, sellers }) {
  const navigate = useNavigate();
  const { busy, failure, run } = useAction();
  const owner = { name: "ownerSellerId", label: "Dueño", type: "select", optional: true, placeholder: "Marketplace",
    options: sellers.map(({ identifier, fullName }) => [identifier, fullName]) };
  const fields = warehouse
    ? [NAME, ...ADDRESS_FIELDS]
    : [IDENTIFIER, NAME, ...(role === "admin" ? [owner] : []), ...ADDRESS_FIELDS];
  const form = useForm(fields, warehouse && { name: warehouse.name, ...warehouse.address });

  function handleSubmit(event) {
    event.preventDefault();
    if (!form.check(event.currentTarget)) return;
    const { text } = form;
    const body = { name: text("name"), address: toAddress(text) };
    run(async () => {
      if (warehouse) {
        await updateWarehouse(role, warehouse.identifier, body);
      } else {
        const ownerSellerId = role === "admin" && text("ownerSellerId");
        await registerWarehouse(role, { ...body, identifier: text("identifier"), ...(ownerSellerId && { ownerSellerId }) });
      }
      navigate(base);
    });
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <FieldList fields={fields} form={form} />
      <Alert type="error">{failure}</Alert>
      <button type="submit" className="primary" disabled={busy}>{busy ? "Guardando…" : "Guardar"}</button>
    </form>
  );
}

/** Registers a warehouse, or edits the one in the route. */
export default function WarehouseFormPage({ role, base }) {
  const { warehouseId } = useParams();
  const { data, loading, error, reload } = useLoad(useCallback(async () => {
    const [warehouse, sellers] = await Promise.all([
      warehouseId ? getWarehouse(role, warehouseId) : null,
      role === "admin" && !warehouseId ? listUsers("admin", "SELLER") : []
    ]);
    return { warehouse, sellers: sellers.filter((seller) => seller.status === "ACTIVE") };
  }, [role, warehouseId]));

  return (
    <>
      <p><Link to={base}>← Bodegas</Link></p>
      <h1>{warehouseId ? "Editar bodega" : "Registrar bodega"}</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && <WarehouseForm key={warehouseId ?? "new"} role={role} base={base} {...data} />}
    </>
  );
}
