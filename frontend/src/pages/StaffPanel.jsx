import { useEffect, useState } from "react";
import api from "../api";
import Badge from "../components/Badge";
import TicketDetail from "../components/TicketDetail";

function Bars({ title, data }) {
  const max = Math.max(1, ...Object.values(data));
  return (
    <div className="card">
      <h3>{title}</h3>
      {Object.entries(data).map(([k, v]) => (
        <div className="bar" key={k}><span>{k.replace("_", " ").toLowerCase()}</span><i style={{ width: (v / max) * 100 + "%" }} /><b>{v}</b></div>
      ))}
      {Object.keys(data).length === 0 && <p className="muted">No data yet</p>}
    </div>
  );
}

export default function StaffPanel() {
  const [list, setList] = useState([]);
  const [stats, setStats] = useState(null);
  const [sel, setSel] = useState(null);
  const [filter, setFilter] = useState("ALL");
  const load = () => {
    api.get("/staff/tickets").then((r) => setList(r.data));
    api.get("/staff/analytics").then((r) => setStats(r.data));
  };
  useEffect(load, []);
  const shown = list.filter((x) => filter === "ALL" || x.status === filter);

  return (
    <div>
      <h1>Support desk</h1>
      {stats && (
        <>
          <div className="grid4">
            <div className="card stat"><b>{stats.total}</b><span>Total tickets</span></div>
            <div className="card stat"><b>{stats.byStatus.OPEN || 0}</b><span>Open</span></div>
            <div className="card stat"><b>{stats.byPriority.CRITICAL || 0}</b><span>Critical</span></div>
            <div className="card stat"><b>{stats.avgRating ? stats.avgRating.toFixed(1) : "-"}</b><span>Avg rating ({stats.ratedCount})</span></div>
          </div>
          <div className="grid4">
            <Bars title="By status" data={stats.byStatus} />
            <Bars title="By priority" data={stats.byPriority} />
            <Bars title="By category" data={stats.byCategory} />
            <Bars title="By sentiment" data={stats.bySentiment} />
          </div>
        </>
      )}
      <div className="row between"><h2>All tickets</h2>
        <select value={filter} onChange={(e) => setFilter(e.target.value)}>{["ALL", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"].map((s) => <option key={s}>{s}</option>)}</select>
      </div>
      <div className="split">
        <aside>
          {shown.map((x) => (
            <button key={x.id} className={"card item pick" + (sel === x.id ? " on" : "")} onClick={() => setSel(x.id)}>
              <div><b>{x.ticketNumber}</b> {x.subject}<br /><span className="muted">{x.customer} · {x.category} · {x.assignedTo || "unassigned"}</span></div>
              <div className="col"><Badge v={x.status} /><Badge v={x.priority} /></div>
            </button>
          ))}
          {shown.length === 0 && <p className="muted">No tickets in this view.</p>}
        </aside>
        <section>{sel ? <TicketDetail id={sel} staff onChange={load} /> : <p className="muted">Select a ticket to work on it.</p>}</section>
      </div>
    </div>
  );
}
