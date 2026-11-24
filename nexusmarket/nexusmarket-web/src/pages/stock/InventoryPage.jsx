import { useCallback, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { searchCatalog } from "../../api/buyer.js";
import { listMyProducts } from "../../api/seller.js";
import { listWarehouses, loadStock, registerMovement } from "../../api/stock.js";
import Alert from "../../components/Alert.jsx";
import FieldList from "../../components/FieldList.jsx";
import LoadState from "../../components/LoadState.jsx";
import { date, MOVEMENT_TYPE } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useForm } from "../../utils/useForm.js";
import { useLoad } from "../../utils/useLoad.js";

const OTHER = "__other__";
const MAX_UNITS = 100000;

/** Select with an "other" option to type an identifier that the API does not list. */
function Picker({ id, label, otherLabel, options, value, onChange }) {
  const listed = options.some(([optionValue]) => optionValue === value);
  const [typing, setTyping] = useState(Boolean(value) && !listed);
  const [text, setText] = useState(typing ? value : "");

  function handleSelect({ target }) {
    setTyping(target.value === OTHER);
    setText("");
    onChange(target.value === OTHER ? "" : target.value);
  }

  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <select id={id} value={typing ? OTHER : value} onChange={handleSelect}>
        <option value="">Elige una opción</option>
        {options.map(([optionValue, optionLabel]) => (
          <option key={optionValue} value={optionValue}>{optionLabel}</option>
        ))}
        <option value={OTHER}>{otherLabel}</option>
      </select>
      {typing && (
        <form className="search compact" onSubmit={(event) => { event.preventDefault(); onChange(text.trim()); }}>
          <input aria-label={`Código: ${label.toLowerCase()}`} placeholder="Código" maxLength={64} value={text}
                 autoFocus={!value} onChange={(event) => setText(event.target.value)} />
          <button type="submit" className="secondary">Ver</button>
        </form>
      )}
    </div>
  );
}

const TYPES = {
  STOCK_IN: { label: "Entrada", hint: "Unidades que llegan a la bodega." },
  ADJUSTMENT: { label: "Ajuste", hint: "Con signo: -2 si faltan unidades, 5 si sobran." },
  DAMAGE: { label: "Daño", hint: "Unidades que ya no se pueden vender." }
};

function MovementForm({ hasStock, busy, onSave }) {
  const [type, setType] = useState("STOCK_IN");
  const signed = type === "ADJUSTMENT";
  // Without a record in the warehouse only an entry makes sense: it creates it.
  const options = Object.entries(TYPES).filter(([code]) => hasStock || code === "STOCK_IN");
  const fields = [{
    name: "quantity", label: "Unidades", type: "number", hint: TYPES[type].hint,
    rule: (value) => {
      const units = Number(value);
      const valid = Number.isInteger(units) && Math.abs(units) <= MAX_UNITS && (signed ? units !== 0 : units > 0);
      if (valid) return "";
      return signed ? "Escribe un número entero distinto de 0." : `Escribe un número entero entre 1 y ${MAX_UNITS}.`;
    }
  }];
  const form = useForm(fields);

  function handleSubmit(event) {
    event.preventDefault();
    if (form.check(event.currentTarget, fields)) onSave(type, Number(form.text("quantity")));
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <h2>Registrar movimiento</h2>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="type">Tipo</label>
          <select id="type" value={type} onChange={(event) => setType(event.target.value)}>
            {options.map(([code, { label }]) => <option key={code} value={code}>{label}</option>)}
          </select>
        </div>
      </div>
      <FieldList fields={fields} form={form} />
      <button type="submit" className="primary" disabled={busy}>{busy ? "Guardando…" : "Registrar"}</button>
    </form>
  );
}

function Stock({ role, readOnly, productId, warehouseId }) {
  const { data, loading, error, reload } = useLoad(
    useCallback(() => loadStock(role, productId, warehouseId), [role, productId, warehouseId]));
  const { busy, failure, run } = useAction();
  const [done, setDone] = useState("");

  async function handleSave(movementType, quantity) {
    setDone("");
    const problem = await run(() => registerMovement(role, { productId, warehouseId, movementType, quantity }));
    if (!problem) setDone(`Movimiento registrado: ${TYPES[movementType].label.toLowerCase()} de ${quantity}.`);
    if (!problem || problem.status) await reload();
  }

  if (!data) return <LoadState loading={loading} error={error} onRetry={reload} />;
  const { stock, movements } = data;
  return (
    <>
      {stock ? (
        <dl className="figures">
          <div><dt>Disponibles</dt><dd>{stock.availableQuantity}</dd></div>
          <div><dt>Reservadas</dt><dd>{stock.reservedQuantity}</dd></div>
          <div><dt>Dañadas</dt><dd>{stock.damagedQuantity}</dd></div>
        </dl>
      ) : (
        <p>
          Este producto todavía no tiene unidades en esta bodega.{!readOnly && " Registra una entrada para empezar."}
        </p>
      )}
      <Alert type="error">{failure}</Alert>
      <Alert>{done}</Alert>
      {!readOnly && <MovementForm key={movements.length} hasStock={Boolean(stock)} busy={busy} onSave={handleSave} />}
      {movements.length > 0 && (
        <section aria-label="Historial">
          <h2>Historial</h2>
          <ul className="card rows">
            {[...movements].reverse().map((movement) => (
              <li key={movement.movementNumber} className="row">
                <span className="row-main">
                  <strong>{MOVEMENT_TYPE[movement.movementType] ?? movement.movementType}</strong>
                  <span className="muted">{date(movement.movementDate)} · {movement.performedById}</span>
                </span>
                <span className="row-total">{movement.quantity}</span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  );
}

/** Stock of a product in a warehouse, for the seller, the operator and the staff. */
export default function InventoryPage({ role, readOnly = false }) {
  const [params, setParams] = useSearchParams();
  const productId = params.get("producto") ?? "";
  const warehouseId = params.get("bodega") ?? "";
  const { data, loading, error, reload } = useLoad(useCallback(async () => {
    const [products, warehouses] = await Promise.all([
      role === "seller" ? listMyProducts() : searchCatalog(""),
      listWarehouses(role)
    ]);
    return {
      products: products.filter((product) => !product.digital).map(({ identifier, name }) => [identifier, name]),
      warehouses: warehouses.map(({ identifier, name }) => [identifier, name])
    };
  }, [role]));

  function choose(name, value) {
    const next = new URLSearchParams(params);
    if (value) next.set(name, value);
    else next.delete(name);
    setParams(next);
  }

  return (
    <>
      <h1>Inventario</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <>
          <div className="form-grid pickers">
            <Picker id="product" label="Producto" otherLabel="Otro producto…" options={data.products} value={productId}
                    onChange={(value) => choose("producto", value)} />
            <Picker id="warehouse" label="Bodega" otherLabel="Otra bodega…" options={data.warehouses} value={warehouseId}
                    onChange={(value) => choose("bodega", value)} />
          </div>
          {productId && warehouseId
            ? <Stock key={`${productId}|${warehouseId}`} role={role} readOnly={readOnly} productId={productId}
                     warehouseId={warehouseId} />
            : <p className="muted">Elige un producto y una bodega.</p>}
        </>
      )}
    </>
  );
}
