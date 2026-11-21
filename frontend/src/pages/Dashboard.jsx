import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";
import { useAuth } from "../auth";
import Badge from "../components/Badge";

export default function Dashboard() {
  const { user } = useAuth();
  const [t, setT] = useState([]);
  useEffect(() => { api.get("/tickets/mine").then((r) => setT(r.data)); }, []);
  const n = (...s) => t.filter((x) => s.includes(x.status)).length;
  const h = new Date().getHours();
  const greet = h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
  const active = n("OPEN", "IN_PROGRESS");
  const stats = [["All tickets", t.length, "s-all"], ["Open", n("OPEN"), "s-open"], ["In progress", n("IN_PROGRESS"), "s-prog"], ["Resolved", n("RESOLVED", "CLOSED"), "s-done"]];

  return (
    <div>
      <section className="hero-band">
        <div>
          <h1>{greet}, {user.name.split(" ")[0]}</h1>
          <p>{active > 0 ? `${active} ${active === 1 ? "ticket is" : "tickets are"} being worked on right now.` : "No active tickets. Everything is resolved."}</p>
        </div>
        <div className="row">
          <Link className="btn light" to="/raise">Raise a complaint</Link>
          <Link className="btn outline" to="/track">Track complaints</Link>
        </div>
      </section>
      <div className="grid4">
        {stats.map(([l, v, c]) => <div className={"card stat " + c} key={l}><b>{v}</b><span>{l}</span></div>)}
      </div>
      <h2>Latest tickets</h2>
      {t.length === 0 && <p className="muted">No tickets yet. Raise your first complaint and it is routed to an agent right away.</p>}
      {t.slice(0, 5).map((x) => (
        <Link key={x.id} to="/track" className="card item">
          <div><b>{x.ticketNumber}</b> &nbsp;{x.subject}<br /><span className="muted">{x.category} · {new Date(x.createdAt).toLocaleDateString()}</span></div>
          <div className="row"><Badge v={x.status} /><Badge v={x.priority} /></div>
        </Link>
      ))}
    </div>
  );
}
