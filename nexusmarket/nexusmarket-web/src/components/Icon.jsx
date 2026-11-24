const PATHS = {
  home: "M3 11l9-8 9 8M5 9.5V21h5v-6h4v6h5V9.5",
  search: "M11 4a7 7 0 100 14 7 7 0 000-14zM16 16l5 5",
  cart: "M3 4h2l2.5 11h10L20 7H6M9 20a1 1 0 100-2 1 1 0 000 2zM16 20a1 1 0 100-2 1 1 0 000 2z",
  box: "M12 3l8 4.5v9L12 21l-8-4.5v-9zM4 7.5l8 4.5 8-4.5M12 12v9",
  undo: "M8 5L4 9l4 4M4 9h10a5 5 0 010 10h-3",
  user: "M12 4a4 4 0 100 8 4 4 0 000-8zM5 21a7 7 0 0114 0",
  users: "M9 5a3.5 3.5 0 100 7 3.5 3.5 0 000-7zM2.5 20a6.5 6.5 0 0113 0M16 5.5a3.5 3.5 0 010 6.3M18 14.5a6.5 6.5 0 013.5 5.5",
  tag: "M4 4h8l9 9-8 8-9-9zM8 8h.01",
  warehouse: "M3 21V9l9-5 9 5v12M7 21v-8h10v8M7 17h10",
  layers: "M12 3l9 5-9 5-9-5zM3 12.5l9 5 9-5M3 17l9 5 9-5",
  truck: "M3 6h11v10H3zM14 10h4l3 3v3h-7M7 19a2 2 0 100-4 2 2 0 000 4zM17 19a2 2 0 100-4 2 2 0 000 4z",
  card: "M3 6h18v12H3zM3 10h18M7 15h3",
  chart: "M4 20h16M7 16v-5M12 16V6M17 16v-8",
  logout: "M14 8V5H5v14h9v-3M10 12h11M18 9l3 3-3 3"
};

export default function Icon({ name }) {
  return (
    <svg className="icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d={PATHS[name]} />
    </svg>
  );
}
