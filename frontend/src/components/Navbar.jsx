import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";

const Item = ({ to, d, children }) => (
  <NavLink to={to}>
    <svg viewBox="0 0 24 24" aria-hidden="true"><path d={d} /></svg>
    <span>{children}</span>
  </NavLink>
);

export default function Navbar() {
  const { user, logout, isStaff } = useAuth();
  const nav = useNavigate();
  if (!user) return null;
  return (
    <aside className="nav side">
      <div className="brand">ResolveIQ</div>
      <nav>
        <Item to="/dashboard" d="M3 11.5 12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z">Check-in</Item>
        <Item to="/raise" d="M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18Zm0 5v8m-4-4h8">Raise complaint</Item>
        <Item to="/track" d="M4 6h16M4 12h10M4 18h7">Track complaints</Item>
        {isStaff && <Item to="/staff" d="M5 20V10m7 10V4m7 16v-7">Support desk</Item>}
      </nav>
      <div className="me">
        <span className="avatar">{user.name[0].toUpperCase()}</span>
        <div><b>{user.name}</b><small>{user.role.toLowerCase()}</small></div>
        <button className="link" onClick={() => { logout(); nav("/login"); }}>Log out</button>
      </div>
    </aside>
  );
}
