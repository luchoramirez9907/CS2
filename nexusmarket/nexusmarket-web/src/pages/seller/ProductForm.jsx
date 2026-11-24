import { useState } from "react";
import Alert from "../../components/Alert.jsx";
import FieldList from "../../components/FieldList.jsx";
import { noSpaces } from "../../utils/fields.js";
import { useForm } from "../../utils/useForm.js";

const IDENTIFIER = { name: "identifier", label: "Código del producto", max: 64, rule: noSpaces,
  hint: "No se puede cambiar después." };
const KIND = { name: "kind", label: "Tipo", type: "select", options: [["physical", "Físico"], ["digital", "Digital"]] };
const DELIVERY = { name: "digitalDeliveryDetails", label: "Cómo se entrega", max: 500, wide: true,
  hint: "Por ejemplo, un enlace de descarga." };
const COMMON = [
  { name: "name", label: "Nombre", max: 160 },
  { name: "price", label: "Precio en pesos, con IVA", type: "number", rule: priceRule },
  { name: "description", label: "Descripción", type: "textarea", max: 1000, wide: true, optional: true }
];
const MAX_PRICE = 9999999999999.99;

function priceRule(value) {
  const price = Number(value);
  if (!(price >= 0 && price <= MAX_PRICE)) return "Escribe un precio de 0 o más.";
  return /^\d+([.]\d{1,2})?$/.test(value.trim()) ? "" : "Usa máximo dos decimales.";
}

function Variants({ variants, onChange }) {
  const edit = (index, change) => onChange(variants.map((item, i) => (i === index ? { ...item, ...change } : item)));
  return (
    <fieldset>
      <legend>Características</legend>
      {variants.map((variant, index) => (
        <div key={variant.key} className="variant">
          <input aria-label={`Característica ${index + 1}`} placeholder="Color" maxLength={60} value={variant.name}
                 onChange={(event) => edit(index, { name: event.target.value })} />
          <input aria-label={`Valor ${index + 1}`} placeholder="Negro" maxLength={120} value={variant.value}
                 onChange={(event) => edit(index, { value: event.target.value })} />
          <button type="button" className="link-button" aria-label={`Quitar característica ${index + 1}`}
                  onClick={() => onChange(variants.filter((_, i) => i !== index))}>Quitar</button>
        </div>
      ))}
      <button type="button" className="secondary"
              onClick={() => onChange([...variants, { key: crypto.randomUUID(), name: "", value: "" }])}>
        Agregar característica
      </button>
    </fieldset>
  );
}

/** Form to publish (no `product`) or edit a product. `onSave` receives the body of the API. */
export default function ProductForm({ product, busy, onSave }) {
  const form = useForm([IDENTIFIER, KIND, ...COMMON, DELIVERY], product);
  const [variants, setVariants] = useState(() =>
    (product?.variants ?? []).map((variant, index) => ({ ...variant, key: index })));
  const [error, setError] = useState("");
  const digital = form.values.kind === "digital";
  const fields = product ? COMMON : [IDENTIFIER, KIND, ...COMMON, ...(digital ? [DELIVERY] : [])];

  function handleSubmit(event) {
    event.preventDefault();
    setError("");
    if (!form.check(event.currentTarget, fields)) return;
    // A row left empty is ignored; half a row is a mistake.
    const filled = variants
      .map(({ name, value }) => ({ name: name.trim(), value: value.trim() }))
      .filter(({ name, value }) => name || value);
    if (filled.some(({ name, value }) => !name || !value)) {
      setError("Cada característica necesita nombre y valor.");
      return;
    }
    const { text } = form;
    onSave({
      name: text("name"),
      price: Number(text("price")),
      description: text("description"),
      variants: filled,
      ...(!product && { identifier: text("identifier"), digital }),
      ...(!product && digital && { digitalDeliveryDetails: text("digitalDeliveryDetails") })
    });
  }

  return (
    <form className="card panel" onSubmit={handleSubmit} noValidate>
      <FieldList fields={fields} form={form} />
      <Variants variants={variants} onChange={setVariants} />
      <Alert type="error">{error}</Alert>
      <button type="submit" className="primary" disabled={busy}>
        {busy ? "Guardando…" : product ? "Guardar cambios" : "Publicar"}
      </button>
    </form>
  );
}
