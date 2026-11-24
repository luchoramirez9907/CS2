import { useCallback, useState } from "react";
import { listUsers } from "../../api/people.js";
import { getCommercialReport } from "../../api/reports.js";
import LoadState from "../../components/LoadState.jsx";
import { date, money, ORDER_STATUS } from "../../utils/format.js";
import { useLoad } from "../../utils/useLoad.js";
import Counts from "./Counts.jsx";
import SellerReport from "./SellerReport.jsx";

function Inventory({ levels }) {
  if (levels.length === 0) return null;
  return (
    <section aria-label="Inventario">
      <h2>Inventario</h2>
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Producto</th><th>Bodega</th>
              <th className="num">Disponibles</th><th className="num">Reservadas</th><th className="num">Dañadas</th>
            </tr>
          </thead>
          <tbody>
            {levels.map((level) => (
              <tr key={`${level.productId}|${level.warehouseId}`}>
                <td>{level.productName}</td><td>{level.warehouseName}</td>
                <td className="num">{level.availableQuantity}</td>
                <td className="num">{level.reservedQuantity}</td>
                <td className="num">{level.damagedQuantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function Performance({ staff }) {
  const { data, loading, error, reload } = useLoad(useCallback(() => listUsers(staff, "SELLER"), [staff]));
  const [seller, setSeller] = useState("");
  return (
    <section aria-label="Desempeño por vendedor">
      <h2>Desempeño por vendedor</h2>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <div className="filter">
          <label htmlFor="seller">Vendedor</label>
          <select id="seller" value={seller} onChange={(event) => setSeller(event.target.value)}>
            <option value="">Elige un vendedor</option>
            {data.map(({ identifier, fullName }) => <option key={identifier} value={identifier}>{fullName}</option>)}
          </select>
        </div>
      )}
      {seller && <SellerReport key={seller} staff={staff} sellerId={seller} />}
    </section>
  );
}

export default function CommercialReportPage({ staff }) {
  const { data, loading, error, reload } = useLoad(useCallback(() => getCommercialReport(staff), [staff]));
  return (
    <>
      <h1>Reporte comercial</h1>
      <LoadState loading={loading && !data} error={error} onRetry={reload} />
      {data && (
        <>
          <p className="muted">{date(data.generatedAt)}</p>
          <dl className="figures">
            <div><dt>Ingresos</dt><dd>{money(data.grossRevenue)}</dd></div>
            <div><dt>Reembolsado</dt><dd>{money(data.refundedAmount)}</dd></div>
            <div><dt>Ingreso neto</dt><dd>{money(data.netRevenue)}</dd></div>
            <div><dt>Pedidos</dt><dd>{data.totalOrders}</dd></div>
          </dl>
          <p className="muted">
            Facturas: {data.invoicesIssued}, por {money(data.invoicedAmount)} (IVA {money(data.invoicedTaxAmount)}).
            Los ingresos solo cuentan pedidos pagados.
          </p>
          <Counts title="Pedidos por estado" counts={data.ordersByStatus} labels={ORDER_STATUS} />
          <Inventory levels={data.inventoryLevels} />
          <Performance staff={staff} />
        </>
      )}
    </>
  );
}
