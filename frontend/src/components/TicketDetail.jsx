import { useEffect, useState } from "react";
import api, { errMsg } from "../api";
import Badge from "./Badge";

const STEPS = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];

export default function TicketDetail({ id, staff, onChange }) {
  const [t, setT] = useState(null);
  const [cm, setCm] = useState([]);
  const [agents, setAgents] = useState([]);
  const [msg, setMsg] = useState("");
  const [internal, setInternal] = useState(false);
  const [rating, setRating] = useState(0);
  const [fb, setFb] = useState("");
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState("");

  const load = async () => {
    const [a, b] = await Promise.all([api.get(`/tickets/${id}`), api.get(`/tickets/${id}/comments`)]);
    setT(a.data); setCm(b.data);
  };
  useEffect(() => { setT(null); setErr(""); load(); if (staff) api.get("/staff/agents").then((r) => setAgents(r.data)); }, [id]);

  const run = async (key, fn) => {
    setBusy(key); setErr("");
    try { await fn(); await load(); onChange && onChange(); } catch (e) { setErr(errMsg(e)); }
    setBusy("");
  };
  const comment = (text, isInternal) => run("c", async () => { await api.post(`/tickets/${id}/comments`, { message: text, internal: isInternal }); setMsg(""); });

  if (!t) return <div className="card">Loading ticket...</div>;
  const step = STEPS.indexOf(t.status);

  return (
    <div className="card detail">
      <div className="row between">
        <h2>{t.ticketNumber}: {t.subject}</h2>
        <div className="row"><Badge v={t.priority} /><Badge v={t.sentiment} /></div>
      </div>
      <ol className="steps">{STEPS.map((s, i) => <li key={s} className={i <= step ? "done" : ""}>{s.replace("_", " ").toLowerCase()}</li>)}</ol>
      <p>{t.description}</p>
      <dl>
        <dt>Category</dt><dd>{t.category}</dd>
        {staff && <><dt>Customer</dt><dd>{t.customer}</dd></>}
        <dt>Assigned to</dt><dd>{t.assignedTo || "Waiting for an agent"}</dd>
        <dt>Raised</dt><dd>{new Date(t.createdAt).toLocaleString()}</dd>
        {t.aiSummary && <><dt>AI summary</dt><dd>{t.aiSummary}</dd></>}
      </dl>

      {staff && (
        <div className="staff">
          <div className="row">
            <label>Status<select value={t.status} onChange={(e) => run("s", () => api.put(`/staff/tickets/${id}/status`, { value: e.target.value }))}>{STEPS.map((s) => <option key={s}>{s}</option>)}</select></label>
            <label>Priority<select value={t.priority} onChange={(e) => run("p", () => api.put(`/staff/tickets/${id}/priority`, { value: e.target.value }))}>{["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((s) => <option key={s}>{s}</option>)}</select></label>
            <label>Assign<select value={t.assignedToId || ""} onChange={(e) => run("a", () => api.put(`/staff/tickets/${id}/assign`, { agentId: Number(e.target.value) }))}>
              <option value="" disabled>Choose agent</option>{agents.map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}</select></label>
          </div>
          <div className="row">
            <button className="btn ghost" disabled={busy === "r"} onClick={() => run("r", () => api.post(`/staff/tickets/${id}/ai/resolution`))}>{busy === "r" ? "Generating..." : "AI: suggest resolution"}</button>
            <button className="btn ghost" disabled={busy === "y"} onClick={() => run("y", () => api.post(`/staff/tickets/${id}/ai/reply`))}>{busy === "y" ? "Generating..." : "AI: draft customer reply"}</button>
          </div>
          {t.suggestedResolution && <div className="ai"><b>Suggested resolution</b><pre>{t.suggestedResolution}</pre></div>}
          {t.aiReply && <div className="ai"><b>Draft reply</b><pre>{t.aiReply}</pre>
            <button className="btn" onClick={() => comment(t.aiReply, false)}>Send to customer</button></div>}
        </div>
      )}

      <h3>Conversation</h3>
      {cm.length === 0 && <p className="muted">No messages yet.</p>}
      {cm.map((c) => (
        <div key={c.id} className={"cm" + (c.internal ? " internal" : "")}>
          <b>{c.author}</b> <span className="muted">{c.role.toLowerCase()} {c.internal && "· internal note"} · {new Date(c.createdAt).toLocaleString()}</span>
          <p>{c.message}</p>
        </div>
      ))}
      <form className="row" onSubmit={(e) => { e.preventDefault(); if (msg.trim()) comment(msg, internal); }}>
        <input className="grow" placeholder="Write a message" value={msg} onChange={(e) => setMsg(e.target.value)} />
        {staff && <label className="inline"><input type="checkbox" checked={internal} onChange={(e) => setInternal(e.target.checked)} /> Internal note</label>}
        <button className="btn" disabled={busy === "c"}>Send</button>
      </form>

      {!staff && (t.status === "RESOLVED" || t.status === "CLOSED") && (
        <div className="feedback">
          <h3>How did we do?</h3>
          {t.rating ? (
            <p>You rated this {"★".repeat(t.rating)}{"☆".repeat(5 - t.rating)}. {t.feedback}</p>
          ) : (
            <>
              <div className="stars">{[1, 2, 3, 4, 5].map((n) => <button key={n} className={n <= rating ? "on" : ""} onClick={() => setRating(n)} aria-label={n + " stars"}>★</button>)}</div>
              <textarea rows={3} placeholder="Tell us more (optional)" value={fb} onChange={(e) => setFb(e.target.value)} />
              <button className="btn" disabled={!rating} onClick={() => run("f", () => api.post(`/tickets/${id}/feedback`, { rating, feedback: fb }))}>Submit feedback</button>
            </>
          )}
        </div>
      )}
      {err && <p className="err">{err}</p>}
    </div>
  );
}
