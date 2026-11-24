import { useCallback, useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { searchCatalog } from "../../api/buyer.js";
import LoadState from "../../components/LoadState.jsx";
import ProductMark from "../../components/ProductMark.jsx";
import { money } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

export default function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const query = params.get("q") ?? "";
  const [text, setText] = useState(query);
  const { data, loading, error, reload } = useLoad(useCallback(() => searchCatalog(query), [query]));

  useEffect(() => setText(query), [query]);

  function handleSubmit(event) {
    event.preventDefault();
    setParams(text.trim() ? { q: text.trim() } : {});
  }

  return (
    <>
      <h1>Catálogo</h1>
      <form className="search" role="search" onSubmit={handleSubmit}>
        <label className="sr-only" htmlFor="q">Buscar productos</label>
        <input id="q" type="search" placeholder="Teclado" value={text}
               onChange={(event) => setText(event.target.value)} />
        <button type="submit" className="primary">Buscar</button>
      </form>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data?.length === 0 && (
        <p>{query ? `No hay productos que coincidan con “${query}”.` : "Todavía no hay productos publicados."}</p>
      )}
      <ul className="products">
        {data?.map((product) => (
          <li key={product.identifier}>
            <Link className="card product" to={`/comprador/catalogo/${encodeURIComponent(product.identifier)}`}>
              <ProductMark product={product} />
              <strong>{product.name}</strong>
              <span className="muted clamp">{product.description}</span>
              <span className="price">{money(product.price)}</span>
              {product.digital && <span className="badge">Digital</span>}
            </Link>
          </li>
        ))}
      </ul>
    </>
  );
}
