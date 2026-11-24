import { Link, useNavigate } from "react-router-dom";
import { publishProduct } from "../../api/seller.js";
import Alert from "../../components/Alert.jsx";
import { useAction } from "../../utils/useAction.js";
import ProductForm from "./ProductForm.jsx";

export default function NewProductPage() {
  const navigate = useNavigate();
  const { busy, failure, run } = useAction();

  const handleSave = (product) => run(async () => {
    const { identifier } = await publishProduct(product);
    navigate(`/vendedor/productos/${encodeURIComponent(identifier)}`, { replace: true, state: { created: true } });
  });

  return (
    <>
      <p><Link to="/vendedor/productos">← Productos</Link></p>
      <h1>Publicar producto</h1>
      <Alert type="error">{failure}</Alert>
      <ProductForm busy={busy} onSave={handleSave} />
    </>
  );
}
