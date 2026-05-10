import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Patients from './pages/Patients';
import Medecins from './pages/Medecins';
import Hopitaux from './pages/Hopitaux';
import Services from './pages/Services';
import Users from './pages/Users';
import Transplantations from './pages/Transplantations';
import MedicalData from './pages/MedicalData';
import api from './services/api';

function App() {
  const [bootstrapping, setBootstrapping] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    let cancelled = false;

    const bootstrapSession = async () => {
      const storedUser = localStorage.getItem('user');

      if (!storedUser) {
        if (!cancelled) {
          setIsAuthenticated(false);
          setBootstrapping(false);
        }
        return;
      }

      try {
        const { data } = await api.get('/auth/me');
        if (cancelled) return;

        localStorage.setItem('user', JSON.stringify({
          username: data.username,
          role: data.role,
          service: data.services?.[0]?.libelleS || 'Unknown Service',
          hopital: data.services?.[0]?.hopital?.libelleH || 'Unknown Hospital',
          serviceId: data.services?.[0]?.identifiantS || null,
          hopitalId: data.services?.[0]?.hopital?.identifiantH || null,
        }));
        setIsAuthenticated(true);
      } catch (error) {
        if (!cancelled) {
          localStorage.removeItem('user');
          setIsAuthenticated(false);
        }
      } finally {
        if (!cancelled) {
          setBootstrapping(false);
        }
      }
    };

    bootstrapSession();
    return () => { cancelled = true; };
  }, []);

  if (bootstrapping) {
    return <div style={{ padding: '2rem', color: '#334155' }}>Chargement de la session...</div>;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login onLoginSuccess={() => setIsAuthenticated(true)} />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Layout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="patients" element={<Patients />} />
          <Route path="medecins" element={<Medecins />} />
          <Route path="transplantations" element={<Transplantations />} />
          <Route path="medical-data" element={<MedicalData />} />
          <Route
            path="hopitaux"
            element={
              <PrivateRoute requiredRole="ADMIN">
                <Hopitaux />
              </PrivateRoute>
            }
          />
          <Route
            path="services"
            element={
              <PrivateRoute requiredRole="ADMIN">
                <Services />
              </PrivateRoute>
            }
          />
          <Route
            path="users"
            element={
              <PrivateRoute requiredRole="ADMIN">
                <Users />
              </PrivateRoute>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
