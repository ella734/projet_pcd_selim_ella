import { useState, useEffect, useRef } from 'react';
import api from '../services/api';
import { Building2, X, Eye, Edit2, Trash2, Download, Upload } from 'lucide-react';
import { exportRowsToExcel } from '../utils/excel';
import { validateHopital, hasErrors, sanitize } from '../schemas/validation';
import { printSection } from '../utils/print';

export default function Hopitaux() {
  // SÉCURITÉ : Accès réservé aux admins (double garde UI)
  const _user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
  if (_user.role?.toUpperCase() !== 'ADMIN') {
    return <div style={{ padding: '2rem', color: 'red' }}>Accès non autorisé.</div>;
  }
  const [hopitaux, setHopitaux] = useState([]);
  const [selected, setSelected] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [capacityFilter, setCapacityFilter] = useState('ALL');
  const [formErrors, setFormErrors] = useState({});
  const printSectionRef = useRef(null);
  const [formData, setFormData] = useState({
    libelleH: '', adresseH: '', nbBlocH: '', nbServiceH: '',
    nbLitsH: '', descriptionH: '', dateCreationH: ''
  });

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';

  useEffect(() => {
    fetchHopitaux();
  }, []);

  const fetchHopitaux = async () => {
    try {
      const { data } = await api.get('/hopitaux');
      setHopitaux(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Failed to fetch hospitals", err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveHopital = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        libelleH: formData.libelleH,
        adresseH: formData.adresseH || null,
        nbBlocH: formData.nbBlocH === '' ? null : Number(formData.nbBlocH),
        nbServiceH: formData.nbServiceH === '' ? null : Number(formData.nbServiceH),
        nbLitsH: formData.nbLitsH === '' ? null : Number(formData.nbLitsH),
        descriptionH: formData.descriptionH || null,
        dateCreationH: formData.dateCreationH || null
      };
      if (editingId) {
        await api.put(`/hopitaux/${editingId}`, payload);
      } else {
        await api.post('/hopitaux', payload);
      }
      setIsAdding(false);
      setEditingId(null);
      fetchHopitaux();
      setFormData({
        libelleH: '', adresseH: '', nbBlocH: '', nbServiceH: '',
        nbLitsH: '', descriptionH: '', dateCreationH: ''
      });
    } catch (err) {
      console.error("Failed to save hopital", err);
      alert("Erreur lors de l'enregistrement de l'hôpital : " + (err.response?.data?.message || err.response?.data?.error || err.message));
    }
  };

  const handleEdit = (hopital) => {
    setFormData({
      libelleH: hopital.libelleH || '',
      adresseH: hopital.adresseH || '',
      nbBlocH: hopital.nbBlocH || '',
      nbServiceH: hopital.nbServiceH || '',
      nbLitsH: hopital.nbLitsH || '',
      descriptionH: hopital.descriptionH || '',
      dateCreationH: hopital.dateCreationH ? hopital.dateCreationH.split('T')[0] : ''
    });
    setEditingId(hopital.identifiantH);
    setIsAdding(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm("Êtes-vous sûr de vouloir supprimer cet élément ?")) {
      try {
        await api.delete(`/hopitaux/${id}`);
        fetchHopitaux();
      } catch (err) {
        console.error("Failed to delete", err);
        alert("Erreur lors de la suppression.");
      }
    }
  };

  const handlePrint = () => {
    printSection({
      title: 'Structures Hospitalières',
      element: printSectionRef.current,
    });
  };

  const handleDownloadExcel = () => {
    exportRowsToExcel({
      rows: hopitaux.map((hopital) => ({
        ID: hopital.identifiantH ?? '',
        Désignation: hopital.libelleH ?? '',
        Adresse: hopital.adresseH ?? '',
        Blocs: hopital.nbBlocH ?? '',
        Services: hopital.nbServiceH ?? '',
        Lits: hopital.nbLitsH ?? '',
        Description: hopital.descriptionH ?? '',
        'Date de création': hopital.dateCreationH ? hopital.dateCreationH.split('T')[0] : ''
      })),
      sheetName: 'Hopitaux',
      fileName: 'hopitaux'
    });
  };

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredHopitaux = hopitaux.filter((hopital) => {
    const matchesSearch = !normalizedSearch || [
      hopital.identifiantH,
      hopital.libelleH,
      hopital.adresseH,
      hopital.descriptionH,
    ].some((value) => String(value ?? '').toLowerCase().includes(normalizedSearch));

    const matchesCapacity =
      capacityFilter === 'ALL' ||
      (capacityFilter === 'WITH_BEDS' && Number(hopital.nbLitsH || 0) > 0) ||
      (capacityFilter === 'WITHOUT_BEDS' && Number(hopital.nbLitsH || 0) === 0);

    return matchesSearch && matchesCapacity;
  });


  return (
    <div>
      <div className="no-print" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem' }}>Structures Hospitalières</h1>
        {isAdmin && (
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <button className="btn-primary" style={{ width: 'auto', padding: '0.5rem 1rem' }} onClick={() => setIsAdding(true)}>
              + Ajouter un hôpital
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
        <div className="card-header">
          <div className="card-title">Toutes les structures ({filteredHopitaux.length})</div>
        </div>
        <div className="no-print" style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', padding: '0 1.5rem 1rem' }}>
          <input
            type="text"
            placeholder="Rechercher par nom, adresse, description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ flex: '1 1 280px', minWidth: '240px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          />
          <select
            value={capacityFilter}
            onChange={(e) => setCapacityFilter(e.target.value)}
            style={{ minWidth: '220px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les hôpitaux</option>
            <option value="WITH_BEDS">Avec lits</option>
            <option value="WITHOUT_BEDS">Sans lits</option>
          </select>
        </div>
        <div className="table-container" style={{ overflowX: 'auto' }}>
          <table style={{ minWidth: '1200px' }}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Désignation</th>
                <th>Adresse</th>
                <th>Blocs</th>
                <th>Services</th>
                <th>Lits</th>
                <th>Description</th>
                <th>Date de création</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredHopitaux.map(h => (
                <tr key={h.identifiantH}>
                  <td>#{h.identifiantH}</td>
                  <td style={{ fontWeight: 600 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Building2 size={16} color="var(--primary-color)" />
                      {h.libelleH}
                    </div>
                  </td>
                  <td>{h.adresseH}</td>
                  <td>{h.nbBlocH}</td>
                  <td>{h.nbServiceH}</td>
                  <td>{h.nbLitsH}</td>
                  <td><div style={{ maxWidth: '200px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={h.descriptionH}>{h.descriptionH}</div></td>
                  <td>{h.dateCreationH ? new Date(h.dateCreationH).toLocaleDateString() : ''}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => { setSelected(h); setShowModal(true); }} style={{ color: 'var(--primary-color)', background: 'none' }} title="Détails">
                        <Eye size={18} />
                      </button>
                      {isAdmin && (
                        <>
                          <button onClick={() => handleEdit(h)} style={{ color: 'var(--text-secondary)', background: 'none' }} title="Modifier"><Edit2 size={18} /></button>
                          <button onClick={() => handleDelete(h.identifiantH)} style={{ color: 'var(--danger-color)', background: 'none' }} title="Supprimer"><Trash2 size={18} /></button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filteredHopitaux.length === 0 && (
                <tr><td colSpan="9" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Aucune structure hospitalière trouvée.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && selected && (
        <div className="modal-overlay no-print" onClick={() => setShowModal(false)}>
          <div className="modal-card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="card-title">Fiche hôpital : {selected.libelleH}</h2>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <div className="modal-body">
              <div className="detail-item"><span className="detail-label">Identifiant</span><span className="detail-value">#{selected.identifiantH}</span></div>
              <div className="detail-item"><span className="detail-label">Désignation</span><span className="detail-value">{selected.libelleH}</span></div>
              <div className="detail-item"><span className="detail-label">Adresse</span><span className="detail-value">{selected.adresseH}</span></div>
              <div className="detail-item"><span className="detail-label">Blocs</span><span className="detail-value">{selected.nbBlocH}</span></div>
              <div className="detail-item"><span className="detail-label">Services</span><span className="detail-value">{selected.nbServiceH}</span></div>
              <div className="detail-item"><span className="detail-label">Lits</span><span className="detail-value">{selected.nbLitsH}</span></div>
              <div className="detail-item"><span className="detail-label">Description</span><span className="detail-value">{selected.descriptionH}</span></div>
              <div className="detail-item"><span className="detail-label">Date de création</span><span className="detail-value">{selected.dateCreationH ? new Date(selected.dateCreationH).toLocaleDateString() : 'N/A'}</span></div>
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
              <h2 className="card-title">{editingId ? 'Modifier la structure hospitalière' : 'Ajouter une structure hospitalière'}</h2>
              <button onClick={() => setIsAdding(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <form onSubmit={handleSaveHopital} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
              <div className="modal-body">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Désignation *</label>
                  <input type="text" name="libelleH" value={formData.libelleH} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Adresse</label>
                  <input type="text" name="adresseH" value={formData.adresseH} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nombre de blocs</label>
                  <input type="number" name="nbBlocH" value={formData.nbBlocH} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nombre de services</label>
                  <input type="number" name="nbServiceH" value={formData.nbServiceH} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nombre de lits</label>
                  <input type="number" name="nbLitsH" value={formData.nbLitsH} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Date de création</label>
                  <input type="date" name="dateCreationH" value={formData.dateCreationH} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', gridColumn: '1 / -1' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Description</label>
                  <textarea name="descriptionH" value={formData.descriptionH} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px', minHeight: '80px' }}></textarea>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-primary" style={{ width: 'auto', background: 'var(--text-secondary)' }} onClick={() => { setIsAdding(false); setEditingId(null); setFormData({libelleH: '', adresseH: '', nbBlocH: '', nbServiceH: '', nbLitsH: '', descriptionH: '', dateCreationH: ''}); }}>Annuler</button>
                <button type="submit" className="btn-primary" style={{ width: 'auto' }}>Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
