const MESSAGES = {
  INVALID_CREDENTIALS: "El correo o la contraseña no son correctos.",
  AUTHENTICATION_REQUIRED: "Tu sesión terminó. Vuelve a ingresar.",
  FORBIDDEN: "No tienes permiso para hacer esta acción.",
  RESOURCE_NOT_FOUND: "No encontramos lo que buscas.",
  RESOURCE_ALREADY_EXISTS: "Ya existe un registro con esos datos.",
  INVALID_STATE_TRANSITION: "Esta acción no se puede hacer en el estado actual.",
  BUSINESS_RULE_VIOLATION: "La acción no cumple una regla del negocio.",
  INVALID_REQUEST: "Revisa los datos e intenta de nuevo.",
  DEPENDENCY_UNAVAILABLE: "Un servicio no está disponible. Intenta en unos minutos.",
  CONNECTION_FAILED: "No pudimos conectar con el servidor. Revisa que la API esté encendida."
};

const STATE_CHANGED = "Los datos cambiaron mientras los veías. Esto es lo actual.";

// Business messages of the API that deserve a specific explanation.
const DETAILS = [
  [/Insufficient inventory.*available=(\d+)/, ([, units]) => units === "0"
    ? "No quedan unidades disponibles de este producto."
    : `No hay suficientes unidades. Disponibles, sin contar las reservadas: ${units}.`],
  [/is not available in the catalog/, () => "Este producto ya no está disponible."],
  [/no active shopping cart/, () => "Tu carrito está vacío."],
  [/password policy/, () => "La contraseña debe tener al menos una letra y un número."],
  [/A person with (email|identifier) .* already exists/, () => "Ya existe una cuenta con ese correo o esa identificación."],
  [/Adjustment would drive available stock negative \(current: (\d+)/, ([, units]) =>
    `El ajuste dejaría el inventario en negativo. Unidades disponibles: ${units}.`],
  [/A (product|warehouse) with identifier/, () => "Ya existe otro con ese código."],
  [/discontinued and cannot/, () => "El producto está descontinuado y ya no se puede cambiar."],
  [/holds \d+ reserved units/, () => "No hay unidades reservadas suficientes para despachar este pedido."],
  [/Person .* is not active/, () => "Esa persona no está activa. Elige otra."],
  [/Illegal order transition|is not in transit|was not returned|already has a shipment/, () => STATE_CHANGED],
  [/must be PAID to enter|is finalized with status|can only be finalized/, () => STATE_CHANGED],
  [/Cannot (approve|reject|complete) return|already generated a refund/, () => STATE_CHANGED],
  [/already has an active return/, () => "Este pedido ya tiene una devolución activa."],
  [/not authorized to perform purchasing/, () => "Tu cuenta no tiene permiso para comprar. Comunícate con un administrador."],
  [/cannot change its own status/, () => "No puedes cambiar el estado de tu propia cuenta."],
  [/must be different from the current/, () => "La contraseña nueva debe ser distinta de la actual."]
];

function translate(code, detail) {
  for (const [pattern, message] of DETAILS) {
    const match = detail?.match(pattern);
    if (match) return message(match);
  }
  return MESSAGES[code] ?? "Ocurrió un error inesperado. Intenta de nuevo.";
}

export class ApiError extends Error {
  constructor(status, code, detail) {
    super(translate(code, detail));
    this.status = status;
    this.code = code;
    // Technical message returned by the API
    this.detail = detail;
  }
}
