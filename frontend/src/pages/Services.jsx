import { useState, useEffect, useRef } from 'react';
import api from '../services/api';
import { LayoutGrid, X, Edit2, Trash2, Download, Upload } from 'lucide-react';
import { exportRowsToExcel } from '../utils/excel';
import { validateService, hasErrors, sanitize } from '../schemas/validation';
import { printSection } from '../utils/print';

export default function Services() {
  // SÉCURITÉ : Accès réservé aux admins
  const _user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
  if (_user.role?.toUpperCase() !== 'ADMIN') {
    return <div style={{ padding: '2rem', color: 'red' }}>Accès non autorisé.</div>;
  }
  const [services, setServices] = useState([]);
  const [hopitaux, setHopitaux] = useState([]);
  const [selected, setSelected] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [hopitalFilter, setHopitalFilter] = useState('ALL');
  const [formErrors, setFormErrors] = useState({});
  const printSectionRef = useRef(null);
  const [formData, setFormData] = useState({
    libelleS: '', nbLitsS: '', nbChambresS: '', nbMedecinsS: '', hopitalId: ''
  });

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';

  useEffect(() => {
    const userStr = localStorage.getItem('user');
    const user = JSON.parse(userStr || '{}');
    const hopitalId = user.hopitalId;

    fetchServices(hopitalId);
    fetchHopitaux();
  }, []);

  const fetchServices = async (hopitalId = null) => {
    try {
      const userStr = localStorage.getItem('user');
      const user = JSON.parse(userStr || '{}');

      let url = '/services';
      if (user.role !== 'ADMIN' && hopitalId) {
        url += `?hopitalId=${hopitalId}`;
      }

      const { data } = await api.get(url);
      setServices(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to fetch services", err);
    }
  };

  const fetchHopitaux = async () => {
    try {
      const response = await api.get('/hopitaux');
      setHopitaux(response.data);
    } catch (err) {
      console.error("Failed to fetch hopitaux", err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveService = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        libelleS: formData.libelleS,
        nbLitsS: formData.nbLitsS === '' ? null : Number(formData.nbLitsS),
        nbChambresS: formData.nbChambresS === '' ? null : Number(formData.nbChambresS),
        nbMedecinsS: formData.nbMedecinsS === '' ? null : Number(formData.nbMedecinsS)
      };
      if (editingId) {
        await api.put(`/services/${editingId}`, payload);
      } else {
        await api.post(`/services/hopital/${formData.hopitalId}`, payload);
      }
      setIsAdding(false);
      setEditingId(null);
      fetchServices();
      setFormData({
        libelleS: '', nbLitsS: '', nbChambresS: '', nbMedecinsS: '', hopitalId: ''
      });
    } catch (err) {
      console.error("Failed to save service", err);
      alert("Erreur lors de l'enregistrement du service.");
    }
  };

  const handleEdit = (service) => {
    setFormData({
      libelleS: service.libelleS || '',
      nbLitsS: service.nbLitsS || '',
      nbChambresS: service.nbChambresS || '',
      nbMedecinsS: service.nbMedecinsS || '',
      hopitalId: service.hopital ? service.hopital.identifiantH : ''
    });
    setEditingId(service.identifiantS);
    setIsAdding(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm("Êtes-vous sûr de vouloir supprimer cet élément ?")) {
      try {
        await api.delete(`/services/${id}`);
        fetchServices();
      } catch (err) {
        console.error("Failed to delete", err);
        alert("Erreur lors de la suppression.");
      }
    }
  };

  const handlePrint = () => {
    printSection({
      title: 'Services Médicaux',
      element: printSectionRef.current,
    });
  };

  const handleDownloadExcel = () => {
    exportRowsToExcel({
      rows: services.map((service) => ({
        ID: service.identifiantS ?? '',
        Désignation: service.libelleS ?? '',
        Lits: service.nbLitsS ?? '',
        Chambres: service.nbChambresS ?? '',
        Médecins: service.nbMedecinsS ?? '',
        'ID Hôpital': service.hopital?.identifiantH ?? '',
        Hôpital: service.hopital?.libelleH ?? ''
      })),
      sheetName: 'Services',
      fileName: 'services'
    });
  };

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredServices = services.filter((service) => {
    const matchesSearch = !normalizedSearch || [
      service.identifiantS,
      service.libelleS,
      service.hopital?.libelleH,
      service.nbLitsS,
      service.nbChambresS,
      service.nbMedecinsS,
    ].some((value) => String(value ?? '').toLowerCase().includes(normalizedSearch));

    const matchesHopital =
      hopitalFilter === 'ALL' ||
      String(service.hopital?.identifiantH ?? '') === hopitalFilter;

    return matchesSearch && matchesHopital;
  });


  return (
    <div>
      <div className="no-print" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem' }}>Services Médicaux</h1>
        {isAdmin && (
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <button className="btn-primary" style={{ width: 'auto', padding: '0.5rem 1rem' }} onClick={() => setIsAdding(true)}>
              + Nouveau service
            </button>
            <button className="btn-primary" type="button" style={{ width: 'auto', padding: '0.5rem 1rem', background: '#0f766e' }} onClick={handlePrint}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Download size={16} /> Imprimer</span>
            </button>
            <button className="btn-primary" type="button" style={{ width: 'auto', padding: '0.5rem 1rem', background: '#1d4ed8' }} onClick={handleDownloadExcel}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Download size={16} /> Exporter Excel</span>
            </button>
          </div>
        )}
      </div>

      <div className="card print-section" ref={printSectionRef}>
        <div className="no-print" style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', padding: '1.5rem 1.5rem 0' }}>
          <input
            type="text"
            placeholder="Rechercher un service..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ flex: '1 1 280px', minWidth: '240px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          />
          <select
            value={hopitalFilter}
            onChange={(e) => setHopitalFilter(e.target.value)}
            style={{ minWidth: '220px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les hôpitaux</option>
            {hopitaux.map((hopital) => (
              <option key={hopital.identifiantH} value={String(hopital.identifiantH)}>
                {hopital.libelleH}
              </option>
            ))}
          </select>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Désignation</th>
                <th>Lits</th>
                <th>Chambres</th>
                <th>Médecins</th>
                <th>Hôpital</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredServices.map(s => (
                <tr key={s.identifiantS}>
                  <td>#{s.identifiantS}</td>
                  <td style={{ fontWeight: 600 }}>{s.libelleS}</td>
                  <td>{s.nbLitsS}</td>
                  <td>{s.nbChambresS}</td>
                  <td>{s.nbMedecinsS}</td>
                  <td>
                    {s.hopital ? (
                      <span className="tag tag-blue">{s.hopital.libelleH}</span>
                    ) : (
                      <span className="tag tag-blue" style={{background:'#f1f5f9', color:'#64748b'}}>Aucun hôpital</span>
                    )}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => { setSelected(s); setShowModal(true); }} style={{ color: 'var(--primary-color)', background: 'none' }} title="Détails">
                        <LayoutGrid size={18} />
                      </button>
                      {isAdmin && (
                        <>
                          <button onClick={() => handleEdit(s)} style={{ color: 'var(--text-secondary)', background: 'none' }} title="Modifier"><Edit2 size={18} /></button>
                          <button onClick={() => handleDelete(s.identifiantS)} style={{ color: 'var(--danger-color)', background: 'none' }} title="Supprimer"><Trash2 size={18} /></button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filteredServices.length === 0 && (
                <tr><td colSpan="7" style={{ textAlign: 'center', padding: '3rem' }}>Aucun service médical enregistré.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && selected && (
        <div className="modal-overlay no-print" onClick={() => setShowModal(false)}>
          <div className="modal-card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="card-title">Fiche service : {selected.libelleS}</h2>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <div className="modal-body">
              <div className="detail-item"><span className="detail-label">Identifiant</span><span className="detail-value">#{selected.identifiantS}</span></div>
              <div className="detail-item"><span className="detail-label">Désignation</span><span className="detail-value">{selected.libelleS}</span></div>
              <div className="detail-item"><span className="detail-label">Lits</span><span className="detail-value">{selected.nbLitsS}</span></div>
              <div className="detail-item"><span className="detail-label">Chambres</span><span className="detail-value">{selected.nbChambresS}</span></div>
              <div className="detail-item"><span className="detail-label">Médecins</span><span className="detail-value">{selected.nbMedecinsS}</span></div>

              <div className="detail-item" style={{ gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                 <span className="detail-label" style={{ color: 'var(--primary-color)' }}>Hôpital rattaché</span>
                 <div style={{ marginTop: '0.5rem' }}>
                    {selected.hopital ? (
                      <span className="tag tag-blue" style={{ padding: '0.5rem 1rem' }}>
                        {selected.hopital.libelleH} (ID: #{selected.hopital.identifiantH})
                      </span>
                    ) : <span className="detail-value">Aucun hôpital rattaché.</span>}
                 </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-primary" style={{ width: 'auto' }} onClick={() => setShowModal(false)}>Fermer</button>
            </div>
          </div>
        </div>
      )}

      {isAdding && (
        <div className="modal-overlay no-print" onClick={() => setIsAdding(false)}>
          <div className="modal-card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="card-title">{editingId ? 'Modifier le service' : 'Ajouter un service médical'}</h2>
              <button onClick={() => setIsAdding(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <form onSubmit={handleSaveService} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
              <div className="modal-body">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Désignation *</label>
                  <input type="text" name="libelleS" value={formData.libelleS} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nombre de lits</label>
                  <input type="number" name="nbLitsS" value={formData.nbLitsS} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nombre de chambres</label>
                  <input type="number" name="nbChambresS" value={formData.nbChambresS} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nombre de médecins</label>
                  <input type="number" name="nbMedecinsS" value={formData.nbMedecinsS} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', marginTop: '0.5rem' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--primary-color)' }}>Hôpital *</label>
                  <select name="hopitalId" value={formData.hopitalId} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">-- Sélectionner un hôpital --</option>
                    {hopitaux.map(h => (
                      <option key={h.identifiantH} value={h.identifiantH}>{h.libelleH}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-primary" style={{ width: 'auto', background: 'var(--text-secondary)' }} onClick={() => { setIsAdding(false); setEditingId(null); setFormData({libelleS: '', nbLitsS: '', nbChambresS: '', nbMedecinsS: '', hopitalId: ''}); }}>Annuler</button>
                <button type="submit" className="btn-primary" style={{ width: 'auto' }}>Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
