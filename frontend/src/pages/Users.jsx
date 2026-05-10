import { useState, useEffect } from 'react';
import api from '../services/api';
import { Users as UsersIcon, X, Edit2, Trash2, UserPlus } from 'lucide-react';
import { validateUser, hasErrors, sanitize } from '../schemas/validation';

export default function Users() {
  const [users, setUsers] = useState([]);
  const [services, setServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [serviceFilter, setServiceFilter] = useState('ALL');
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formErrors, setFormErrors] = useState({});
  const [formData, setFormData] = useState({
    loginU: '', motPasseU: '', role: 'MEDECIN', serviceId: ''
  });

  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); }
    catch { return {}; }
  })();
  const isAdmin = user.role?.toUpperCase() === 'ADMIN';

  useEffect(() => {
    fetchUsers();
    fetchServices();
  }, []);

  const fetchUsers = async () => {
    try {
      const { data } = await api.get('/users');
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch users', err);
    }
  };

  const fetchServices = async () => {
    try {
      const { data } = await api.get('/services');
      setServices(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch services', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (formErrors[name]) setFormErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const handleSaveUser = async (e) => {
    e.preventDefault();

    const errors = validateUser({
      loginU: formData.loginU,
      motPasseU: formData.motPasseU,
      role: formData.role,
      isEditing: !!editingId,
    });

    if (hasErrors(errors)) {
      setFormErrors(errors);
      return;
    }

    try {
      const payload = {
        loginU: sanitize(formData.loginU),
        role: formData.role,
        serviceId: formData.serviceId ? parseInt(formData.serviceId, 10) : null,
        ...(formData.motPasseU ? { motPasseU: formData.motPasseU } : {}),
      };

      if (editingId) {
        await api.put(`/users/${editingId}`, payload);
      } else {
        await api.post('/users', payload);
      }

      handleClose();
      fetchUsers();
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || err.message;
      alert(`Erreur : ${msg}`);
    }
  };

  const handleEdit = (account) => {
    setFormData({
      loginU: account.loginU || '',
      motPasseU: '',
      role: account.role || 'MEDECIN',
      serviceId: account.service ? account.service.identifiantS : '',
    });
    setEditingId(account.identifiantU);
    setFormErrors({});
    setIsAdding(true);
  };

  const handleDelete = async (id) => {
    const targetUser = users.find((account) => account.identifiantU === id);
    if (targetUser?.loginU === user.username) {
      alert('Vous ne pouvez pas supprimer votre propre compte.');
      return;
    }

    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      try {
        await api.delete(`/users/${id}`);
        fetchUsers();
      } catch (err) {
        alert('Erreur lors de la suppression.');
      }
    }
  };

  const handleClose = () => {
    setIsAdding(false);
    setEditingId(null);
    setFormErrors({});
    setFormData({ loginU: '', motPasseU: '', role: 'MEDECIN', serviceId: '' });
  };

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredUsers = users.filter((account) => {
    const matchesSearch = !normalizedSearch || [
      account.identifiantU,
      account.loginU,
      account.role,
      account.service?.libelleS,
      account.service?.hopital?.libelleH,
    ].some((value) => String(value ?? '').toLowerCase().includes(normalizedSearch));

    const matchesRole = roleFilter === 'ALL' || account.role === roleFilter;
    const matchesService = serviceFilter === 'ALL' || String(account.service?.identifiantS ?? '') === serviceFilter;

    return matchesSearch && matchesRole && matchesService;
  });

  if (!isAdmin) {
    return <div style={{ padding: '2rem', color: 'var(--danger-color)' }}>Accès non autorisé.</div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem' }}>Gestion des utilisateurs</h1>
        <button className="btn-primary" style={{ width: 'auto', padding: '0.5rem 1rem' }} onClick={() => setIsAdding(true)}>
          <UserPlus size={18} style={{ marginRight: '0.5rem' }} />
          Nouveau compte
        </button>
      </div>

      <div className="card">
        <div className="card-header">
          <div className="card-title">Utilisateurs du système ({filteredUsers.length})</div>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', padding: '0 1.5rem 1rem' }}>
          <input
            type="text"
            placeholder="Rechercher un utilisateur..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ flex: '1 1 280px', minWidth: '240px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          />
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            style={{ minWidth: '180px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les rôles</option>
            <option value="ADMIN">ADMIN</option>
            <option value="MEDECIN">MEDECIN</option>
            <option value="MEDECIN_INVESTIGATEUR">MEDECIN_INVESTIGATEUR</option>
            <option value="MEDECIN_SUIVI">MEDECIN_SUIVI</option>
            <option value="AGENT_LABORATOIRE">AGENT_LABORATOIRE</option>
            <option value="AGENT_IMMUNO">AGENT_IMMUNO</option>
          </select>
          <select
            value={serviceFilter}
            onChange={(e) => setServiceFilter(e.target.value)}
            style={{ minWidth: '220px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les services</option>
            {services.map((service) => (
              <option key={service.identifiantS} value={String(service.identifiantS)}>
                {service.libelleS}
              </option>
            ))}
          </select>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Identifiant</th>
                <th>Rôle</th>
                <th>Service affecté</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((account) => (
                <tr key={account.identifiantU}>
                  <td>#{account.identifiantU}</td>
                  <td style={{ fontWeight: 600 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <UsersIcon size={16} color="var(--primary-color)" />
                      {account.loginU}
                      {account.loginU === user.username && (
                        <span style={{ fontSize: '0.7rem', background: '#e0f2fe', color: '#0369a1', padding: '1px 6px', borderRadius: '10px' }}>vous</span>
                      )}
                    </div>
                  </td>
                  <td>
                    <span className={`tag ${account.role === 'ADMIN' ? 'tag-purple' : account.role?.startsWith('AGENT') ? 'tag-green' : 'tag-blue'}`}>{account.role}</span>
                  </td>
                  <td>
                    {account.service ? (
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                        <span style={{ fontWeight: 500 }}>{account.service.libelleS}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {account.service.hopital?.libelleH || 'Aucun hôpital'}
                        </span>
                      </div>
                    ) : (
                      <span style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>Aucune affectation</span>
                    )}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleEdit(account)} style={{ color: 'var(--text-secondary)', background: 'none' }} title="Modifier">
                        <Edit2 size={18} />
                      </button>
                      {account.loginU !== user.username && (
                        <button onClick={() => handleDelete(account.identifiantU)} style={{ color: 'var(--danger-color)', background: 'none' }} title="Supprimer">
                          <Trash2 size={18} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filteredUsers.length === 0 && (
                <tr><td colSpan="5" style={{ textAlign: 'center', padding: '3rem' }}>Aucun utilisateur trouvé.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {isAdding && (
        <div className="modal-overlay" onClick={handleClose}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="card-title">{editingId ? 'Modifier le compte' : 'Créer un compte'}</h2>
              <button onClick={handleClose} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <form onSubmit={handleSaveUser} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
              <div className="modal-body">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Identifiant *</label>
                  <input
                    type="text"
                    name="loginU"
                    value={formData.loginU}
                    onChange={handleInputChange}
                    required
                    maxLength={50}
                    style={{ padding: '0.5rem', border: `1px solid ${formErrors.loginU ? 'red' : 'var(--border-color)'}`, borderRadius: '4px' }}
                  />
                  {formErrors.loginU && <span style={{ color: 'red', fontSize: '0.75rem' }}>{formErrors.loginU}</span>}
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>
                    Mot de passe {editingId ? '(laisser vide pour ne pas changer)' : '*'}
                  </label>
                  <input
                    type="password"
                    name="motPasseU"
                    value={formData.motPasseU}
                    onChange={handleInputChange}
                    required={!editingId}
                    maxLength={100}
                    autoComplete="new-password"
                    style={{ padding: '0.5rem', border: `1px solid ${formErrors.motPasseU ? 'red' : 'var(--border-color)'}`, borderRadius: '4px' }}
                  />
                  {formErrors.motPasseU && <span style={{ color: 'red', fontSize: '0.75rem' }}>{formErrors.motPasseU}</span>}
                  {!editingId && <small style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>Minimum 8 caractères</small>}
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Rôle *</label>
                  <select
                    name="role"
                    value={formData.role}
                    onChange={handleInputChange}
                    required
                    style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
                  >
                    <option value="ADMIN">Administrateur</option>
                    <option value="MEDECIN">Médecin</option>
                    <option value="MEDECIN_INVESTIGATEUR">Médecin investigateur</option>
                    <option value="MEDECIN_SUIVI">Médecin de suivi</option>
                    <option value="AGENT_LABORATOIRE">Agent laboratoire</option>
                    <option value="AGENT_IMMUNO">Agent immunologie</option>
                  </select>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', marginTop: '0.5rem' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--primary-color)' }}>
                    Service affecté {formData.role !== 'ADMIN' ? '*' : ''}
                  </label>
                  <select
                    name="serviceId"
                    value={formData.serviceId}
                    onChange={handleInputChange}
                    required={formData.role !== 'ADMIN'}
                    style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}
                  >
                    <option value="">-- Sélectionner un service --</option>
                    {services.map((service) => (
                      <option key={service.identifiantS} value={service.identifiantS}>
                        {service.libelleS} ({service.hopital?.libelleH || 'Sans hôpital'})
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-primary" style={{ width: 'auto', background: 'var(--text-secondary)' }} onClick={handleClose}>Annuler</button>
                <button type="submit" className="btn-primary" style={{ width: 'auto' }}>Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
