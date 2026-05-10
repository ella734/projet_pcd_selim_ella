import { useState } from 'react';
import { Outlet, Navigate, Link, useLocation } from 'react-router-dom';
import { Activity, Users, PlusSquare, Hospital, LogOut, LayoutGrid, Shield, HeartPulse, ClipboardList } from 'lucide-react';
import { useIdleTimeout } from '../hooks/useIdleTimeout';
import api from '../services/api';

export default function Layout() {
  const [showTimeoutWarning, setShowTimeoutWarning] = useState(false);
  const location = useLocation();

  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null'); }
    catch { return null; }
  })();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  useIdleTimeout(() => setShowTimeoutWarning(true));

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch {
    } finally {
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
  };

  const role = user.role?.toUpperCase();
  const isAdmin = role === 'ADMIN';
  const isMedecin = role === 'MEDECIN';
  const canViewPatients = ['ADMIN', 'MEDECIN', 'MEDECIN_INVESTIGATEUR', 'MEDECIN_SUIVI'].includes(role);
  const canViewTransplantations = ['ADMIN', 'MEDECIN', 'MEDECIN_SUIVI'].includes(role);
  const canViewMedicalData = ['ADMIN', 'MEDECIN', 'MEDECIN_SUIVI', 'AGENT_LABORATOIRE', 'AGENT_IMMUNO'].includes(role);
  const canViewMedecins = ['ADMIN', 'MEDECIN'].includes(role);

  const navItems = [
    { name: 'Tableau de bord', path: '/dashboard', icon: Activity },
    ...(canViewPatients ? [{ name: 'Patients', path: '/patients', icon: Users }] : []),
    ...(canViewMedecins ? [{ name: 'Médecins', path: '/medecins', icon: PlusSquare }] : []),
    ...(canViewTransplantations ? [{ name: 'Transplantations', path: '/transplantations', icon: HeartPulse }] : []),
    ...(canViewMedicalData ? [{ name: 'Données médicales', path: '/medical-data', icon: ClipboardList }] : []),
    ...(isAdmin ? [
      { name: 'Hôpitaux', path: '/hopitaux', icon: Hospital },
      { name: 'Services', path: '/services', icon: LayoutGrid },
      { name: 'Comptes utilisateurs', path: '/users', icon: Shield },
    ] : []),
  ];

  return (
    <div className="app-layout">
      {showTimeoutWarning && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999
        }}>
          <div style={{
            background: 'white', borderRadius: '12px', padding: '2rem',
            maxWidth: '400px', textAlign: 'center', boxShadow: '0 20px 60px rgba(0,0,0,0.3)'
          }}>
            <h3 style={{ marginBottom: '1rem', color: '#c05a1a' }}>Session sur le point d'expirer</h3>
            <p style={{ marginBottom: '1.5rem', color: '#555', fontSize: '0.9rem' }}>
              Votre session va expirer dans 2 minutes en raison d'inactivité.
              Cliquez sur « Continuer » pour rester connecté.
            </p>
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <button
                onClick={() => setShowTimeoutWarning(false)}
                style={{ padding: '0.6rem 1.5rem', background: '#1A3A6B', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer' }}
              >
                Continuer
              </button>
              <button
                onClick={handleLogout}
                style={{ padding: '0.6rem 1.5rem', background: '#eee', color: '#333', border: 'none', borderRadius: '6px', cursor: 'pointer' }}
              >
                Se déconnecter
              </button>
            </div>
          </div>
        </div>
      )}

      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">M</div>
          <h2 style={{ fontSize: '1.25rem', color: 'var(--text-primary)' }}>MedPlatform</h2>
        </div>
        <nav className="sidebar-menu">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname.startsWith(item.path);
            return (
              <Link key={item.path} to={item.path} className={`menu-item ${isActive ? 'active' : ''}`}>
                <Icon size={20} />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="page-title">
            <h2 style={{ fontSize: '1.25rem', fontWeight: 600 }}>Vue d'ensemble</h2>
          </div>
          <div style={{ flex: 1, padding: '0 2rem' }}>
            <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 500 }}>
              Structure : <span style={{ color: 'var(--primary-color)' }}>{user.hopital}</span> |
              Service : <span style={{ color: 'var(--secondary-color)' }}>{user.service}</span>
            </div>
          </div>
          <div className="user-info">
            <div className="user-details">
              <div className="user-name">{user.username}</div>
              <div className="user-context">{user.role}</div>
            </div>
            <div className="avatar">{user.username?.charAt(0).toUpperCase()}</div>
            <button
              onClick={handleLogout}
              style={{ background: 'none', color: 'var(--text-muted)', marginLeft: '1rem' }}
              title="Déconnexion"
            >
              <LogOut size={20} />
            </button>
          </div>
        </header>
        <div className="page-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
