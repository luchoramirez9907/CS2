import { Navigate } from "react-router-dom";
import ForbiddenPage from "../pages/ForbiddenPage.jsx";
import { useAuth } from "./useAuth.js";

export default function ProtectedRoute({ role, children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <ForbiddenPage />;
  return children;
}
