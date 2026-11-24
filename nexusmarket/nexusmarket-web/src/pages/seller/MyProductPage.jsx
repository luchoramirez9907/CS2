import { useCallback, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { changeProductStatus, getMyProduct, updateProduct } from "../../api/seller.js";
import Alert from "../../components/Alert.jsx";
import ConfirmButton from "../../components/ConfirmButton.jsx";
import LoadState from "../../components/LoadState.jsx";
import { money, PRODUCT_STATUS } from "../../utils/format.js";
import { useAction } from "../../utils/useAction.js";
import { useLoad } from "../../utils/useLoad.js";
import ProductForm from "./ProductForm.jsx";

function ProductView({ productId }) {
  const { state } = useLocation();
  const { data: product, loading, error, reload, setData } =
    useLoad(useCallback(() => getMyProduct(productId), [productId]));
  const { busy, failure, run } = useAction();
  const [done, setDone] = useState(state?.created ? "Producto publicado." : "");

  async function act(action, message) {
    setDone("");
    const problem = await run(async () => setData(await action()));
    if (!problem) setDone(message);
    else if (problem.status) await reload();
  }

  const setStatus = (status, message) => act(() => changeProductStatus(productId, status), message);
  const discontinued = product?.status === "DISCONTINUED";
  return (
    <>
      <p><Link to="/vendedor/productos">← Productos</Link></p>
      <LoadState loading={loading && !product} error={error} onRetry={reload} />
      {product && (
        <>
          <div className="heading">
            <div>
              <h1>{product.name}</h1>
              <p className="muted">
                {product.identifier} · {product.digital ? "Digital" : "Físico"} · {money(product.price)}
              </p>
            </div>
            <span className="badge">{PRODUCT_STATUS[product.status] ?? product.status}</span>
          </div>
          <Alert type="error">{failure}</Alert>
          <Alert>{done}</Alert>
          {!product.digital && (
            <p>
              <Link to={`/vendedor/inventario?producto=${encodeURIComponent(product.identifier)}`}>Ver su inventario</Link>
            </p>
          )}
          {discontinued ? (
            <p className="muted">Este producto está descontinuado: ya no se puede cambiar ni vender.</p>
          ) : (
            <>
              <ProductForm product={product} busy={busy}
                           onSave={(body) => act(() => updateProduct(productId, body), "Cambios guardados.")} />
              <div className="actions">
                {product.status === "PUBLISHED" ? (
                  <button type="button" className="secondary" disabled={busy}
                          onClick={() => setStatus("SUSPENDED", "Producto suspendido: ya no aparece en el catálogo.")}>
                    Suspender
                  </button>
                ) : (
                  <button type="button" className="secondary" disabled={busy}
                          onClick={() => setStatus("PUBLISHED", "Producto publicado de nuevo.")}>
                    Volver a publicar
                  </button>
                )}
                <ConfirmButton label="Descontinuar" className="secondary" disabled={busy}
                               question="Es permanente: no se podrá volver a vender ni editar."
                               confirmLabel="Sí, descontinuar"
                               onConfirm={() => setStatus("DISCONTINUED", "Producto descontinuado.")} />
              </div>
            </>
          )}
        </>
      )}
    </>
  );
}

// The key starts each product from scratch.
export default function MyProductPage() {
  const { productId } = useParams();
  return <ProductView key={productId} productId={productId} />;
}
