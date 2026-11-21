import AuthShell from "../components/AuthShell";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api, { errMsg } from "../api";
import { useAuth } from "../auth";

export default function Login() {
  const [f, setF] = useState({ email: "", password: "" });
  const [err, setErr] = useState("");
  const { login } = useAuth();
  const nav = useNavigate();

  const submit = async (e) => {
    e.preventDefault(); setErr("");
    try {
      const r = await api.post("/auth/login", f);
      login(r.data);
      nav("/dashboard");
    } catch (e2) { setErr(errMsg(e2)); }
  };

  return (
    <AuthShell><form className="auth-form" onSubmit={submit}>
      <h1>Welcome back</h1>
      <p className="muted">Log in to check in on your complaints.</p>
      <label>Email<input type="email" required value={f.email} onChange={(e) => setF({ ...f, email: e.target.value })} /></label>
      <label>Password<input type="password" required value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} /></label>
      {err && <p className="err">{err}</p>}
      <button className="btn">Log in</button>
      <p className="muted">New here? <Link to="/register">Create an account</Link></p>
    </form></AuthShell>
  );
}
