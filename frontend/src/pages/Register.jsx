import AuthShell from "../components/AuthShell";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { errMsg } from "../api";

export default function Register() {
  const [f, setF] = useState({ name: "", email: "", phone: "", password: "" });
  const [err, setErr] = useState("");
  const [ok, setOk] = useState("");
  const nav = useNavigate();
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault(); setErr("");
    try {
      const r = await api.post("/auth/register", f);
      setOk(r.data.message);
      setTimeout(() => nav("/login"), 1200);
    } catch (e2) { setErr(errMsg(e2)); }
  };

  return (
    <AuthShell><form className="auth-form" onSubmit={submit}>
      <h1>Create your account</h1>
      <p className="muted">Register once to raise and track complaints.</p>
      <label>Full name<input required value={f.name} onChange={set("name")} /></label>
      <label>Email<input type="email" required value={f.email} onChange={set("email")} /></label>
      <label>Phone<input value={f.phone} onChange={set("phone")} /></label>
      <label>Password<input type="password" minLength={6} required value={f.password} onChange={set("password")} /></label>
      {err && <p className="err">{err}</p>}
      {ok && <p className="okmsg">{ok}</p>}
      <button className="btn">Create account</button>
      <p className="muted">Already registered? <Link to="/login">Log in</Link></p>
    </form></AuthShell>
  );
}
