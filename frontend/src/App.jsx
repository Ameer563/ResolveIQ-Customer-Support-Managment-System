import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth";
import Navbar from "./components/Navbar";
import AiAssistant from "./components/AiAssistant";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import RaiseComplaint from "./pages/RaiseComplaint";
import TrackComplaints from "./pages/TrackComplaints";
import StaffPanel from "./pages/StaffPanel";

function Protected({ children, staff }) {
  const { user, isStaff } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (staff && !isStaff) return <Navigate to="/dashboard" replace />;
  return children;
}

export default function App() {
  const { user } = useAuth();
  return (
    <>
      <Navbar />
      <main className={"page" + (user ? " with-side" : "")}>
        <Routes>
          <Route path="/" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Protected><Dashboard /></Protected>} />
          <Route path="/raise" element={<Protected><RaiseComplaint /></Protected>} />
          <Route path="/track" element={<Protected><TrackComplaints /></Protected>} />
          <Route path="/staff" element={<Protected staff><StaffPanel /></Protected>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      {user && <AiAssistant />}
    </>
  );
}
