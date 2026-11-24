// Fields shared by several forms, with the limits the API validates.
export const noSpaces = (value) => (/\s/.test(value.trim()) ? "No puede tener espacios." : "");

export const emailRule = (value) => (/^\S+@\S+\.\S+$/.test(value.trim()) ? "" : "Escribe un correo válido.");

export const passwordRule = (value) => (value.length >= 8 && /\p{L}/u.test(value) && /\d/.test(value)
  ? "" : "Usa mínimo 8 caracteres, con una letra y un número.");

export const IDENTIFIER = { name: "identifier", label: "Número de identificación", max: 64, autoComplete: "off",
  rule: noSpaces };

export const PERSON_FIELDS = [
  { name: "fullName", label: "Nombre completo", max: 160, autoComplete: "name" },
  { name: "email", label: "Correo", type: "email", max: 160, autoComplete: "email", rule: emailRule }
];

export const PASSWORD = { name: "password", label: "Contraseña", type: "password", max: 100,
  autoComplete: "new-password", hint: "Mínimo 8 caracteres, con una letra y un número.", rule: passwordRule };

export const ADDRESS_FIELDS = [
  { name: "street", label: "Dirección", max: 200, autoComplete: "street-address" },
  { name: "city", label: "Ciudad", max: 100, autoComplete: "address-level2" },
  { name: "state", label: "Departamento", max: 100, autoComplete: "address-level1" },
  { name: "country", label: "País", max: 100, autoComplete: "country-name" },
  { name: "postalCode", label: "Código postal", max: 20, autoComplete: "postal-code" }
];

export const toAddress = (text) => Object.fromEntries(ADDRESS_FIELDS.map(({ name }) => [name, text(name)]));
