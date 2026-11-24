import { Link } from "react-router-dom";
import { listMyProducts } from "../../api/seller.js";
import LoadState from "../../components/LoadState.jsx";
import { money, PRODUCT_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

export default function ProductsPage() {
  const { data, loading, error, reload } = useLoad(listMyProducts);
  return (
    <>
      <div className="heading">
        <h1>Productos</h1>
        <Link className="primary" to="/vendedor/productos/nuevo">Publicar producto</Link>
      </div>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data?.length === 0 && <p>Todavía no has publicado productos.</p>}
      {data?.length > 0 && (
        <ul className="card rows">
          {data.map((product) => (
            <li key={product.identifier}>
              <Link className="row row-link" to={`/vendedor/productos/${encodeURIComponent(product.identifier)}`}>
                <span className="row-main">
                  <strong>{product.name}</strong>
                  <span className="muted">{product.identifier} · {product.digital ? "Digital" : "Físico"}</span>
                </span>
                <span className="badge">{PRODUCT_STATUS[product.status] ?? product.status}</span>
                <span className="row-total">{money(product.price)}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
