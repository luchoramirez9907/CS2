/** The API has no product images: each product gets its initial on a stable color. */
export default function ProductMark({ product }) {
  let hue = 0;
  for (const letter of product.identifier) hue = (hue * 31 + letter.charCodeAt(0)) % 360;
  return (
    <div className="mark" style={{ "--hue": hue }} aria-hidden="true">
      {product.name.charAt(0).toUpperCase()}
    </div>
  );
}
