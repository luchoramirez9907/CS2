// Menu of each role. The API is who really enforces the permissions (403);
// hiding options here only keeps the screen clean.
export const ROLES = {
  BUYER: {
    label: "Comprador",
    sessionPath: () => "/buyer/profile",
    menu: [
      { path: "/comprador/catalogo", label: "Catálogo", icon: "search", hint: "Busca productos" },
      { path: "/comprador/carrito", label: "Carrito", icon: "cart", hint: "Revisa tu compra" },
      { path: "/comprador/pedidos", label: "Pedidos", icon: "box", hint: "Estado y rastreo" },
      { path: "/comprador/devoluciones", label: "Devoluciones", icon: "undo", hint: "Solicitudes y reembolsos" },
      { path: "/comprador/perfil", label: "Perfil", icon: "user", hint: "Tus datos" }
    ]
  },
  SELLER: {
    label: "Vendedor",
    sessionPath: () => "/seller/profile",
    menu: [
      { path: "/vendedor/productos", label: "Productos", icon: "tag", hint: "Publica y edita" },
      { path: "/vendedor/bodegas", label: "Bodegas", icon: "warehouse", hint: "Dónde guardas tu stock" },
      { path: "/vendedor/inventario", label: "Inventario", icon: "layers", hint: "Entradas, ajustes y daños" },
      { path: "/vendedor/ventas", label: "Ventas", icon: "box", hint: "Pedidos con tus productos" },
      { path: "/vendedor/devoluciones", label: "Devoluciones", icon: "undo", hint: "De tus productos" },
      { path: "/vendedor/reporte", label: "Reporte", icon: "chart", hint: "Tu desempeño" },
      { path: "/vendedor/perfil", label: "Perfil", icon: "user", hint: "Tus datos" }
    ]
  },
  LOGISTICS_OPERATOR: {
    label: "Operador logístico",
    sessionPath: () => "/operator/profile",
    menu: [
      { path: "/operador/envios", label: "Envíos asignados", icon: "truck", hint: "Despacha y entrega" },
      { path: "/operador/inventario", label: "Inventario", icon: "layers", hint: "Stock de las bodegas" },
      { path: "/operador/perfil", label: "Perfil", icon: "user", hint: "Tus datos" }
    ]
  },
  ADMINISTRATOR: {
    label: "Administrador",
    sessionPath: (userId) => `/admin/users/${encodeURIComponent(userId)}`,
    menu: [
      { path: "/admin/usuarios", label: "Usuarios", icon: "users", hint: "Crea y bloquea cuentas" },
      { path: "/admin/pedidos", label: "Pedidos y pagos", icon: "card", hint: "Confirma pagos" },
      { path: "/admin/envios", label: "Envíos", icon: "truck", hint: "Asigna operadores" },
      { path: "/admin/devoluciones", label: "Devoluciones", icon: "undo", hint: "Decide y reembolsa" },
      { path: "/admin/bodegas", label: "Bodegas", icon: "warehouse", hint: "Bodegas e inventario" },
      { path: "/admin/reporte", label: "Reporte", icon: "chart", hint: "Ventas e inventario" },
      { path: "/admin/perfil", label: "Perfil", icon: "user", hint: "Tus datos" }
    ]
  },
  SUPERVISOR: {
    label: "Supervisor",
    sessionPath: (userId) => `/supervisor/users/${encodeURIComponent(userId)}`,
    menu: [
      { path: "/supervisor/usuarios", label: "Usuarios", icon: "users", hint: "Consulta cuentas" },
      { path: "/supervisor/pedidos", label: "Pedidos", icon: "box", hint: "Consulta ventas" },
      { path: "/supervisor/devoluciones", label: "Devoluciones", icon: "undo", hint: "Consulta solicitudes" },
      { path: "/supervisor/inventario", label: "Inventario", icon: "layers", hint: "Stock de las bodegas" },
      { path: "/supervisor/reporte", label: "Reporte", icon: "chart", hint: "Ventas e inventario" },
      { path: "/supervisor/perfil", label: "Perfil", icon: "user", hint: "Tu cuenta" }
    ]
  }
};
