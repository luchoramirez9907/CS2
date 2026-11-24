import { useCallback } from "react";
import { Link } from "react-router-dom";
import { listUsers } from "../../api/people.js";
import { listWarehouses } from "../../api/stock.js";
import LoadState from "../../components/LoadState.jsx";
import { address } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

/** Warehouses of the seller, or every warehouse for the administrator. */
export default function WarehousesPage({ role, base, inventory }) {
  const { data, loading, error, reload } = useLoad(useCallback(async () => {
    const [warehouses, sellers] = await Promise.all([
      listWarehouses(role),
      role === "admin" ? listUsers("admin", "SELLER") : []
    ]);
    return { warehouses, owners: new Map(sellers.map((seller) => [seller.identifier, seller.fullName])) };
  }, [role]));

  function owner({ ownership, ownerId }) {
    return ownership === "MARKETPLACE" ? "Marketplace" : data.owners.get(ownerId) ?? "Vendedor";
  }

  return (
    <>
      <div className="heading">
        <h1>Bodegas</h1>
        <Link className="primary" to={`${base}/nueva`}>Registrar bodega</Link>
      </div>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data?.warehouses.length === 0 && <p>Todavía no tienes bodegas.</p>}
      {data?.warehouses.length > 0 && (
        <ul className="card rows">
          {data.warehouses.map((warehouse) => (
            <li key={warehouse.identifier} className="row">
              <span className="row-main">
                <strong>{warehouse.name}</strong>
                <span className="muted">{warehouse.identifier} · {address(warehouse.address)}</span>
              </span>
              {role === "admin" && <span className="badge">{owner(warehouse)}</span>}
              <Link to={`${inventory}?bodega=${encodeURIComponent(warehouse.identifier)}`}
                    aria-label={`Inventario de ${warehouse.name}`}>Inventario</Link>
              <Link to={`${base}/${encodeURIComponent(warehouse.identifier)}`}
                    aria-label={`Editar ${warehouse.name}`}>Editar</Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
