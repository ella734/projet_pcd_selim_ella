import { useEffect, useMemo, useState } from 'react';
import {
  Activity,
  Briefcase,
  ClipboardList,
  Download,
  Edit2,
  HeartPulse,
  Pill,
  Shield,
  Trash2,
  UserRound,
} from 'lucide-react';
import { exportRowsToExcel } from '../utils/excel';
import api from '../services/api';
import GestionMedicaments from '../components/GestionMedicaments';

const BLOOD_GROUPS = ['', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
const SEXES = ['', 'M', 'F'];

const tabs = [
  { key: 'identification', label: 'Identification', icon: UserRound },
  { key: 'antecedents', label: 'Antecedents medicaux', icon: ClipboardList },
  { key: 'transplantation', label: 'Transplantation', icon: HeartPulse },
  { key: 'traitements', label: 'Traitements', icon: Pill },
  { key: 'suivi', label: 'Suivi post-greffe', icon: Activity },
  { key: 'social', label: 'Profil social', icon: Briefcase },
  { key: 'administration', label: 'Administration', icon: Shield },
];

const initialFormData = {
  patient: { identifiantP: '' },
  identifiantContact: '',
  numero: '',
  serviceOrigine: '',
  dateTr: '',
  lieuTr: '',
  lieuDeSuivi: '',
  typeDonneur: '',
  ageDonneur: '',
  sexeDonneur: '',
  grpSanguinDonneur: '',
  ageReceveur: '',
  sexeReceveur: '',
  grpSanguinReceveur: '',
  hlaA1Donneur: '',
  hlaA2Donneur: '',
  hlaB1Donneur: '',
  hlaB2Donneur: '',
  hlaDr1Donneur: '',
  hlaDr2Donneur: '',
  hlaA1Receveur: '',
  hlaA2Receveur: '',
  hlaB1Receveur: '',
  hlaB2Receveur: '',
  hlaDr1Receveur: '',
  hlaDr2Receveur: '',
  diabetePreTr: false,
  htaPreTr: false,
  agHbsPreTr: '',
  anticorpsHvcPreTr: '',
  transfusionPreTr: false,
  accPreTr: '',
  nephropathieInitiale: '',
  etiologieIrc: '',
  modaliteEer: '',
  dateDebutEer: '',
  delaiTrMois: '',
  antecedentMedical: '',
  antecedentChirurgical: '',
  typeDialyse: '',
  habitudes: '',
  allergies: '',
  dateGreffe: '',
  descriptionGreffe: '',
  bilanReceveur: '',
  bilanDonneur: '',
  nbTransplantaion: '',
  nbUretre: '',
  rein: '',
  nbArteresVeines: '',
  kystes: false,
  ischemieFroideH: '',
  ischemieChaudeMin: '',
  liquideDeConservation: '',
  liquideDeRincage: '',
  machineAPerfusion: false,
  typeAnastomoseArterielle: '',
  typeAnastomoseVeineuse: '',
  typeAnastomoseUreteroVesicale: '',
  sondeEnDoubleJ: false,
  induction: false,
  typeInduction: '',
  corticoides: false,
  mmf: false,
  azathioprine: false,
  tacrolimus: false,
  ciclosporineA: false,
  sirolimus: false,
  dciTis: '',
  dureeTraitementTis: '',
  tisInduction: '',
  tisEntretien: '',
  medicament: '',
  effetSecondaire: '',
  dateCreationEvolution: '',
  duree1ereHospitJ: '',
  retardRepriseFct: false,
  creatinineM3: '',
  dfgMdrdM3: '',
  iraPrecoceM3: false,
  complicationUro: false,
  infection1ereAnnee: false,
  infectionUrinaire1ereAnnee: false,
  infectionCmv1ereAnnee: false,
  rejetAigu1ereAnnee: false,
  nbreHospitalisations1ereAnnee: '',
  dateDernieresNvl: '',
  vivantGreffon: false,
  retourDialyse: false,
  dcAvecGreffon: false,
  pdv: false,
  typeHospitalisation: '',
  dateEntreeHospitalisation: '',
  dateSortieHospitalisation: '',
  statutSocioEconomique: '',
  profession: '',
  niveauEducation: '',
  enEtatActivite: false,
  assurance: '',
  medecinId: '',
  serviceId: '',
  hopitalId: '',
  userId: '',
};

const fieldStyle = {
  padding: '0.58rem 0.65rem',
  border: '1px solid var(--border-color)',
  borderRadius: 6,
  width: '100%',
  background: 'white',
};

const labelStyle = {
  fontSize: '0.78rem',
  fontWeight: 700,
  color: 'var(--text-secondary)',
  display: 'block',
  marginBottom: 5,
};

function Field({ label, name, value, onChange, type = 'text', options, requiredMcd = false, placeholder = '' }) {
  return (
    <label>
      <span style={labelStyle}>
        {label}
        {requiredMcd && <span style={{ color: 'var(--danger-color)' }}> *</span>}
      </span>
      {options ? (
        <select style={fieldStyle} name={name} value={value ?? ''} onChange={onChange}>
          {options.map((option) => (
            <option key={option} value={option}>
              {option || '--'}
            </option>
          ))}
        </select>
      ) : (
        <input style={fieldStyle} type={type} name={name} value={value ?? ''} onChange={onChange} placeholder={placeholder} />
      )}
    </label>
  );
}

function CheckboxField({ label, name, checked, onChange }) {
  return (
    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.88rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
      <input type="checkbox" name={name} checked={Boolean(checked)} onChange={onChange} style={{ width: 16, height: 16 }} />
      {label}
    </label>
  );
}

function Section({ title, subtitle, children }) {
  return (
    <section style={{ border: '1px solid var(--border-color)', borderRadius: 8, background: '#fff', overflow: 'hidden' }}>
      <div style={{ padding: '0.9rem 1rem', borderBottom: '1px solid var(--border-color)', background: '#f8fafc' }}>
        <h3 style={{ fontSize: '1rem', marginBottom: subtitle ? 3 : 0 }}>{title}</h3>
        {subtitle && <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>{subtitle}</p>}
      </div>
      <div style={{ padding: '1rem', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.9rem' }}>
        {children}
      </div>
    </section>
  );
}

function ValueItem({ label, value }) {
  return (
    <div style={{ padding: '0.65rem 0.75rem', border: '1px solid var(--border-color)', borderRadius: 6, background: '#f8fafc' }}>
      <div style={{ color: 'var(--text-muted)', fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase' }}>{label}</div>
      <div style={{ marginTop: 3, fontWeight: 600 }}>{value || 'Non renseigne'}</div>
    </div>
  );
}

function buildAnalysePayload(formData) {
  const clean = (value) => {
    if (value === null || value === undefined) return null;
    if (typeof value === 'object') {
      const result = {};
      for (const [k, v] of Object.entries(value)) {
        result[k] = clean(v);
      }
      return result;
    }
    if (value === '') return null;
    return value;
  };
  const payload = clean(formData);
  if (!payload.patient?.identifiantP) {
    payload.patient = null;
  }
  return payload;
}

export default function Transplantations() {
  const [patients, setPatients] = useState([]);
  const [medecins, setMedecins] = useState([]);
  const [services, setServices] = useState([]);
  const [hopitaux, setHopitaux] = useState([]);
  const [users, setUsers] = useState([]);
  const [transplantations, setTransplantations] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [activeTab, setActiveTab] = useState('identification');
  const [analyse, setAnalyse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedPatientId, setSelectedPatientId] = useState(null);
  const [historique, setHistorique] = useState([]);
  const [showHistorique, setShowHistorique] = useState(false);
  const [showMedicaments, setShowMedicaments] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState(initialFormData);

  useEffect(() => {
    api.get('/patients').then(({ data }) => setPatients(Array.isArray(data) ? data : [])).catch(console.error);
    api.get('/medecins').then(({ data }) => setMedecins(Array.isArray(data) ? data : [])).catch(console.error);
    api.get('/services').then(({ data }) => setServices(Array.isArray(data) ? data : [])).catch(console.error);
    api.get('/hopitaux').then(({ data }) => setHopitaux(Array.isArray(data) ? data : [])).catch(console.error);
    api.get('/users').then(({ data }) => setUsers(Array.isArray(data) ? data : [])).catch(() => setUsers([]));
  }, []);

  const fetchTransplantations = () => {
    api.get('/transplantations')
      .then(({ data }) => setTransplantations(Array.isArray(data) ? data : []))
      .catch(console.error);
  };

  useEffect(() => {
    fetchTransplantations();
  }, []);

  const selectedPatient = useMemo(
    () => patients.find((patient) => String(patient.identifiantP) === String(formData.patient.identifiantP)),
    [patients, formData.patient.identifiantP]
  );

  const selectedService = selectedPatient?.affectations?.[0]?.service;
  const selectedMedecin = selectedPatient?.medecins?.[0] || selectedPatient?.medecinInvestigateur;

  const formatDate = (value) => (value ? String(value).split('T')[0] : '');
  const formatBoolean = (value) => (value === null || value === undefined ? '' : value ? 'true' : 'false');

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    if (name === 'patientId') {
      const patient = patients.find((item) => String(item.identifiantP) === String(value));
      setSelectedPatientId(value || null);
      setFormData((previous) => ({
        ...previous,
        patient: { identifiantP: value ? parseInt(value, 10) : '' },
        sexeReceveur: patient?.sexeP || previous.sexeReceveur,
        medecinId: patient?.medecins?.[0]?.identifiantM || patient?.medecinInvestigateur?.identifiantM || '',
        serviceId: patient?.affectations?.[0]?.service?.identifiantS || '',
        hopitalId: patient?.affectations?.[0]?.service?.hopital?.identifiantH || patient?.indexHopitalP || '',
        statutSocioEconomique: patient?.statut || '',
        niveauEducation: patient?.niveauEducation || '',
        enEtatActivite: Boolean(patient?.enEtatActivite),
      }));
      return;
    }

    setFormData((previous) => ({
      ...previous,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setAnalyse(null);
    try {
      if (editingId) {
        await api.put(`/transplantations/${editingId}`, formData);
      } else {
        await api.post('/transplantations', formData);
      }
      setEditingId(null);
      fetchTransplantations();
    } catch (error) {
      console.error('Erreur sauvegarde:', error);
      alert(`Erreur lors de l'enregistrement: ${error.response?.data?.error || error.response?.data?.message || error.message}`);
      setLoading(false);
      return;
    }

    try {
      const analysePayload = buildAnalysePayload(formData);
      const { data } = await api.post('/ia/analyser', analysePayload);
      setAnalyse(data);
    } catch (iaError) {
      console.warn('Analyse IA non disponible:', iaError);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (transplantation) => {
    setEditingId(transplantation.numeroTr || transplantation.id);
    setShowForm(true);
    setActiveTab('identification');
    setAnalyse(null);
    setSelectedPatientId(transplantation.patient?.identifiantP || null);
    setFormData({
      ...initialFormData,
      patient: { identifiantP: transplantation.patient?.identifiantP || '' },
      numero: String(transplantation.numeroTr || transplantation.id || transplantation.numero || ''),
      dateTr: formatDate(transplantation.dateTr),
      lieuTr: transplantation.lieuTr || transplantation.lieuDeLaGreffe || '',
      lieuDeSuivi: transplantation.lieuDeSuivi || '',
      nbTransplantaion: transplantation.nbTransplantaion || '',
      nbUretre: transplantation.nbUretre || '',
      rein: transplantation.rein || '',
      nbArteresVeines: transplantation.nbArteresVeines || '',
      kystes: Boolean(transplantation.kystes),
      liquideDeConservation: transplantation.liquideDeConservation || '',
      liquideDeRincage: transplantation.liquideDeRincage || '',
      machineAPerfusion: Boolean(transplantation.machineAPerfusion),
      typeAnastomoseArterielle: transplantation.typeAnastomoseArterielle || '',
      typeAnastomoseVeineuse: transplantation.typeAnastomoseVeineuse || '',
      typeAnastomoseUreteroVesicale: transplantation.typeAnastomoseUreteroVesicale || '',
      sondeEnDoubleJ: Boolean(transplantation.sondeEnDoubleJ),
      medecinId: transplantation.patient?.medecins?.[0]?.identifiantM || transplantation.patient?.medecinInvestigateur?.identifiantM || '',
      serviceId: transplantation.patient?.affectations?.[0]?.service?.identifiantS || '',
      hopitalId: transplantation.patient?.affectations?.[0]?.service?.hopital?.identifiantH || transplantation.patient?.indexHopitalP || '',
      statutSocioEconomique: transplantation.patient?.statut || '',
      niveauEducation: transplantation.patient?.niveauEducation || '',
      enEtatActivite: Boolean(transplantation.patient?.enEtatActivite),
    });
  };

  const handleDelete = async (transplantation) => {
    const id = transplantation.numeroTr || transplantation.id;
    if (!id) return;
    if (!window.confirm('Supprimer cette transplantation ?')) return;
    try {
      await api.delete(`/transplantations/${id}`);
      if (editingId === id) {
        setEditingId(null);
        setFormData(initialFormData);
      }
      fetchTransplantations();
    } catch (error) {
      alert(`Erreur suppression: ${error.response?.data?.message || error.response?.data?.error || error.message}`);
    }
  };

  const voirHistorique = async (patientId) => {
    if (!patientId) return;
    try {
      const { data } = await api.get(`/transplantations/historique/${patientId}`);
      setHistorique(Array.isArray(data) ? data : []);
      setSelectedPatientId(patientId);
      setShowHistorique(true);
      setShowMedicaments(false);
    } catch {
      alert("Erreur lors du chargement de l'historique");
    }
  };

  const voirMedicaments = (patientId) => {
    if (!patientId) return;
    setSelectedPatientId(patientId);
    setShowMedicaments(true);
    setShowHistorique(false);
  };

  const handleDownloadExcel = () => {
    exportRowsToExcel({
      rows: transplantations.map((transplantation) => ({
        NumeroTr: transplantation.numeroTr ?? transplantation.id ?? '',
        PatientId: transplantation.patient?.identifiantP ?? '',
        PatientNom: transplantation.patient?.nomP ?? '',
        PatientPrenom: transplantation.patient?.prenomP ?? '',
        DateTr: formatDate(transplantation.dateTr),
        LieuTr: transplantation.lieuTr ?? transplantation.lieuDeLaGreffe ?? '',
        TypeDonneur: transplantation.typeDonneur ?? '',
        AgeDonneur: transplantation.ageDonneur ?? '',
        SexeDonneur: transplantation.sexeDonneur ?? '',
        GrpSanguinDonneur: transplantation.grpSanguinDonneur ?? '',
        AgeReceveur: transplantation.ageReceveur ?? '',
        SexeReceveur: transplantation.sexeReceveur ?? '',
        GrpSanguinReceveur: transplantation.grpSanguinReceveur ?? '',
        DiabetePreTr: formatBoolean(transplantation.diabetePreTr),
        HtaPreTr: formatBoolean(transplantation.htaPreTr),
        IschemieFroideH: transplantation.ischemieFroideH ?? '',
        IschemieChaudeMin: transplantation.ischemieChaudeMin ?? '',
        Tacrolimus: formatBoolean(transplantation.tacrolimus),
        CiclosporineA: formatBoolean(transplantation.ciclosporineA),
        CreatinineM3: transplantation.creatinineM3 ?? '',
        DfgMdrdM3: transplantation.dfgMdrdM3 ?? '',
        RejetAigu1ereAnnee: formatBoolean(transplantation.rejetAigu1ereAnnee),
        RetourDialyse: formatBoolean(transplantation.retourDialyse),
        VivantGreffon: formatBoolean(transplantation.vivantGreffon),
      })),
      sheetName: 'Transplantations',
      fileName: 'transplantations',
    });
  };

  const renderTabContent = () => {
    if (activeTab === 'identification') {
      return (
        <>
          <Section title="Patient-IdAdmin" subtitle="Le patient reste le point d'ancrage de toute la fiche.">
            <Field label="IdentifiantP" name="patientId" value={formData.patient.identifiantP} onChange={handleChange} options={['', ...patients.map((patient) => String(patient.identifiantP))]} requiredMcd />
            <ValueItem label="NomP" value={selectedPatient?.nomP} />
            <ValueItem label="PrenomP" value={selectedPatient?.prenomP} />
            <ValueItem label="SexeP" value={selectedPatient?.sexeP} />
            <ValueItem label="DateNaissP" value={formatDate(selectedPatient?.dateNaissP)} />
            <ValueItem label="NumeroCIN" value={selectedPatient?.numeroCin} />
            <ValueItem label="Statut" value={selectedPatient?.statut} />
            <ValueItem label="Evolution" value={selectedPatient?.evolution} />
          </Section>
          <Section title="IdentificationEtContacts">
            <Field label="Identifiant contact" name="identifiantContact" value={formData.identifiantContact} onChange={handleChange} requiredMcd />
            <ValueItem label="AdresseP" value={selectedPatient?.adresseP} />
            <ValueItem label="TelephoneP" value={selectedPatient?.telephoneP} />
            <ValueItem label="AdressEmailP" value={selectedPatient?.adressEmailP} />
            <ValueItem label="TelephoneWhatsAppP" value={selectedPatient?.telephoneWhatsAppP} />
            <ValueItem label="PersonneAcontacterP" value={selectedPatient?.personneAcontacterP} />
            <ValueItem label="TypeCarnetP" value={selectedPatient?.typeCarnetP} />
            <ValueItem label="NumCarnetP" value={selectedPatient?.numCarnetP} />
          </Section>
        </>
      );
    }

    if (activeTab === 'antecedents') {
      return (
        <>
          <Section title="Nephropathie initiale et comorbidite">
            <Field label="IdentifiantNI" name="identifiantNi" value={formData.identifiantNi} onChange={handleChange} requiredMcd />
            <Field label="Nephropathie initiale" name="nephropathieInitiale" value={formData.nephropathieInitiale} onChange={handleChange} />
            <Field label="Etiologie IRC" name="etiologieIrc" value={formData.etiologieIrc} onChange={handleChange} />
            <Field label="Modalite EER" name="modaliteEer" value={formData.modaliteEer} onChange={handleChange} />
            <Field label="Date debut EER" name="dateDebutEer" type="date" value={formData.dateDebutEer} onChange={handleChange} />
            <Field label="Delai transplantation (mois)" name="delaiTrMois" type="number" value={formData.delaiTrMois} onChange={handleChange} />
            <CheckboxField label="Diabete pre-TR" name="diabetePreTr" checked={formData.diabetePreTr} onChange={handleChange} />
            <CheckboxField label="HTA pre-TR" name="htaPreTr" checked={formData.htaPreTr} onChange={handleChange} />
          </Section>
          <Section title="Antecedents, dialyse, habitudes et allergies">
            <Field label="Antecedents medicaux" name="antecedentMedical" value={formData.antecedentMedical} onChange={handleChange} />
            <Field label="Antecedents chirurgicaux" name="antecedentChirurgical" value={formData.antecedentChirurgical} onChange={handleChange} />
            <Field label="Type dialyse" name="typeDialyse" value={formData.typeDialyse} onChange={handleChange} />
            <Field label="Ag HBs pre-TR" name="agHbsPreTr" value={formData.agHbsPreTr} onChange={handleChange} />
            <Field label="Anticorps HVC pre-TR" name="anticorpsHvcPreTr" value={formData.anticorpsHvcPreTr} onChange={handleChange} />
            <Field label="ACC pre-TR" name="accPreTr" value={formData.accPreTr} onChange={handleChange} />
            <Field label="Habitudes" name="habitudes" value={formData.habitudes} onChange={handleChange} />
            <Field label="Allergies" name="allergies" value={formData.allergies} onChange={handleChange} />
            <CheckboxField label="Transfusion pre-TR" name="transfusionPreTr" checked={formData.transfusionPreTr} onChange={handleChange} />
          </Section>
        </>
      );
    }

    if (activeTab === 'transplantation') {
      return (
        <>
          <Section title="Greffe, donneur et receveur">
            <Field label="IdentifiantG" name="identifiantG" value={formData.identifiantG} onChange={handleChange} requiredMcd />
            <Field label="Date greffe" name="dateGreffe" type="date" value={formData.dateGreffe} onChange={handleChange} />
            <Field label="Description greffe" name="descriptionGreffe" value={formData.descriptionGreffe} onChange={handleChange} />
            <Field label="Type donneur" name="typeDonneur" value={formData.typeDonneur} onChange={handleChange} />
            <Field label="Age donneur" name="ageDonneur" type="number" value={formData.ageDonneur} onChange={handleChange} />
            <Field label="Sexe donneur" name="sexeDonneur" value={formData.sexeDonneur} onChange={handleChange} options={SEXES} />
            <Field label="Groupe sanguin donneur" name="grpSanguinDonneur" value={formData.grpSanguinDonneur} onChange={handleChange} options={BLOOD_GROUPS} />
            <Field label="Age receveur" name="ageReceveur" type="number" value={formData.ageReceveur} onChange={handleChange} />
            <Field label="Sexe receveur" name="sexeReceveur" value={formData.sexeReceveur} onChange={handleChange} options={SEXES} />
            <Field label="Groupe sanguin receveur" name="grpSanguinReceveur" value={formData.grpSanguinReceveur} onChange={handleChange} options={BLOOD_GROUPS} />
          </Section>
          <Section title="BilanGreffe, BilanGreffeDonneur et Transplantation">
            <Field label="NumeroTR" name="numero" value={formData.numero} onChange={handleChange} requiredMcd />
            <Field label="DateTR" name="dateTr" type="date" value={formData.dateTr} onChange={handleChange} />
            <Field label="Lieu de la greffe" name="lieuTr" value={formData.lieuTr} onChange={handleChange} />
            <Field label="Lieu de suivi" name="lieuDeSuivi" value={formData.lieuDeSuivi} onChange={handleChange} />
            <Field label="Bilan receveur" name="bilanReceveur" value={formData.bilanReceveur} onChange={handleChange} />
            <Field label="Bilan donneur" name="bilanDonneur" value={formData.bilanDonneur} onChange={handleChange} />
            <Field label="Rein" name="rein" value={formData.rein} onChange={handleChange} />
            <Field label="Nb uretere" name="nbUretre" value={formData.nbUretre} onChange={handleChange} />
            <Field label="Nb arteres/veines" name="nbArteresVeines" value={formData.nbArteresVeines} onChange={handleChange} />
            <Field label="Ischemie froide (h)" name="ischemieFroideH" type="number" value={formData.ischemieFroideH} onChange={handleChange} />
            <Field label="Ischemie chaude (min)" name="ischemieChaudeMin" type="number" value={formData.ischemieChaudeMin} onChange={handleChange} />
            <Field label="Liquide de conservation" name="liquideDeConservation" value={formData.liquideDeConservation} onChange={handleChange} />
            <Field label="Liquide de rincage" name="liquideDeRincage" value={formData.liquideDeRincage} onChange={handleChange} />
            <Field label="Anastomose arterielle" name="typeAnastomoseArterielle" value={formData.typeAnastomoseArterielle} onChange={handleChange} />
            <Field label="Anastomose veineuse" name="typeAnastomoseVeineuse" value={formData.typeAnastomoseVeineuse} onChange={handleChange} />
            <Field label="Anastomose uretero-vesicale" name="typeAnastomoseUreteroVesicale" value={formData.typeAnastomoseUreteroVesicale} onChange={handleChange} />
            <CheckboxField label="Kystes" name="kystes" checked={formData.kystes} onChange={handleChange} />
            <CheckboxField label="Machine a perfusion" name="machineAPerfusion" checked={formData.machineAPerfusion} onChange={handleChange} />
            <CheckboxField label="Sonde en double J" name="sondeEnDoubleJ" checked={formData.sondeEnDoubleJ} onChange={handleChange} />
          </Section>
        </>
      );
    }

    if (activeTab === 'traitements') {
      return (
        <>
          <Section title="Traitement immunosuppresseur">
            <Field label="IdentifiantTIS" name="identifiantTis" value={formData.identifiantTis} onChange={handleChange} requiredMcd />
            <Field label="DCI TIS" name="dciTis" value={formData.dciTis} onChange={handleChange} />
            <Field label="Duree traitement TIS" name="dureeTraitementTis" value={formData.dureeTraitementTis} onChange={handleChange} />
            <Field label="TIS induction" name="tisInduction" value={formData.tisInduction} onChange={handleChange} />
            <Field label="Type induction" name="typeInduction" value={formData.typeInduction} onChange={handleChange} />
            <Field label="TIS entretien" name="tisEntretien" value={formData.tisEntretien} onChange={handleChange} />
            <CheckboxField label="Induction" name="induction" checked={formData.induction} onChange={handleChange} />
            <CheckboxField label="Corticoides" name="corticoides" checked={formData.corticoides} onChange={handleChange} />
            <CheckboxField label="MMF" name="mmf" checked={formData.mmf} onChange={handleChange} />
            <CheckboxField label="Azathioprine" name="azathioprine" checked={formData.azathioprine} onChange={handleChange} />
            <CheckboxField label="Tacrolimus" name="tacrolimus" checked={formData.tacrolimus} onChange={handleChange} />
            <CheckboxField label="Ciclosporine A" name="ciclosporineA" checked={formData.ciclosporineA} onChange={handleChange} />
            <CheckboxField label="Sirolimus" name="sirolimus" checked={formData.sirolimus} onChange={handleChange} />
          </Section>
          <Section title="Medicament et effet secondaire">
            <Field label="IdentifiantMed" name="identifiantMed" value={formData.identifiantMed} onChange={handleChange} requiredMcd />
            <Field label="Medicament" name="medicament" value={formData.medicament} onChange={handleChange} />
            <Field label="IdentifiantEFS" name="identifiantEfs" value={formData.identifiantEfs} onChange={handleChange} requiredMcd />
            <Field label="Effet secondaire" name="effetSecondaire" value={formData.effetSecondaire} onChange={handleChange} />
          </Section>
        </>
      );
    }

    if (activeTab === 'suivi') {
      return (
        <>
          <Section title="EvolutionRenale">
            <Field label="DateCreation" name="dateCreationEvolution" type="date" value={formData.dateCreationEvolution} onChange={handleChange} requiredMcd />
            <Field label="Creatinine M3 (umol/L)" name="creatinineM3" type="number" value={formData.creatinineM3} onChange={handleChange} />
            <Field label="DFG MDRD M3 (mL/min)" name="dfgMdrdM3" type="number" value={formData.dfgMdrdM3} onChange={handleChange} />
            <Field label="Date dernieres nouvelles" name="dateDernieresNvl" type="date" value={formData.dateDernieresNvl} onChange={handleChange} />
            <Field label="Nombre hospitalisations 1ere annee" name="nbreHospitalisations1ereAnnee" type="number" value={formData.nbreHospitalisations1ereAnnee} onChange={handleChange} />
            <CheckboxField label="Retard reprise fonction" name="retardRepriseFct" checked={formData.retardRepriseFct} onChange={handleChange} />
            <CheckboxField label="IRA precoce M3" name="iraPrecoceM3" checked={formData.iraPrecoceM3} onChange={handleChange} />
            <CheckboxField label="Complication urologique" name="complicationUro" checked={formData.complicationUro} onChange={handleChange} />
            <CheckboxField label="Infection 1ere annee" name="infection1ereAnnee" checked={formData.infection1ereAnnee} onChange={handleChange} />
            <CheckboxField label="Infection urinaire" name="infectionUrinaire1ereAnnee" checked={formData.infectionUrinaire1ereAnnee} onChange={handleChange} />
            <CheckboxField label="Infection CMV" name="infectionCmv1ereAnnee" checked={formData.infectionCmv1ereAnnee} onChange={handleChange} />
            <CheckboxField label="Rejet aigu" name="rejetAigu1ereAnnee" checked={formData.rejetAigu1ereAnnee} onChange={handleChange} />
            <CheckboxField label="Vivant avec greffon" name="vivantGreffon" checked={formData.vivantGreffon} onChange={handleChange} />
            <CheckboxField label="Retour dialyse" name="retourDialyse" checked={formData.retourDialyse} onChange={handleChange} />
            <CheckboxField label="Deces avec greffon" name="dcAvecGreffon" checked={formData.dcAvecGreffon} onChange={handleChange} />
            <CheckboxField label="Perdu de vue" name="pdv" checked={formData.pdv} onChange={handleChange} />
          </Section>
          <Section title="HospitalisationPostTransplantation">
            <Field label="Duree 1ere hospitalisation (j)" name="duree1ereHospitJ" type="number" value={formData.duree1ereHospitJ} onChange={handleChange} />
            <Field label="Type hospitalisation" name="typeHospitalisation" value={formData.typeHospitalisation} onChange={handleChange} />
            <Field label="Date entree" name="dateEntreeHospitalisation" type="date" value={formData.dateEntreeHospitalisation} onChange={handleChange} />
            <Field label="Date sortie" name="dateSortieHospitalisation" type="date" value={formData.dateSortieHospitalisation} onChange={handleChange} />
          </Section>
        </>
      );
    }

    if (activeTab === 'social') {
      return (
        <Section title="StatutSocio-EconomiqeEtProfessionnel et Assurance">
          <Field label="Statut socio-economique" name="statutSocioEconomique" value={formData.statutSocioEconomique} onChange={handleChange} />
          <Field label="Profession" name="profession" value={formData.profession} onChange={handleChange} />
          <Field label="Niveau education" name="niveauEducation" value={formData.niveauEducation} onChange={handleChange} />
          <Field label="Assurance" name="assurance" value={formData.assurance} onChange={handleChange} />
          <CheckboxField label="En etat d'activite" name="enEtatActivite" checked={formData.enEtatActivite} onChange={handleChange} />
        </Section>
      );
    }

    return (
      <>
        <Section title="Medecin, Service et Hopital-StructureSoin">
          <Field label="IdentifiantM" name="medecinId" value={formData.medecinId} onChange={handleChange} options={['', ...medecins.map((medecin) => String(medecin.identifiantM))]} requiredMcd />
          <Field label="IdentifiantS" name="serviceId" value={formData.serviceId} onChange={handleChange} options={['', ...services.map((service) => String(service.identifiantS))]} requiredMcd />
          <Field label="IdentifiantH" name="hopitalId" value={formData.hopitalId} onChange={handleChange} options={['', ...hopitaux.map((hopital) => String(hopital.identifiantH))]} requiredMcd />
          <ValueItem label="Medecin courant" value={selectedMedecin ? `Dr. ${selectedMedecin.nomM || ''} ${selectedMedecin.prenomM || ''}`.trim() : ''} />
          <ValueItem label="Service courant" value={selectedService?.libelleS} />
          <ValueItem label="Hopital courant" value={selectedService?.hopital?.libelleH || selectedPatient?.indexHopitalP} />
        </Section>
        <Section title="User">
          <Field label="User ID" name="userId" value={formData.userId} onChange={handleChange} options={['', ...users.map((user) => String(user.id || user.identifiantUser || user.username))]} requiredMcd />
          <ValueItem label="Compte connecte" value={(() => {
            try { return JSON.parse(localStorage.getItem('user') || '{}').username; }
            catch { return ''; }
          })()} />
        </Section>
      </>
    );
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', gap: '1rem', flexWrap: 'wrap' }}>
        <div>
          <h1 style={{ fontSize: '1.5rem' }}>Transplantations rénales</h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: 4 }}>
            Suivi des fiches de transplantation rénale et des données cliniques associées.
          </p>
        </div>
        <button
          className="btn-primary"
          style={{ width: 'auto', padding: '0.55rem 1.2rem' }}
              onClick={() => { setShowForm(!showForm); setAnalyse(null); }}
        >
          {showForm ? 'Fermer la fiche' : '+ Nouvelle fiche patient'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ border: '1px solid var(--border-color)', borderRadius: 8, background: 'white', padding: '1rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(240px, 360px) 1fr', gap: '1rem', alignItems: 'end' }}>
              <Field label="Patient central - IdentifiantP" name="patientId" value={formData.patient.identifiantP} onChange={handleChange} options={['', ...patients.map((patient) => String(patient.identifiantP))]} requiredMcd />
              <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                <span className="tag tag-blue">Patient : {selectedPatient ? `${selectedPatient.nomP || ''} ${selectedPatient.prenomP || ''}`.trim() : 'non sélectionné'}</span>
                <span className="tag tag-success">Champs facultatifs sauf identifiants et DateCreation</span>
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const active = tab.key === activeTab;
              return (
                <button
                  key={tab.key}
                  type="button"
                  onClick={() => setActiveTab(tab.key)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8,
                    border: '1px solid var(--border-color)',
                    background: active ? 'var(--primary-color)' : 'white',
                    color: active ? 'white' : 'var(--text-primary)',
                    padding: '0.55rem 0.75rem',
                    borderRadius: 6,
                    fontWeight: 700,
                    fontSize: '0.86rem',
                  }}
                >
                  <Icon size={16} />
                  {tab.label}
                </button>
              );
            })}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>{renderTabContent()}</div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', flexWrap: 'wrap' }}>
            <button
              type="button"
              className="btn-primary"
              style={{ width: 'auto', padding: '0.7rem 1.2rem', background: 'var(--text-secondary)' }}
              onClick={() => { setFormData(initialFormData); setActiveTab('identification'); setEditingId(null); }}
            >
              Réinitialiser
            </button>
            <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '0.7rem 1.4rem' }} disabled={loading}>
              {loading ? 'Analyse en cours...' : editingId ? 'Mettre à jour + analyser' : 'Enregistrer + analyser'}
            </button>
          </div>
        </form>
      )}

      {analyse && (
        <div style={{ marginTop: '2rem', padding: '1.25rem', border: '2px solid var(--primary-color)', borderRadius: 8, background: '#f0f9ff' }}>
          <h2 style={{ color: 'var(--primary-color)', marginBottom: '1rem', fontSize: '1.15rem' }}>Résultat de l'analyse clinique</h2>
          <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
            <ValueItem label="Score de risque" value={analyse.scoreRisque === undefined ? '' : `${analyse.scoreRisque}/100`} />
            <ValueItem label="Niveau de risque" value={analyse.niveauRisque} />
          </div>
          {analyse.alertes?.length > 0 && analyse.alertes.map((alerte, index) => (
            <div key={index} style={{ padding: '0.55rem 0.75rem', background: '#fee2e2', borderRadius: 6, marginBottom: '0.5rem', color: '#991b1b' }}>
              {alerte}
            </div>
          ))}
          {analyse.recommandations?.length > 0 && analyse.recommandations.map((recommandation, index) => (
            <div key={index} style={{ padding: '0.55rem 0.75rem', background: '#dbeafe', borderRadius: 6, marginBottom: '0.5rem', color: '#1e3a5f' }}>
              {recommandation}
            </div>
          ))}
        </div>
      )}

      <div className="card" style={{ marginTop: '2rem' }}>
        <div className="card-header">
          <div className="card-title">Liste des transplantations ({transplantations.length})</div>
          <button onClick={handleDownloadExcel} className="btn-primary" style={{ width: 'auto', padding: '0.5rem 0.9rem', display: 'flex', alignItems: 'center', gap: 8, background: '#0f766e' }}>
            <Download size={16} /> Exporter Excel
          </button>
        </div>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>N° dossier</th>
                <th>Patient</th>
                <th>Date</th>
                <th>Lieu</th>
                <th>DFG M3</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {transplantations.map((transplantation, index) => (
                <tr key={transplantation.numeroTr || transplantation.id || index}>
                  <td>#{transplantation.numeroTr || transplantation.id || transplantation.numero || index + 1}</td>
                  <td>{transplantation.patient ? `${transplantation.patient.nomP || ''} ${transplantation.patient.prenomP || ''}`.trim() : 'Non renseigné'}</td>
                  <td>{formatDate(transplantation.dateTr)}</td>
                  <td>{transplantation.lieuTr || transplantation.lieuDeLaGreffe || ''}</td>
                  <td>{transplantation.dfgMdrdM3 ? `${transplantation.dfgMdrdM3} mL/min` : ''}</td>
                  <td>
                    {transplantation.retourDialyse ? 'Dialyse'
                      : transplantation.vivantGreffon ? 'Greffon OK'
                      : transplantation.dcAvecGreffon ? 'Décès'
                      : transplantation.pdv ? 'Perdu de vue'
                      : ''}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      <button type="button" onClick={() => voirHistorique(transplantation.patient?.identifiantP)} style={{ background: '#1d4ed8', color: 'white', borderRadius: 6, padding: '0.35rem 0.6rem' }}>
                        Historique
                      </button>
                      <button type="button" onClick={() => voirMedicaments(transplantation.patient?.identifiantP)} style={{ background: '#b91c1c', color: 'white', borderRadius: 6, padding: '0.35rem 0.6rem' }}>
                        Médicaments
                      </button>
                      <button type="button" onClick={() => handleEdit(transplantation)} title="Modifier" style={{ background: '#f8fafc', color: 'var(--primary-color)', border: '1px solid var(--border-color)', borderRadius: 6, padding: '0.35rem 0.5rem' }}>
                        <Edit2 size={15} />
                      </button>
                      <button type="button" onClick={() => handleDelete(transplantation)} title="Supprimer" style={{ background: '#fee2e2', color: '#991b1b', border: '1px solid #fecaca', borderRadius: 6, padding: '0.35rem 0.5rem' }}>
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {transplantations.length === 0 && (
                <tr><td colSpan="7" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>Aucune transplantation enregistrée.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showHistorique && selectedPatientId && (
        <div style={{ marginTop: '2rem', padding: '1.25rem', border: '1px solid var(--border-color)', borderRadius: 8, background: 'white' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h2 style={{ fontSize: '1.1rem' }}>Historique patient #{selectedPatientId}</h2>
            <button type="button" onClick={() => setShowHistorique(false)} style={{ background: '#e2e8f0', padding: '0.4rem 0.8rem', borderRadius: 6 }}>Fermer</button>
          </div>
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>NumeroTR</th>
                  <th>DateTR</th>
                  <th>Creatinine M3</th>
                  <th>DFG M3</th>
                  <th>Rejet aigu</th>
                  <th>Statut final</th>
                </tr>
              </thead>
              <tbody>
                {historique.map((item, index) => (
                  <tr key={item.numeroTr || item.id || index}>
                    <td>#{item.numeroTr || item.id || index + 1}</td>
                    <td>{formatDate(item.dateTr)}</td>
                    <td>{item.creatinineM3 ? `${item.creatinineM3} umol/L` : ''}</td>
                    <td>{item.dfgMdrdM3 ? `${item.dfgMdrdM3} mL/min` : ''}</td>
                    <td>{item.rejetAigu1ereAnnee ? 'Oui' : 'Non'}</td>
                    <td>{item.retourDialyse ? 'Dialyse' : item.vivantGreffon ? 'Greffon OK' : item.dcAvecGreffon ? 'Deces' : item.pdv ? 'Perdu de vue' : ''}</td>
                  </tr>
                ))}
                {historique.length === 0 && (
                  <tr><td colSpan="6" style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>Aucune transplantation pour ce patient.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showMedicaments && selectedPatientId && (
        <div style={{ marginTop: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '0.5rem' }}>
            <button type="button" onClick={() => setShowMedicaments(false)} style={{ background: '#e2e8f0', padding: '0.4rem 0.8rem', borderRadius: 6 }}>Fermer</button>
          </div>
          <GestionMedicaments patientId={selectedPatientId} />
        </div>
      )}
    </div>
  );
}
