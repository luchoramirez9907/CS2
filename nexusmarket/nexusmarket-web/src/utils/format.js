const currency = (decimals) => new Intl.NumberFormat("es-CO", {
  style: "currency", currency: "COP", minimumFractionDigits: decimals, maximumFractionDigits: decimals
});
const WHOLE = currency(0);
const CENTS = currency(2);
const DATE = new Intl.DateTimeFormat("es-CO", { dateStyle: "medium", timeStyle: "short" });

export const money = (value) => (Number.isInteger(value) ? WHOLE : CENTS).format(value);
export const date = (value) => (value ? DATE.format(new Date(value)) : "—");
export const shortId = (value) => value.slice(0, 8);
export const address = (value) =>
  `${value.street}, ${value.city}, ${value.state}, ${value.country} (${value.postalCode})`;

export const ORDER_STATUS = {
  PENDING_PAYMENT: "Pendiente de pago",
  PAID: "Pagado",
  SHIPPED: "Enviado",
  DELIVERED: "Entregado"
};

export const SHIPMENT_STATUS = {
  PENDING: "En preparación",
  IN_TRANSIT: "En camino",
  RETURNED: "Volvió a la bodega",
  DELIVERED: "Entregado"
};

export const TRACKING_EVENT = {
  CREATED: "Envío creado",
  DISPATCHED: "Salió de la bodega",
  RETURNED: "No se pudo entregar",
  DELIVERED: "Entregado"
};

export const RETURN_STATUS = {
  REQUESTED: "Solicitada",
  APPROVED: "Aprobada",
  REJECTED: "Rechazada",
  COMPLETED: "Completada"
};

export const REFUND_STATUS = {
  PROCESSED: "Pagado",
  REJECTED: "Rechazado"
};

export const USER_STATUS = {
  ACTIVE: "Activa",
  INACTIVE: "Inactiva",
  BLOCKED: "Bloqueada"
};

export const COMMERCIAL_STATUS = {
  ACTIVE: "Puede comprar",
  RESTRICTED: "Restringido",
  BLOCKED: "Bloqueado"
};

export const PRODUCT_STATUS = {
  PUBLISHED: "Publicado",
  SUSPENDED: "Suspendido",
  DISCONTINUED: "Descontinuado"
};

export const MOVEMENT_TYPE = {
  STOCK_IN: "Entrada",
  ADJUSTMENT: "Ajuste",
  DAMAGE: "Daño",
  RESERVATION: "Reserva de un carrito",
  RESERVATION_RELEASE: "Reserva liberada",
  SALE_EXIT: "Salida por venta",
  RETURN: "Devolución"
};
