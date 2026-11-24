import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { addToCart, getProduct } from "../../api/buyer.js";
import Alert from "../../components/Alert.jsx";
import LoadState from "../../components/LoadState.jsx";
import ProductMark from "../../components/ProductMark.jsx";
import { money } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";

const MAX_UNITS = 1000;

function ProductView({ productId }) {
  const { data: product, loading, error, reload } = useLoad(useCallback(() => getProduct(productId), [productId]));
  const [quantity, setQuantity] = useState("1");
  const [result, setResult] = useState(null);
  const [sending, setSending] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    const units = Number(quantity);
    if (!Number.isInteger(units) || units < 1 || units > MAX_UNITS) {
      setResult({ error: `Escribe una cantidad entre 1 y ${MAX_UNITS}.` });
      return;
    }
    setSending(true);
    setResult(null);
    try {
      const cart = await addToCart(productId, units);
      const inCart = cart.items.find((item) => item.productId === productId)?.quantity;
      setResult({ inCart });
      setQuantity("1");
    } catch (failure) {
      setResult({ error: failure.message });
    } finally {
      setSending(false);
    }
  }

  return (
    <>
      <p><Link to="/comprador/catalogo">← Catálogo</Link></p>
      <LoadState loading={loading && !product} error={error} onRetry={reload} />
      {product && (
        <article className="card product-detail">
          <ProductMark product={product} />
          <div>
            <h1>{product.name}</h1>
            <p className="price">{money(product.price)}</p>
            <p>{product.description}</p>
            {product.variants.length > 0 && (
              <dl className="pairs">
                {product.variants.map(({ name, value }) => (
                  <div key={`${name}-${value}`}><dt>{name}</dt><dd>{value}</dd></div>
                ))}
              </dl>
            )}
            <p className="muted">
              {product.digital ? "Producto digital: se entrega al confirmar el pago." : "Producto físico: se envía a tu dirección."}
            </p>
            <form className="add" onSubmit={handleSubmit} noValidate>
              <div>
                <label htmlFor="quantity">Cantidad</label>
                <input id="quantity" type="number" min="1" max={MAX_UNITS} value={quantity}
                       onChange={(event) => setQuantity(event.target.value)} />
              </div>
              <button type="submit" className="primary" disabled={sending}>
                {sending ? "Agregando…" : "Agregar al carrito"}
              </button>
            </form>
            <Alert type="error">{result?.error}</Alert>
            {result?.inCart && (
              <Alert>
                Agregado. Tienes {result.inCart} en tu carrito. <Link to="/comprador/carrito">Ver carrito</Link>
              </Alert>
            )}
          </div>
        </article>
      )}
    </>
  );
}

// The key starts each product from scratch (quantity and messages).
export default function ProductPage() {
  const { productId } = useParams();
  return <ProductView key={productId} productId={productId} />;
}
