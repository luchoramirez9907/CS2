import { useSearchParams } from "react-router-dom";

/** Filter kept in the address bar; a value that is not an option means "all". */
export function useFilter(name, options) {
  const [params, setParams] = useSearchParams();
  const requested = params.get(name) ?? "";
  const value = Object.hasOwn(options, requested) ? requested : "";
  return [value, (next) => setParams(next ? { [name]: next } : {})];
}

export default function Filter({ id, label, options, value, onChange }) {
  return (
    <div className="filter">
      <label htmlFor={id}>{label}</label>
      <select id={id} value={value} onChange={(event) => onChange(event.target.value)}>
        <option value="">Todos</option>
        {Object.entries(options).map(([code, text]) => <option key={code} value={code}>{text}</option>)}
      </select>
    </div>
  );
}
