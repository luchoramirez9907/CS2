import AdminOrderPage from "./pages/admin/AdminOrderPage.jsx";
import AdminShipmentsPage from "./pages/admin/AdminShipmentsPage.jsx";
import CartPage from "./pages/buyer/CartPage.jsx";
import CatalogPage from "./pages/buyer/CatalogPage.jsx";
import OrderPage from "./pages/buyer/OrderPage.jsx";
import OrdersPage from "./pages/buyer/OrdersPage.jsx";
import ProductPage from "./pages/buyer/ProductPage.jsx";
import AssignedOrderPage from "./pages/operator/AssignedOrderPage.jsx";
import AssignedOrdersPage from "./pages/operator/AssignedOrdersPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import CommercialReportPage from "./pages/reports/CommercialReportPage.jsx";
import MyReportPage from "./pages/reports/MyReportPage.jsx";
import ReturnPage from "./pages/returns/ReturnPage.jsx";
import ReturnsPage from "./pages/returns/ReturnsPage.jsx";
import MyProductPage from "./pages/seller/MyProductPage.jsx";
import NewProductPage from "./pages/seller/NewProductPage.jsx";
import ProductsPage from "./pages/seller/ProductsPage.jsx";
import SalesPage from "./pages/seller/SalesPage.jsx";
import AllOrdersPage from "./pages/staff/AllOrdersPage.jsx";
import NewUserPage from "./pages/staff/NewUserPage.jsx";
import UserPage from "./pages/staff/UserPage.jsx";
import UsersPage from "./pages/staff/UsersPage.jsx";
import InventoryPage from "./pages/stock/InventoryPage.jsx";
import WarehouseFormPage from "./pages/stock/WarehouseFormPage.jsx";
import WarehousesPage from "./pages/stock/WarehousesPage.jsx";
import ReadOnlyOrder from "./components/ReadOnlyOrder.jsx";

const buyerReturns = { role: "buyer", base: "/comprador/devoluciones" };
const sellerReturns = { role: "seller", base: "/vendedor/devoluciones" };
const adminReturns = { role: "admin", base: "/admin/devoluciones" };
const supervisorReturns = { role: "supervisor", base: "/supervisor/devoluciones" };
const sellerWarehouses = { role: "seller", base: "/vendedor/bodegas" };
const adminWarehouses = { role: "admin", base: "/admin/bodegas" };
const adminUsers = { staff: "admin", base: "/admin/usuarios" };
const supervisorUsers = { staff: "supervisor", base: "/supervisor/usuarios" };

/** Pages of each role: the ones of its menu (see auth/roles.js) and the ones they open. */
export const PAGES = {
  BUYER: {
    "/comprador/catalogo": <CatalogPage />,
    "/comprador/catalogo/:productId": <ProductPage />,
    "/comprador/carrito": <CartPage />,
    "/comprador/pedidos": <OrdersPage />,
    "/comprador/pedidos/:orderId": <OrderPage />,
    "/comprador/devoluciones": <ReturnsPage {...buyerReturns} />,
    "/comprador/devoluciones/:returnId": <ReturnPage {...buyerReturns} orders="/comprador/pedidos" />,
    "/comprador/perfil": <ProfilePage role="buyer" />
  },
  SELLER: {
    "/vendedor/productos": <ProductsPage />,
    "/vendedor/productos/nuevo": <NewProductPage />,
    "/vendedor/productos/:productId": <MyProductPage />,
    "/vendedor/bodegas": <WarehousesPage {...sellerWarehouses} inventory="/vendedor/inventario" />,
    "/vendedor/bodegas/nueva": <WarehouseFormPage {...sellerWarehouses} />,
    "/vendedor/bodegas/:warehouseId": <WarehouseFormPage {...sellerWarehouses} />,
    "/vendedor/inventario": <InventoryPage role="seller" />,
    "/vendedor/ventas": <SalesPage />,
    "/vendedor/ventas/:orderId": <ReadOnlyOrder role="seller" base="/vendedor/ventas" backLabel="Ventas" />,
    "/vendedor/devoluciones": <ReturnsPage {...sellerReturns} />,
    "/vendedor/devoluciones/:returnId": <ReturnPage {...sellerReturns} orders="/vendedor/ventas" />,
    "/vendedor/reporte": <MyReportPage />,
    "/vendedor/perfil": <ProfilePage role="seller" />
  },
  LOGISTICS_OPERATOR: {
    "/operador/envios": <AssignedOrdersPage />,
    "/operador/envios/:orderId": <AssignedOrderPage />,
    "/operador/inventario": <InventoryPage role="operator" />,
    "/operador/perfil": <ProfilePage role="operator" />
  },
  ADMINISTRATOR: {
    "/admin/usuarios": <UsersPage {...adminUsers} />,
    "/admin/usuarios/nuevo": <NewUserPage base={adminUsers.base} />,
    "/admin/usuarios/:userId": <UserPage {...adminUsers} />,
    "/admin/pedidos": <AllOrdersPage staff="admin" base="/admin/pedidos" title="Pedidos y pagos" />,
    "/admin/pedidos/:orderId": <AdminOrderPage />,
    "/admin/envios": <AdminShipmentsPage />,
    "/admin/devoluciones": <ReturnsPage {...adminReturns} />,
    "/admin/devoluciones/:returnId": <ReturnPage {...adminReturns} orders="/admin/pedidos" />,
    "/admin/bodegas": <WarehousesPage {...adminWarehouses} inventory="/admin/bodegas/inventario" />,
    "/admin/bodegas/nueva": <WarehouseFormPage {...adminWarehouses} />,
    "/admin/bodegas/inventario": <InventoryPage role="admin" />,
    "/admin/bodegas/:warehouseId": <WarehouseFormPage {...adminWarehouses} />,
    "/admin/reporte": <CommercialReportPage staff="admin" />,
    "/admin/perfil": <ProfilePage role="admin" />
  },
  SUPERVISOR: {
    "/supervisor/usuarios": <UsersPage {...supervisorUsers} />,
    "/supervisor/usuarios/:userId": <UserPage {...supervisorUsers} />,
    "/supervisor/pedidos": <AllOrdersPage staff="supervisor" base="/supervisor/pedidos" title="Pedidos" />,
    "/supervisor/pedidos/:orderId": <ReadOnlyOrder role="supervisor" base="/supervisor/pedidos" backLabel="Pedidos" />,
    "/supervisor/devoluciones": <ReturnsPage {...supervisorReturns} />,
    "/supervisor/devoluciones/:returnId": <ReturnPage {...supervisorReturns} orders="/supervisor/pedidos" />,
    "/supervisor/inventario": <InventoryPage role="supervisor" readOnly />,
    "/supervisor/reporte": <CommercialReportPage staff="supervisor" />,
    "/supervisor/perfil": <ProfilePage role="supervisor" />
  }
};
