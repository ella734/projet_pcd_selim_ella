import { useState, useEffect, useRef } from 'react';
import api from '../services/api';
import { Eye, Edit2, Trash2, X, Award, Download, Upload } from 'lucide-react';
import { exportRowsToExcel } from '../utils/excel';
import { sanitize } from '../schemas/validation';
import { printSection } from '../utils/print';

export default function Medecins() {
  const [medecins, setMedecins] = useState([]);
  const [services, setServices] = useState([]);
  const [hopitaux, setHopitaux] = useState([]);
  const [selectedMedecin, setSelectedMedecin] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [hopitalFilter, setHopitalFilter] = useState('ALL');
  const [serviceFilter, setServiceFilter] = useState('ALL');
  const printSectionRef = useRef(null);
  const [formData, setFormData] = useState({
    nomM: '', prenomM: '', dateNaissM: '', sexeM: '', numTelM: '',
    numTelWhatsAppM: '', adresseDomM: '', specialiteM: '', dateDernierDiplomeM: '',
    indexHopitalM: '', typeMedecin: '', serviceId: ''
  });

  useEffect(() => {
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    const isAdmin = currentUser.role?.toUpperCase() === 'ADMIN';
    const filterId = isAdmin ? null : currentUser.hopitalId;

    fetchMedecins(filterId);
    fetchServices();
    fetchHopitaux();
  }, []);

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role?.toUpperCase() === 'ADMIN';

  const fetchMedecins = async (hopitalId = null) => {
    try {
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      let url = '/medecins';

      if (currentUser.role === 'MEDECIN' && hopitalId) {
        url += `?hopitalId=${hopitalId}`;
      } else if (currentUser.role !== 'ADMIN' && currentUser.serviceId) {
        url += `?serviceId=${currentUser.serviceId}`;
      }

      const { data } = await api.get(url);
      setMedecins(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch medecins', err);
    }
  };

  const fetchServices = async () => {
    try {
      const response = await api.get('/services');
      setServices(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error('Failed to fetch services', err);
    }
  };

  const fetchHopitaux = async () => {
    try {
      const { data } = await api.get('/hopitaux');
      setHopitaux(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch hopitaux', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveMedecin = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        nomM: sanitize(formData.nomM),
        prenomM: sanitize(formData.prenomM),
        dateNaissM: formData.dateNaissM || null,
        sexeM: formData.sexeM,
        numTelM: formData.numTelM || null,
        numTelWhatsAppM: formData.numTelWhatsAppM || null,
        adresseDomM: formData.adresseDomM || null,
        specialiteM: formData.specialiteM,
        dateDernierDiplomeM: formData.dateDernierDiplomeM || null,
        indexHopitalM: formData.indexHopitalM || null,
        typeMedecin: formData.typeMedecin || null,
        service: formData.serviceId ? { identifiantS: parseInt(formData.serviceId, 10) } : null
      };

      if (editingId) {
        await api.put(`/medecins/${editingId}`, payload);
      } else {
        await api.post('/medecins', payload);
      }

      setIsAdding(false);
      setEditingId(null);
      fetchMedecins();
      setFormData({
        nomM: '', prenomM: '', dateNaissM: '', sexeM: '', numTelM: '',
        numTelWhatsAppM: '', adresseDomM: '', specialiteM: '', dateDernierDiplomeM: '',
        indexHopitalM: '', typeMedecin: '', serviceId: ''
      });
    } catch (err) {
      console.error('Failed to save medecin', err);
      const msg = err.response?.data?.message || err.response?.data?.error || "Erreur lors de l'enregistrement du médecin.";
      alert(msg);
    }
  };

  const handleEdit = (medecin) => {
    setFormData({
      nomM: medecin.nomM || '',
      prenomM: medecin.prenomM || '',
      dateNaissM: medecin.dateNaissM ? medecin.dateNaissM.split('T')[0] : '',
      sexeM: medecin.sexeM || '',
      numTelM: medecin.numTelM || '',
      numTelWhatsAppM: medecin.numTelWhatsAppM || '',
      adresseDomM: medecin.adresseDomM || '',
      specialiteM: medecin.specialiteM || '',
      dateDernierDiplomeM: medecin.dateDernierDiplomeM ? medecin.dateDernierDiplomeM.split('T')[0] : '',
      indexHopitalM: medecin.indexHopitalM || '',
      typeMedecin: medecin.typeMedecin || '',
      serviceId: medecin.service ? medecin.service.identifiantS : ''
    });
    setEditingId(medecin.identifiantM);
    setIsAdding(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet élément ?')) {
      try {
        await api.delete(`/medecins/${id}`);
        fetchMedecins();
      } catch (err) {
        console.error('Failed to delete', err);
        alert('Erreur lors de la suppression.');
      }
    }
  };

  const handleView = (medecin) => {
    setSelectedMedecin(medecin);
    setShowModal(true);
  };

  const handlePrint = () => {
    printSection({
      title: 'Médecins',
      element: printSectionRef.current,
    });
  };

  const handleDownloadExcel = () => {
    exportRowsToExcel({
      rows: medecins.map((medecin) => ({
        ID: medecin.identifiantM ?? '',
        Nom: medecin.nomM ?? '',
        Prénom: medecin.prenomM ?? '',
        'Date de naissance': medecin.dateNaissM ? medecin.dateNaissM.split('T')[0] : '',
        Sexe: medecin.sexeM ?? '',
        Téléphone: medecin.numTelM ?? '',
        WhatsApp: medecin.numTelWhatsAppM ?? '',
        Adresse: medecin.adresseDomM ?? '',
        Spécialité: medecin.specialiteM ?? '',
        'Dernier diplôme': medecin.dateDernierDiplomeM ? medecin.dateDernierDiplomeM.split('T')[0] : '',
        Hôpital: medecin.indexHopitalM ?? '',
        Type: medecin.typeMedecin ?? '',
        'ID Service': medecin.service?.identifiantS ?? '',
        Service: medecin.service?.libelleS ?? ''
      })),
      sheetName: 'Medecins',
      fileName: 'medecins'
    });
  };

  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredMedecins = medecins.filter((medecin) => {
    const matchesSearch = !normalizedSearch || [
      medecin.identifiantM,
      medecin.nomM,
      medecin.prenomM,
      medecin.specialiteM,
      medecin.numTelM,
      medecin.service?.libelleS,
      medecin.indexHopitalM,
    ].some((value) => String(value ?? '').toLowerCase().includes(normalizedSearch));

    const matchesType = typeFilter === 'ALL' || medecin.typeMedecin === typeFilter;
    const matchesHopital = hopitalFilter === 'ALL' || String(medecin.indexHopitalM ?? '') === hopitalFilter;
    const matchesService = serviceFilter === 'ALL' || String(medecin.service?.identifiantS ?? '') === serviceFilter;

    return matchesSearch && matchesType && matchesHopital && matchesService;
  });

  return (
    <div>
      <div className="no-print" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem' }}>Répertoire des médecins</h1>
        {isAdmin && (
          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
            <button className="btn-primary" style={{ width: 'auto', padding: '0.5rem 1rem' }} onClick={() => setIsAdding(true)}>
              + Nouveau médecin
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
          <div className="card-title">Personnel médical ({filteredMedecins.length})</div>
        </div>
        <div className="no-print" style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', padding: '0 1.5rem 1rem' }}>
          <input
            type="text"
            placeholder="Rechercher un médecin..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ flex: '1 1 280px', minWidth: '240px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          />
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            style={{ minWidth: '200px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les types</option>
            <option value="INVESTIGATEUR">Investigateur</option>
            <option value="SUIVI">Suivi</option>
          </select>
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
        <div className="table-container" style={{ overflowX: 'auto' }}>
          <table style={{ minWidth: '1800px' }}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom / Prénom</th>
                <th>Spécialité</th>
                <th>Sexe</th>
                <th>Date de naissance</th>
                <th>Téléphone</th>
                <th>WhatsApp</th>
                <th>Adresse</th>
                <th>Dernier diplôme</th>
                <th>Hôpital</th>
                <th>Type</th>
                <th>Service</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredMedecins.map((medecin) => (
                <tr key={medecin.identifiantM}>
                  <td>#{medecin.identifiantM}</td>
                  <td style={{ fontWeight: 600 }}>{medecin.nomM} {medecin.prenomM}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <Award size={16} color="var(--primary-color)" />
                      {medecin.specialiteM}
                    </div>
                  </td>
                  <td>{medecin.sexeM}</td>
                  <td>{medecin.dateNaissM ? new Date(medecin.dateNaissM).toLocaleDateString() : ''}</td>
                  <td>{medecin.numTelM}</td>
                  <td>{medecin.numTelWhatsAppM}</td>
                  <td>{medecin.adresseDomM}</td>
                  <td>{medecin.dateDernierDiplomeM ? new Date(medecin.dateDernierDiplomeM).toLocaleDateString() : ''}</td>
                  <td>{medecin.indexHopitalM}</td>
                  <td>
                    {medecin.typeMedecin === 'INVESTIGATEUR'
                      ? 'Médecin investigateur'
                      : medecin.typeMedecin === 'SUIVI'
                        ? 'Médecin de suivi'
                        : ''}
                  </td>
                  <td>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                      {medecin.service ? (
                        <span className="tag tag-success">{medecin.service.libelleS}</span>
                      ) : <span className="tag tag-blue" style={{ background: '#f1f5f9', color: '#64748b' }}>Aucun service</span>}
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleView(medecin)} style={{ color: 'var(--primary-color)', background: 'none' }} title="Voir le profil"><Eye size={18} /></button>
                      {isAdmin && (
                        <>
                          <button onClick={() => handleEdit(medecin)} style={{ color: 'var(--text-secondary)', background: 'none' }} title="Modifier"><Edit2 size={18} /></button>
                          <button onClick={() => handleDelete(medecin.identifiantM)} style={{ color: 'var(--danger-color)', background: 'none' }} title="Supprimer"><Trash2 size={18} /></button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filteredMedecins.length === 0 && (
                <tr><td colSpan="13" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Aucun médecin trouvé.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && selectedMedecin && (
        <div className="modal-overlay no-print" onClick={() => setShowModal(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="card-title">Fiche médecin : {selectedMedecin.nomM} {selectedMedecin.prenomM}</h2>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <div className="modal-body">
              <div className="detail-item"><span className="detail-label">Identifiant</span><span className="detail-value">#{selectedMedecin.identifiantM}</span></div>
              <div className="detail-item"><span className="detail-label">Nom</span><span className="detail-value">{selectedMedecin.nomM}</span></div>
              <div className="detail-item"><span className="detail-label">Prénom</span><span className="detail-value">{selectedMedecin.prenomM}</span></div>
              <div className="detail-item"><span className="detail-label">Spécialité</span><span className="detail-value">{selectedMedecin.specialiteM}</span></div>
              <div className="detail-item"><span className="detail-label">Date de naissance</span><span className="detail-value">{selectedMedecin.dateNaissM ? new Date(selectedMedecin.dateNaissM).toLocaleDateString() : 'N/A'}</span></div>
              <div className="detail-item"><span className="detail-label">Sexe</span><span className="detail-value">{selectedMedecin.sexeM}</span></div>
              <div className="detail-item"><span className="detail-label">Téléphone</span><span className="detail-value">{selectedMedecin.numTelM}</span></div>
              <div className="detail-item"><span className="detail-label">WhatsApp</span><span className="detail-value">{selectedMedecin.numTelWhatsAppM}</span></div>
              <div className="detail-item"><span className="detail-label">Adresse</span><span className="detail-value">{selectedMedecin.adresseDomM}</span></div>
              <div className="detail-item"><span className="detail-label">Hôpital</span><span className="detail-value">{selectedMedecin.indexHopitalM}</span></div>
              <div className="detail-item"><span className="detail-label">Type</span><span className="detail-value">{selectedMedecin.typeMedecin === 'INVESTIGATEUR' ? 'Médecin investigateur' : selectedMedecin.typeMedecin === 'SUIVI' ? 'Médecin de suivi' : 'N/A'}</span></div>
              <div className="detail-item"><span className="detail-label">Dernier diplôme</span><span className="detail-value">{selectedMedecin.dateDernierDiplomeM ? new Date(selectedMedecin.dateDernierDiplomeM).toLocaleDateString() : 'N/A'}</span></div>

              <div className="detail-item" style={{ gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                <span className="detail-label" style={{ color: 'var(--primary-color)' }}>Service</span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.5rem' }}>
                  {selectedMedecin.service ? (
                    <span className="tag tag-success" style={{ padding: '0.5rem 1rem' }}>
                      {selectedMedecin.service.libelleS}
                    </span>
                  ) : <span className="detail-value">Aucun service affecté.</span>}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-primary" style={{ width: 'auto', background: 'var(--text-secondary)' }} onClick={() => setShowModal(false)}>Fermer</button>
            </div>
          </div>
        </div>
      )}

      {isAdding && (
        <div className="modal-overlay no-print" onClick={() => setIsAdding(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="card-title">{editingId ? 'Modifier le médecin' : 'Ajouter un nouveau médecin'}</h2>
              <button onClick={() => setIsAdding(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <form onSubmit={handleSaveMedecin} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
              <div className="modal-body">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nom *</label>
                  <input type="text" name="nomM" value={formData.nomM} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Prénom *</label>
                  <input type="text" name="prenomM" value={formData.prenomM} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Spécialité *</label>
                  <input type="text" name="specialiteM" value={formData.specialiteM} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Sexe *</label>
                  <select name="sexeM" value={formData.sexeM} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">Sélectionner</option>
                    <option value="M">Masculin</option>
                    <option value="F">Féminin</option>
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Date de naissance</label>
                  <input type="date" name="dateNaissM" value={formData.dateNaissM} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Téléphone</label>
                  <input type="text" name="numTelM" value={formData.numTelM} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>WhatsApp</label>
                  <input type="text" name="numTelWhatsAppM" value={formData.numTelWhatsAppM} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Adresse</label>
                  <input type="text" name="adresseDomM" value={formData.adresseDomM} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Dernier diplôme</label>
                  <input type="date" name="dateDernierDiplomeM" value={formData.dateDernierDiplomeM} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Hôpital</label>
                  <select name="indexHopitalM" value={formData.indexHopitalM} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">-- Aucun --</option>
                    {hopitaux.map((hopital) => (
                      <option key={hopital.identifiantH} value={hopital.identifiantH}>{hopital.libelleH}</option>
                    ))}
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Type de médecin *</label>
                  <select name="typeMedecin" value={formData.typeMedecin} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">Sélectionner</option>
                    <option value="INVESTIGATEUR">Médecin investigateur</option>
                    <option value="SUIVI">Médecin de suivi</option>
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', marginTop: '0.5rem' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--primary-color)' }}>Service *</label>
                  <select name="serviceId" value={formData.serviceId} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">-- Sélectionner un service --</option>
                    {services.map((service) => (
                      <option key={service.identifiantS} value={service.identifiantS}>{service.libelleS} (Hôpital : {service.hopital?.libelleH || 'Aucun'})</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn-primary"
                  style={{ width: 'auto', background: 'var(--text-secondary)' }}
                  onClick={() => {
                    setIsAdding(false);
                    setEditingId(null);
                    setFormData({
                      nomM: '', prenomM: '', dateNaissM: '', sexeM: '', numTelM: '',
                      numTelWhatsAppM: '', adresseDomM: '', specialiteM: '', dateDernierDiplomeM: '',
                      indexHopitalM: '', typeMedecin: '', serviceId: ''
                    });
                  }}
                >
                  Annuler
                </button>
                <button type="submit" className="btn-primary" style={{ width: 'auto' }}>Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
