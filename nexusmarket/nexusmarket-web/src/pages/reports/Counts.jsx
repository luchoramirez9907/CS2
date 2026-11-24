/** How many items there are in each status, in the order of the labels. */
export default function Counts({ title, counts, labels }) {
  const rows = Object.entries(labels).filter(([code]) => counts[code]);
  if (rows.length === 0) return null;
  return (
    <section aria-label={title}>
      <h2>{title}</h2>
      <dl className="pairs">
        {rows.map(([code, label]) => <div key={code}><dt>{label}</dt><dd>{counts[code]}</dd></div>)}
      </dl>
    </section>
  );
}
