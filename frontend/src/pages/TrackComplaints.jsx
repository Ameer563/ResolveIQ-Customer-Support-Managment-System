import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import Badge from "../components/Badge";
import TicketDetail from "../components/TicketDetail";

export default function TrackComplaints() {
  const [list, setList] = useState([]);
  const [sel, setSel] = useState(null);
  const load = () => api.get("/tickets/mine").then((r) => { setList(r.data); if (!sel && r.data[0]) setSel(r.data[0].id); });
  useEffect(() => { load(); }, []);

  if (list.length === 0) return <div><h1>Track complaints</h1><p className="muted">Nothing to track yet. <Link to="/raise">Raise a complaint</Link></p></div>;
  return (
    <div className="split">
      <aside>
        <h1>Track complaints</h1>
        {list.map((x) => (
          <button key={x.id} className={"card item pick" + (sel === x.id ? " on" : "")} onClick={() => setSel(x.id)}>
            <div><b>{x.ticketNumber}</b><br />{x.subject}</div>
            <Badge v={x.status} />
          </button>
        ))}
      </aside>
      <section>{sel && <TicketDetail id={sel} onChange={load} />}</section>
    </div>
  );
}
