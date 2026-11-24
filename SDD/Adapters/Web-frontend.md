# Web Frontend

## 1. Overview

Browser client of the NexusMarket API for the five roles. It is an external driving actor of the hexagon: it only knows the REST contract (`Api-rest-endpoints.md`) and shares no code with the backend.

- Location: `nexusmarket/nexusmarket-web`.
- Technology: React 19, React Router 7 and Vite 8. No other runtime library.
- Address: `http://localhost:5173`, the only origin the API accepts (`Rest-security-cors.md`, section 4).

---

## 2. Structure

| Folder | Responsibility |
| ------ | -------------- |
| `src/api` | Calls to the API. `client.js` is the single entry point: it adds the token and normalizes every error; `errors.js` translates them; `session.js` stores the session |
| `src/auth` | Session (`AuthProvider`), role menus (`roles.js`) and route protection (`ProtectedRoute`) |
| `src/components` | Shared pieces: layout, lists, order detail, forms, confirmations |
| `src/pages` | One folder per area: `buyer`, `seller`, `operator`, `admin`, `staff` (administrator and supervisor), `stock`, `returns`, `reports` |
| `src/utils` | Formats and labels, form rules and loading hooks |
| `src/routes.jsx` | Pages of each role |

---

## 3. Screens by Role

| Role | Screens |
| ---- | ------- |
| Visitor | Login, buyer registration |
| Buyer | Catalog, product, cart, orders (invoice and tracking), returns, profile |
| Seller | Products, warehouses, inventory, sales, returns, performance report, profile |
| Logistics operator | Assigned shipments, inventory, profile |
| Administrator | Users, orders and payments, shipments, returns and refunds, warehouses and inventory, commercial report, profile |
| Supervisor | Users, orders, returns, inventory and commercial report (read only), password |

---

## 4. Session and Security

- The token and the authenticated user are kept in `sessionStorage`: they are cleared when the tab is closed. The password is never stored.
- The token lasts one hour and cannot be renewed: when it expires, the web returns to the login.
- A `401` with code `AUTHENTICATION_REQUIRED` ends the session. A `401` with code `INVALID_CREDENTIALS` (wrong current password) does not.
- The session is checked on every navigation, so a blocked user leaves at once.
- Logout calls `POST /api/v1/auth/logout`, which invalidates the token in the API.
- `ProtectedRoute` hides the pages of other roles. This is only usability: the API enforces role, permission matrix and ownership (`403`).
- In Docker, nginx sends a `Content-Security-Policy` that only runs the scripts of the web.

---

## 5. Behavior Rules

- Every form validates the same limits as the API before sending, and shows the error next to the field.
- Business errors are explained with their data (for example, how many units are available).
- Actions that cannot be undone ask for confirmation.
- A page shows only the next valid step of an order, a shipment or a return.
- When somebody else changed the data, the page warns and reloads its current state.

---

## 6. Limits of the API Reflected in the Web

| Limit | How the web solves it |
| ----- | --------------------- |
| The catalog requires a token | The first screen is the login |
| A product does not include its stock or images | The available units appear when they are not enough; each product shows its initial |
| The operator only lists its assigned shipments | The administrator assigns the shipment of each paid order |
| A seller does not list the marketplace warehouses | The inventory screen accepts a warehouse code |

---

## 7. Running

| Mode | Command |
| ---- | ------- |
| Development | `npm install` and `npm run dev` in `nexusmarket/nexusmarket-web` (tested with Node.js 26) |
| Docker | `docker compose up --build` (container `nexusmarket-web`) |

`VITE_API_URL` changes the address of the API (default `http://localhost:8080/api/v1`). It is fixed when the web is built.
