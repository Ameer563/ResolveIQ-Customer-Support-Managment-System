import { useEffect, useRef, useState } from "react";
import api, { errMsg } from "../api";

export default function AiAssistant() {
  const [open, setOpen] = useState(false);
  const [msgs, setMsgs] = useState([{ from: "bot", text: "Hi! I can help you raise a complaint or check a ticket's status. Try: \"What is the status of my tickets?\"" }]);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const end = useRef();
  useEffect(() => { end.current?.scrollIntoView({ behavior: "smooth" }); }, [msgs, open]);

  const send = async (e) => {
    e.preventDefault();
    const m = text.trim();
    if (!m || busy) return;
    setText(""); setBusy(true);
    setMsgs((x) => [...x, { from: "me", text: m }]);
    try {
      const r = await api.post("/ai/assistant", { message: m });
      setMsgs((x) => [...x, { from: "bot", text: r.data.reply }]);
    } catch (err) {
      setMsgs((x) => [...x, { from: "bot", text: errMsg(err) }]);
    }
    setBusy(false);
  };

  return (
    <>
      {open && (
        <section className="chat" aria-label="AI assistant">
          <div className="chat-head"><b>AI assistant</b><button className="link" onClick={() => setOpen(false)}>Close</button></div>
          <div className="chat-body">
            {msgs.map((m, i) => <p key={i} className={"msg " + m.from}>{m.text}</p>)}
            {busy && <p className="msg bot">Thinking...</p>}
            <div ref={end} />
          </div>
          <form onSubmit={send} className="chat-form">
            <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Ask about your tickets" />
            <button className="btn" disabled={busy}>Send</button>
          </form>
        </section>
      )}
      <button className="fab" onClick={() => setOpen(!open)}>{open ? "Hide assistant" : "Ask AI"}</button>
    </>
  );
}
