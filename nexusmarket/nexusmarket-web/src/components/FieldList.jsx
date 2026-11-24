function Control({ field, ...props }) {
  const { type = "text", max, autoComplete, options, placeholder } = field;
  if (type === "select") {
    return (
      <select {...props}>
        <option value="">{placeholder ?? "Elige una opción"}</option>
        {options.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
      </select>
    );
  }
  if (type === "textarea") return <textarea rows="3" maxLength={max} {...props} />;
  return <input type={type} maxLength={max} autoComplete={autoComplete} placeholder={placeholder}
                step={type === "number" ? "any" : undefined} {...props} />;
}

/** Inputs of a form, each with its hint or its error. */
export default function FieldList({ fields, form: { values, errors, onChange } }) {
  return (
    <div className="form-grid">
      {fields.map((field) => {
        const { name, label, hint, wide, optional } = field;
        const error = errors[name];
        return (
          <div key={name} className={wide ? "field span" : "field"}>
            <label htmlFor={name}>{label}{optional && <span className="muted"> (opcional)</span>}</label>
            <Control field={field} id={name} name={name} value={values[name]} onChange={onChange}
                     aria-invalid={Boolean(error)} aria-describedby={error || hint ? `${name}-help` : undefined} />
            {(error || hint) && (
              <span id={`${name}-help`} className={error ? "field-error" : "field-hint"}>{error || hint}</span>
            )}
          </div>
        );
      })}
    </div>
  );
}
