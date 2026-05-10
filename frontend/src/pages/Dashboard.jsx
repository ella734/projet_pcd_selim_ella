import { useEffect, useState } from 'react';
import api from '../services/api';
import { Users, PlusSquare, Hospital, LayoutGrid } from 'lucide-react';

export default function Dashboard() {
  const [stats, setStats] = useState({ patients: 0, medecins: 0, services: 0, hopitaux: 0 });

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
        const isAdmin = user.role?.toUpperCase() === 'ADMIN';

        const [patients, medecins, services, hopitaux] = await Promise.all([
          api.get('/patients', { params: !isAdmin ? { serviceId: user.serviceId } : {} }),
          api.get('/medecins', { params: !isAdmin ? { serviceId: user.serviceId } : {} }),
          api.get('/services', { params: !isAdmin ? { hopitalId: user.hopitalId } : {} }),
          api.get('/hopitaux')
        ]);

        setStats({
          patients: Array.isArray(patients.data) ? patients.data.length : 0,
          medecins: Array.isArray(medecins.data) ? medecins.data.length : 0,
          services: Array.isArray(services.data) ? services.data.length : 0,
          hopitaux: Array.isArray(hopitaux.data) ? hopitaux.data.length : 0
        });
      } catch (err) {
        console.error('Erreur lors du chargement des statistiques', err);
      }
    };
    fetchStats();
  }, []);

  const statCards = [
    { name: 'Total patients', value: stats.patients, color: 'var(--primary-color)', icon: Users },
    { name: 'Total médecins', value: stats.medecins, color: 'var(--secondary-color)', icon: PlusSquare },
    { name: 'Services médicaux', value: stats.services, color: 'var(--warning-color)', icon: LayoutGrid },
    { name: 'Hôpitaux', value: stats.hopitaux, color: '#8b5cf6', icon: Hospital },
  ];

  return (
    <div>
      <h1 style={{ fontSize: '1.5rem', marginBottom: '2rem' }}>Tableau de bord</h1>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem' }}>
        {statCards.map((card, i) => {
          const Icon = card.icon;
          return (
            <div key={i} className="card" style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
              <div style={{ background: `${card.color}15`, padding: '1rem', borderRadius: 'var(--radius-lg)' }}>
                <Icon size={32} color={card.color} />
              </div>
              <div>
                <h3 style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', fontWeight: 500 }}>{card.name}</h3>
                <p style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.25rem' }}>{card.value}</p>
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ marginTop: '2.5rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
        <div className="card" style={{ padding: '2rem' }}>
          <h3 className="card-title" style={{ marginBottom: '1rem' }}>Activité récente</h3>
          <p style={{ color: 'var(--text-muted)' }}>Suivi des dossiers médicaux et des données cliniques associées.</p>
        </div>
        <div className="card" style={{ padding: '2rem' }}>
          <h3 className="card-title" style={{ marginBottom: '1rem' }}>État du système</h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--success-color)', fontWeight: 600 }}>
            <div style={{ width: 10, height: 10, background: 'var(--success-color)', borderRadius: '50%' }}></div>
            Base de données opérationnelle
          </div>
        </div>
      </div>
    </div>
  );
}
