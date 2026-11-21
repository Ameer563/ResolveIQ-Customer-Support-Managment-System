export default function AuthShell({ children }) {
  return (
    <div className="auth-wrap">
      <aside className="auth-hero">
        <div className="brand">ResolveIQ</div>
        <h2>Every complaint read, ranked and routed in seconds.</h2>
        <div className="stub" aria-hidden="true">
          <div className="stub-top"><b>TKT-1042</b><span className="badge b-CRITICAL">critical</span></div>
          <p>Charged twice for order #4411 and nobody has replied.</p>
          <div className="row"><span className="badge b-OPEN">billing</span><span className="badge b-NEGATIVE">negative</span></div>
          <div className="tear" />
          <small>Routed to Priya. Reply drafted by AI.</small>
        </div>
        <p className="on-dark">AI sorts the category, mood and urgency. Your team just resolves.</p>
      </aside>
      <div className="auth-pane">{children}</div>
    </div>
  );
}
