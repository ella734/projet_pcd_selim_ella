import { Navigate, useLocation } from 'react-router-dom';

export default function PrivateRoute({ children, requiredRole = null }) {
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null'); }
    catch { return null; }
  })();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole && user.role?.toUpperCase() !== requiredRole.toUpperCase()) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
