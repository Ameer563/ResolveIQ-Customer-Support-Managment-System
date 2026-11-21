export default function Badge({ v }) {
  if (!v) return null;
  return <span className={"badge b-" + v}>{v.replace("_", " ").toLowerCase()}</span>;
}
