import { useState } from "react";
import { Link } from "react-router-dom";
import api, { errMsg } from "../api";
import Badge from "../components/Badge";

export default function RaiseComplaint() {
  const [f, setF] = useState({ subject: "", description: "" });
  const [res, setRes] = useState(null);
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault(); setErr(""); setBusy(true);
    try { const r = await api.post("/tickets", f); setRes(r.data); setF({ subject: "", description: "" }); }
    catch (e2) { setErr(errMsg(e2)); }
    setBusy(false);
  };

  return (
    <div className="two">
      <form className="card" onSubmit={submit}>
        <h1>Raise a complaint</h1>
        <p className="muted">Describe the problem. Our AI sorts it by category, mood and urgency, then assigns an agent.</p>
        <label>Subject<input required maxLength={150} value={f.subject} onChange={(e) => setF({ ...f, subject: e.target.value })} /></label>
        <label>What happened?<textarea required minLength={10} rows={7} value={f.description} onChange={(e) => setF({ ...f, description: e.target.value })} /></label>
        {err && <p className="err">{err}</p>}
        <button className="btn" disabled={busy}>{busy ? "Analysing with AI..." : "Submit complaint"}</button>
      </form>
      {res && (
        <div className="card result">
          <h2>Ticket {res.ticketNumber} created</h2>
          <dl>
            <dt>Category</dt><dd>{res.category}</dd>
            <dt>Priority</dt><dd><Badge v={res.priority} /></dd>
            <dt>Sentiment</dt><dd><Badge v={res.sentiment} /></dd>
            <dt>Assigned to</dt><dd>{res.assignedTo || "Next available agent"}</dd>
            <dt>AI summary</dt><dd>{res.aiSummary}</dd>
          </dl>
          <Link className="btn" to="/track">Track this complaint</Link>
        </div>
      )}
    </div>
  );
}
