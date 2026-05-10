import { useEffect, useRef, useState } from 'react';
import { Download, Edit2, Eye, Trash2, Upload, X } from 'lucide-react';
import api from '../services/api';
import { exportRowsToExcel, importRowsFromExcel, parseBoolean, parseDate, parseNumber } from '../utils/excel';
import { validatePatient, hasErrors, sanitize } from '../schemas/validation';
import { printSection } from '../utils/print';
import GraphePatient from '../components/GraphePatient';
import RecommandationsKdigo from '../components/RecommandationsKdigo';

export default function Patients() {
  const [patients, setPatients] = useState([]);
  const [services, setServices] = useState([]);
  const [medecins, setMedecins] = useState([]);
  const [hopitaux, setHopitaux] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sexeFilter, setSexeFilter] = useState('ALL');
  const [adulteFilter, setAdulteFilter] = useState('ALL');
  const [hopitalFilter, setHopitalFilter] = useState('ALL');
  const [serviceFilter, setServiceFilter] = useState('ALL');
  const [creationError, setCreationError] = useState(null);
  const [importFeedback, setImportFeedback] = useState(null);
  const [isImporting, setIsImporting] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const printSectionRef = useRef(null);
  const fileInputRef = useRef(null);
  const [formData, setFormData] = useState({
    nomP: '',
    prenomP: '',
    nationaliteP: '',
    sexeP: '',
    origineGeogP: '',
    adresseP: '',
    telephoneP: '',
    adressEmailP: '',
    telephoneWhatsAppP: '',
    dateNaissP: '',
    personneAcontacterP: '',
    typeCarnetP: '',
    numCarnetP: '',
    indexHopitalP: '',
    adulteP: true,
    investigateurId: '',
    serviceId: '',
    medecinId: '',
  });

  const currentUser = (() => {
    try { return JSON.parse(localStorage.getItem('user') || '{}'); }
    catch { return {}; }
  })();
  const currentRole = currentUser.role?.toUpperCase();
  const canCreatePatient = ['ADMIN', 'MEDECIN', 'MEDECIN_INVESTIGATEUR'].includes(currentRole);

  useEffect(() => {
    fetchPatients();
    fetchServices();
    fetchMedecins();
    fetchHopitaux();
  }, []);

  const fetchPatients = async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const role = user.role?.toUpperCase();
      let url = '/patients';

      if (role === 'MEDECIN' && user.hopitalId) {
        url += `?hopitalId=${user.hopitalId}`;
      }

      const { data } = await api.get(url);
      setPatients(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to fetch patients', err);
    }
  };

  const fetchServices = async () => {
    try {
      const response = await api.get('/services');
      setServices(response.data);
    } catch (err) {
      console.error('Failed to fetch services', err);
    }
  };

  const fetchMedecins = async () => {
    try {
      const response = await api.get('/medecins');
      setMedecins(response.data);
    } catch (err) {
      console.error('Failed to fetch medecins', err);
    }
  };

  const fetchHopitaux = async () => {
    try {
      const response = await api.get('/hopitaux');
      setHopitaux(response.data);
    } catch (err) {
      console.error('Failed to fetch hopitaux', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
      ...(name === 'indexHopitalP' ? { serviceId: '' } : {}),
    }));
  };

  const resetForm = () => {
    setFormData({
      nomP: '',
      prenomP: '',
      nationaliteP: '',
      sexeP: '',
      origineGeogP: '',
      adresseP: '',
      telephoneP: '',
      adressEmailP: '',
      telephoneWhatsAppP: '',
      dateNaissP: '',
      personneAcontacterP: '',
      typeCarnetP: '',
      numCarnetP: '',
      indexHopitalP: '',
      adulteP: true,
      investigateurId: '',
      serviceId: '',
      medecinId: '',
    });
  };

  const handleSavePatient = async (e) => {
    e.preventDefault();
    setCreationError(null);

    const errors = validatePatient({
      nomP: formData.nomP,
      prenomP: formData.prenomP,
      sexeP: formData.sexeP,
      adressEmailP: formData.adressEmailP,
      telephoneP: formData.telephoneP,
    });
    if (hasErrors(errors)) {
      setFormErrors(errors);
      return;
    }
    setFormErrors({});

    try {
      const payload = {
        nomP: sanitize(formData.nomP),
        prenomP: sanitize(formData.prenomP),
        nationaliteP: formData.nationaliteP || null,
        sexeP: formData.sexeP,
        origineGeogP: formData.origineGeogP || null,
        adresseP: formData.adresseP || null,
        telephoneP: formData.telephoneP || null,
        adressEmailP: formData.adressEmailP || null,
        telephoneWhatsAppP: formData.telephoneWhatsAppP || null,
        dateNaissP: formData.dateNaissP || null,
        personneAcontacterP: formData.personneAcontacterP || null,
        typeCarnetP: formData.typeCarnetP || null,
        numCarnetP: formData.numCarnetP || null,
        indexHopitalP: formData.indexHopitalP ? String(formData.indexHopitalP) : null,
        adulteP: formData.adulteP,
        medecinInvestigateur: formData.investigateurId
          ? { identifiantM: parseInt(formData.investigateurId, 10) }
          : null,
        affectations: formData.serviceId
          ? [{ service: { identifiantS: parseInt(formData.serviceId, 10) } }]
          : [],
        medecins: formData.medecinId
          ? [{ identifiantM: parseInt(formData.medecinId, 10) }]
          : [],
      };

      if (editingId) {
        await api.put(`/patients/${editingId}`, payload);
      } else {
        await api.post('/patients', payload);
      }

      setIsAdding(false);
      setEditingId(null);
      resetForm();
      fetchPatients();
    } catch (err) {
      console.error('Failed to save patient', err);
      setCreationError(err?.response?.data?.message || err.message || 'Erreur inattendue');
    }
  };

  const handleEdit = (patient) => {
    setFormData({
      nomP: patient.nomP || '',
      prenomP: patient.prenomP || '',
      nationaliteP: patient.nationaliteP || '',
      sexeP: patient.sexeP || '',
      origineGeogP: patient.origineGeogP || '',
      adresseP: patient.adresseP || '',
      telephoneP: patient.telephoneP || '',
      adressEmailP: patient.adressEmailP || '',
      telephoneWhatsAppP: patient.telephoneWhatsAppP || '',
      dateNaissP: patient.dateNaissP ? patient.dateNaissP.split('T')[0] : '',
      personneAcontacterP: patient.personneAcontacterP || '',
      typeCarnetP: patient.typeCarnetP || '',
      numCarnetP: patient.numCarnetP || '',
      indexHopitalP: patient.indexHopitalP || '',
      adulteP: patient.adulteP || false,
      investigateurId: patient.medecinInvestigateur?.identifiantM || '',
      serviceId: patient.affectations?.[0]?.service?.identifiantS || '',
      medecinId: patient.medecins?.[0]?.identifiantM || '',
    });
    setEditingId(patient.identifiantP);
    setIsAdding(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet élément ?')) {
      try {
        await api.delete(`/patients/${id}`);
        fetchPatients();
      } catch (err) {
        console.error('Failed to delete', err);
        alert('Erreur lors de la suppression.');
      }
    }
  };

  const handleView = (patient) => {
    setSelectedPatient(patient);
    setShowModal(true);
  };

  const handlePrint = () => {
    printSection({
      title: 'Patients',
      element: printSectionRef.current,
    });
  };

  const handleDownloadExcel = () => {
    exportRowsToExcel({
      rows: patients.map((patient) => ({
        ID: patient.identifiantP ?? '',
        Nom: patient.nomP ?? '',
        Prénom: patient.prenomP ?? '',
        Nationalité: patient.nationaliteP ?? '',
        Sexe: patient.sexeP ?? '',
        Origine: patient.origineGeogP ?? '',
        Adresse: patient.adresseP ?? '',
        Téléphone: patient.telephoneP ?? '',
        Email: patient.adressEmailP ?? '',
        WhatsApp: patient.telephoneWhatsAppP ?? '',
        'Date de naissance': patient.dateNaissP ? patient.dateNaissP.split('T')[0] : '',
        'Personne à contacter': patient.personneAcontacterP ?? '',
        'Type carnet': patient.typeCarnetP ?? '',
        'N° carnet': patient.numCarnetP ?? '',
        Hôpital: patient.indexHopitalP ?? '',
        Adulte: patient.adulteP ? 'Oui' : 'Non',
        InvestigateurId: patient.medecinInvestigateur?.identifiantM ?? '',
        SuiviMedecinId: patient.medecins?.[0]?.identifiantM ?? '',
        ServiceId: patient.affectations?.[0]?.service?.identifiantS ?? '',
      })),
      sheetName: 'Patients',
      fileName: 'patients',
    });
  };

  const getCellValue = (row, keys) => {
    for (const key of keys) {
      if (row[key] !== undefined && row[key] !== null && String(row[key]).trim() !== '') {
        return row[key];
      }
    }
    return '';
  };

  const buildPatientPayloadFromRow = (row) => {
    // Keys listed in priority order: French display name first (from export), then field variants
    const serviceId = parseNumber(getCellValue(row, ['ServiceId', 'serviceId', 'service_id']));
    const hopitalId = getCellValue(row, ['Hôpital', 'Hopital', 'IndexHopitalP', 'indexHopitalP', 'HopitalId', 'hopitalId']);
    const investigateurId = parseNumber(getCellValue(row, ['InvestigateurId', 'investigateurId']));
    const medecinId = parseNumber(getCellValue(row, ['SuiviMedecinId', 'medecinId', 'MedecinId']));

    if (!serviceId) {
      throw new Error('ServiceId est obligatoire');
    }

    return {
      nomP: sanitize(getCellValue(row, ['Nom', 'NomP', 'nomP'])),
      prenomP: sanitize(getCellValue(row, ['Prénom', 'Prenom', 'PrenomP', 'prenomP'])),
      nationaliteP: getCellValue(row, ['Nationalité', 'Nationalite', 'NationaliteP', 'nationaliteP']) || null,
      sexeP: getCellValue(row, ['Sexe', 'SexeP', 'sexeP']) || null,
      origineGeogP: getCellValue(row, ['Origine', 'OrigineGeogP', 'origineGeogP']) || null,
      adresseP: getCellValue(row, ['Adresse', 'AdresseP', 'adresseP']) || null,
      telephoneP: getCellValue(row, ['Téléphone', 'Telephone', 'TelephoneP', 'telephoneP']) || null,
      adressEmailP: getCellValue(row, ['Email', 'AdressEmailP', 'adressEmailP']) || null,
      telephoneWhatsAppP: getCellValue(row, ['WhatsApp', 'TelephoneWhatsAppP', 'telephoneWhatsAppP']) || null,
      dateNaissP: parseDate(getCellValue(row, ['Date de naissance', 'DateNaissP', 'dateNaissP'])),
      personneAcontacterP: getCellValue(row, ['Personne à contacter', 'Personne a contacter', 'PersonneAcontacterP', 'personneAcontacterP']) || null,
      typeCarnetP: getCellValue(row, ['Type carnet', 'TypeCarnetP', 'typeCarnetP']) || null,
      numCarnetP: getCellValue(row, ['N° carnet', 'N carnet', 'NumCarnetP', 'numCarnetP']) || null,
      indexHopitalP: hopitalId ? String(hopitalId) : null,
      adulteP: parseBoolean(getCellValue(row, ['Adulte', 'AdulteP', 'adulteP'])),
      medecinInvestigateur: investigateurId ? { identifiantM: investigateurId } : null,
      affectations: [{ service: { identifiantS: serviceId } }],
      medecins: medecinId ? [{ identifiantM: medecinId }] : [],
    };
  };

  const handleImportExcel = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setImportFeedback(null);
    setIsImporting(true);

    try {
      const rows = await importRowsFromExcel(file);
      const nonEmptyRows = rows.filter((row) =>
        Object.values(row).some((value) => String(value ?? '').trim() !== '')
      );

      if (nonEmptyRows.length === 0) {
        throw new Error('Le fichier Excel est vide.');
      }

      let successCount = 0;
      const failures = [];

      for (let index = 0; index < nonEmptyRows.length; index += 1) {
        const row = nonEmptyRows[index];

        try {
          const payload = buildPatientPayloadFromRow(row);
          const validationErrors = validatePatient({
            nomP: payload.nomP,
            prenomP: payload.prenomP,
            sexeP: payload.sexeP,
            adressEmailP: payload.adressEmailP,
            telephoneP: payload.telephoneP,
          });

          if (hasErrors(validationErrors)) {
            throw new Error(Object.values(validationErrors).filter(Boolean).join(', '));
          }

          await api.post('/patients', payload);
          successCount += 1;
        } catch (error) {
          failures.push(`Ligne ${index + 2}: ${error?.response?.data?.message || error.message || 'Erreur inconnue'}`);
        }
      }

      await fetchPatients();

      if (successCount === 0) {
        throw new Error(failures.join(' | '));
      }

      setImportFeedback({
        type: failures.length ? 'warning' : 'success',
        message: failures.length
          ? `${successCount} ligne(s) importée(s). ${failures.length} ligne(s) rejetée(s): ${failures.slice(0, 3).join(' | ')}`
          : `${successCount} ligne(s) importée(s) avec succès.`,
      });
    } catch (error) {
      setImportFeedback({
        type: 'error',
        message: error.message || 'Import Excel impossible.',
      });
    } finally {
      setIsImporting(false);
      event.target.value = '';
    }
  };

  const filteredServices = services.filter((service) => {
    if (!formData.indexHopitalP) return true;
    return String(service.hopital?.identifiantH || service.idHopital || '') === String(formData.indexHopitalP);
  });

  const investigateurs = medecins.filter((medecin) => medecin.typeMedecin === 'INVESTIGATEUR');
  const medecinsSuivi = medecins.filter((medecin) => medecin.typeMedecin === 'SUIVI');
  const normalizedSearch = searchTerm.trim().toLowerCase();
  const displayedPatients = patients.filter((patient) => {
    const matchesSearch = !normalizedSearch || [
      patient.identifiantP,
      patient.nomP,
      patient.prenomP,
      patient.nationaliteP,
      patient.origineGeogP,
      patient.adresseP,
      patient.telephoneP,
      patient.adressEmailP,
      patient.numCarnetP,
      patient.indexHopitalP,
      patient.medecinInvestigateur?.nomM,
      patient.medecinInvestigateur?.prenomM,
      patient.affectations?.map((affectation) => affectation.service?.libelleS).join(' '),
    ].some((value) => String(value ?? '').toLowerCase().includes(normalizedSearch));

    const matchesSexe = sexeFilter === 'ALL' || patient.sexeP === sexeFilter;
    const matchesAdulte =
      adulteFilter === 'ALL' ||
      (adulteFilter === 'ADULTE' && patient.adulteP) ||
      (adulteFilter === 'ENFANT' && !patient.adulteP);
    const matchesHopital = hopitalFilter === 'ALL' || String(patient.indexHopitalP ?? '') === hopitalFilter;
    const matchesService =
      serviceFilter === 'ALL' ||
      patient.affectations?.some((affectation) => String(affectation.service?.identifiantS ?? '') === serviceFilter);

    return matchesSearch && matchesSexe && matchesAdulte && matchesHopital && matchesService;
  });

  return (
    <div>
      <div className="no-print" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.5rem' }}>Répertoire des patients</h1>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <input ref={fileInputRef} type="file" accept=".xlsx,.xls" onChange={handleImportExcel} style={{ display: 'none' }} />
          {canCreatePatient && (
            <button className="btn-primary" style={{ width: 'auto', padding: '0.5rem 1rem' }} onClick={() => setIsAdding(true)}>+ Nouveau patient</button>
          )}
          <button className="btn-primary" type="button" style={{ width: 'auto', padding: '0.5rem 1rem', background: '#0f766e' }} onClick={handlePrint}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Download size={16} /> Imprimer</span>
          </button>
          <button className="btn-primary" type="button" style={{ width: 'auto', padding: '0.5rem 1rem', background: '#1d4ed8' }} onClick={() => fileInputRef.current?.click()} disabled={isImporting}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Upload size={16} /> {isImporting ? 'Import en cours...' : 'Importer Excel'}</span>
          </button>
          <button className="btn-primary" type="button" style={{ width: 'auto', padding: '0.5rem 1rem', background: '#475569' }} onClick={handleDownloadExcel}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Download size={16} /> Exporter Excel</span>
          </button>
        </div>
      </div>

      {importFeedback && (
        <div className="no-print" style={{ marginBottom: '1rem', padding: '0.75rem 1rem', borderRadius: '8px', background: importFeedback.type === 'success' ? '#dcfce7' : importFeedback.type === 'warning' ? '#fef3c7' : '#fee2e2', color: importFeedback.type === 'success' ? '#166534' : importFeedback.type === 'warning' ? '#92400e' : '#991b1b' }}>
          {importFeedback.message}
        </div>
      )}

      <div className="card print-section" ref={printSectionRef}>
        <div className="card-header">
          <div className="card-title">Tous les patients ({displayedPatients.length})</div>
        </div>
        <div className="no-print" style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', padding: '0 1.5rem 1rem' }}>
          <input
            type="text"
            placeholder="Rechercher un patient..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ flex: '1 1 280px', minWidth: '240px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          />
          <select
            value={sexeFilter}
            onChange={(e) => setSexeFilter(e.target.value)}
            style={{ minWidth: '160px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les sexes</option>
            <option value="M">Masculin</option>
            <option value="F">Féminin</option>
          </select>
          <select
            value={adulteFilter}
            onChange={(e) => setAdulteFilter(e.target.value)}
            style={{ minWidth: '160px', padding: '0.75rem 1rem', border: '1px solid var(--border-color)', borderRadius: '8px' }}
          >
            <option value="ALL">Tous les âges</option>
            <option value="ADULTE">Adultes</option>
            <option value="ENFANT">Enfants</option>
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
          <table style={{ minWidth: '2700px' }}>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom / Prénom</th>
                <th>Sexe</th>
                <th>Nationalité</th>
                <th>Origine</th>
                <th>Adresse</th>
                <th>Téléphone</th>
                <th>Email</th>
                <th>WhatsApp</th>
                <th>Date de naissance</th>
                <th>Personne à contacter</th>
                <th>Type carnet</th>
                <th>N° carnet</th>
                <th>Hôpital</th>
                <th>Adulte</th>
                <th>Médecin investigateur</th>
                <th>Service / Hôpital</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayedPatients.map((p) => (
                <tr key={p.identifiantP}>
                  <td>#{p.identifiantP}</td>
                  <td style={{ fontWeight: 600 }}>{p.nomP} {p.prenomP}</td>
                  <td>{p.sexeP}</td>
                  <td>{p.nationaliteP}</td>
                  <td>{p.origineGeogP}</td>
                  <td>{p.adresseP}</td>
                  <td>{p.telephoneP}</td>
                  <td>{p.adressEmailP}</td>
                  <td>{p.telephoneWhatsAppP}</td>
                  <td>{p.dateNaissP ? new Date(p.dateNaissP).toLocaleDateString() : ''}</td>
                  <td>{p.personneAcontacterP}</td>
                  <td>{p.typeCarnetP}</td>
                  <td>{p.numCarnetP}</td>
                  <td>{p.indexHopitalP}</td>
                  <td><span className={`tag ${p.adulteP ? 'tag-success' : 'tag-blue'}`}>{p.adulteP ? 'Oui' : 'Non'}</span></td>
                  <td>{p.medecinInvestigateur ? `Dr. ${p.medecinInvestigateur.nomM || ''} ${p.medecinInvestigateur.prenomM || ''}`.trim() : 'N/A'}</td>
                  <td>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                      {p.affectations?.length > 0 ? (
                        p.affectations.map((a) => (
                          <span key={a.service?.identifiantS || Math.random()} className="tag tag-success" title={`Affecté le : ${a.dateAffectation ? new Date(a.dateAffectation).toLocaleDateString() : 'N/A'}`}>
                            {a.service?.libelleS || 'Service inconnu'} ({a.service?.hopital?.libelleH || 'Sans hôpital'})
                          </span>
                        ))
                      ) : <span className="tag tag-blue" style={{ background: '#f1f5f9', color: '#64748b' }}>Aucun service</span>}
                      {p.medecins?.length > 0 && p.medecins.map((m) => (
                        <span key={m.identifiantM} className="tag tag-purple">Dr. {m?.nomM || 'Inconnu'}</span>
                      ))}
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleView(p)} style={{ color: 'var(--primary-color)', background: 'none' }} title="Voir les détails"><Eye size={18} /></button>
                      <button onClick={() => handleEdit(p)} style={{ color: 'var(--text-secondary)', background: 'none' }} title="Modifier"><Edit2 size={18} /></button>
                      <button onClick={() => handleDelete(p.identifiantP)} style={{ color: 'var(--danger-color)', background: 'none' }} title="Supprimer"><Trash2 size={18} /></button>
                    </div>
                  </td>
                </tr>
              ))}
              {displayedPatients.length === 0 && (
                <tr><td colSpan="18" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>Aucun patient trouvé.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && selectedPatient && (
        <div className="modal-overlay no-print" onClick={() => setShowModal(false)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '800px', width: '90%' }}>
            <div className="modal-header">
              <h2 className="card-title">Fiche patient : {selectedPatient.nomP} {selectedPatient.prenomP}</h2>
              <button onClick={() => setShowModal(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <div className="modal-body">
              <div className="detail-item"><span className="detail-label">Identifiant</span><span className="detail-value">#{selectedPatient.identifiantP}</span></div>
              <div className="detail-item"><span className="detail-label">Nom</span><span className="detail-value">{selectedPatient.nomP}</span></div>
              <div className="detail-item"><span className="detail-label">Prénom</span><span className="detail-value">{selectedPatient.prenomP}</span></div>
              <div className="detail-item"><span className="detail-label">Nationalité</span><span className="detail-value">{selectedPatient.nationaliteP}</span></div>
              <div className="detail-item"><span className="detail-label">Sexe</span><span className="detail-value">{selectedPatient.sexeP}</span></div>
              <div className="detail-item"><span className="detail-label">Adulte</span><span className="detail-value">{selectedPatient.adulteP ? 'Oui' : 'Non'}</span></div>

              {/* Interactions médicamenteuses Neo4j */}
              <div style={{ gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', marginTop: '0.5rem' }}>
                <h3 style={{ color: '#c0392b', marginBottom: '1rem', fontSize: '1rem' }}>
                  ⚠️ Interactions médicamenteuses (Neo4j)
                </h3>
                <GraphePatient patientId={String(selectedPatient.identifiantP)} />
              </div>

              {/* Recommandations KDIGO Neo4j */}
              <div style={{ gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem', marginTop: '0.5rem' }}>
                <h3 style={{ color: '#1B4F8A', marginBottom: '0.5rem', fontSize: '1rem' }}>
                  📋 Recommandations KDIGO 2022 (Graphe de connaissances)
                </h3>
                <RecommandationsKdigo patientId={String(selectedPatient.identifiantP)} />
              </div>

              <div className="detail-item" style={{ gridColumn: '1 / -1', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                <span className="detail-label" style={{ color: 'var(--primary-color)' }}>Affectations cliniques</span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', marginTop: '0.5rem' }}>
                  {selectedPatient.affectations?.length > 0 ? (
                    selectedPatient.affectations.map((a) => (
                      <div key={a.service?.identifiantS || Math.random()} style={{ padding: '0.75rem', background: '#f8fafc', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                        <span style={{ fontWeight: 600, color: 'var(--primary-color)' }}>{a.service?.libelleS || 'Service inconnu'}</span>
                        <span style={{ fontSize: '0.875rem' }}>Structure : {a.service?.hopital?.libelleH || 'N/A'}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Affecté le : {a.dateAffectation ? new Date(a.dateAffectation).toLocaleDateString() : 'N/A'}</span>
                      </div>
                    ))
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
              <h2 className="card-title">{editingId ? 'Modifier le patient' : 'Ajouter un nouveau patient'}</h2>
              <button onClick={() => setIsAdding(false)} style={{ background: 'none', color: 'var(--text-muted)' }}><X size={24} /></button>
            </div>
            <form onSubmit={handleSavePatient} style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
              <div className="modal-body">
                {creationError && <div className="error-message">{creationError}</div>}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nom *</label>
                  <input type="text" name="nomP" value={formData.nomP} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Prénom *</label>
                  <input type="text" name="prenomP" value={formData.prenomP} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Sexe *</label>
                  <select name="sexeP" value={formData.sexeP} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">Sélectionner</option>
                    <option value="M">Masculin</option>
                    <option value="F">Féminin</option>
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Date de naissance</label>
                  <input type="date" name="dateNaissP" value={formData.dateNaissP} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Nationalité</label>
                  <input type="text" name="nationaliteP" value={formData.nationaliteP} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Adresse</label>
                  <input type="text" name="adresseP" value={formData.adresseP} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Téléphone</label>
                  <input type="text" name="telephoneP" value={formData.telephoneP} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500 }}>Email</label>
                  <input type="email" name="adressEmailP" value={formData.adressEmailP} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }} />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <input type="checkbox" name="adulteP" checked={formData.adulteP} onChange={handleInputChange} id="adulteP" style={{ width: '1.2rem', height: '1.2rem' }} />
                  <label htmlFor="adulteP" style={{ fontSize: '0.875rem', fontWeight: 500 }}>Patient adulte</label>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--primary-color)' }}>Médecin investigateur</label>
                  <select name="investigateurId" value={formData.investigateurId} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">-- Sélectionner --</option>
                    {investigateurs.map((m) => (
                      <option key={m.identifiantM} value={m.identifiantM}>Dr. {m.nomM} {m.prenomM}</option>
                    ))}
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--primary-color)' }}>Service *</label>
                  <select name="serviceId" value={formData.serviceId} onChange={handleInputChange} required style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">-- Sélectionner un service --</option>
                    {filteredServices.map((s) => (
                      <option key={s.identifiantS} value={s.identifiantS}>{s.libelleS}</option>
                    ))}
                  </select>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                  <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--primary-color)' }}>Médecin de suivi</label>
                  <select name="medecinId" value={formData.medecinId} onChange={handleInputChange} style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: '4px' }}>
                    <option value="">-- Sélectionner --</option>
                    {medecinsSuivi.map((m) => (
                      <option key={m.identifiantM} value={m.identifiantM}>Dr. {m.nomM} {m.prenomM}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-primary" style={{ width: 'auto', background: 'var(--text-secondary)' }} onClick={() => { setIsAdding(false); setEditingId(null); resetForm(); }}>Annuler</button>
                <button type="submit" className="btn-primary" style={{ width: 'auto' }}>Enregistrer</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
