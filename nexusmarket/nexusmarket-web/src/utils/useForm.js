import { useCallback, useState } from "react";

function findError({ label, type, optional, missing, rule }, value) {
  if (!value.trim()) {
    if (optional) return "";
    return missing ?? `${type === "select" ? "Elige" : "Escribe"}: ${label.toLowerCase()}.`;
  }
  return rule?.(value) ?? "";
}

/** Values and errors of a form described by a list of fields (see FieldList). */
export function useForm(fields, initial) {
  const [values, setValues] = useState(() =>
    Object.fromEntries(fields.map(({ name }) => [name, String(initial?.[name] ?? "")])));
  const [errors, setErrors] = useState({});

  const onChange = useCallback(({ target }) => {
    setValues((previous) => ({ ...previous, [target.name]: target.value }));
    setErrors((previous) => ({ ...previous, [target.name]: "" }));
  }, []);

  /** Checks the given fields, shows their errors and focuses the first one; returns whether all are valid. */
  function check(form, active = fields) {
    const found = Object.fromEntries(active.map((field) => [field.name, findError(field, values[field.name])]));
    setErrors(found);
    const first = active.find(({ name }) => found[name]);
    if (first) form.elements[first.name].focus();
    return !first;
  }

  const text = (name) => values[name].trim();
  return { values, errors, onChange, check, text };
}
