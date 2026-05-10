import { useEffect, useMemo, useState } from 'react';
import { Plus, RefreshCw, Save, Trash2 } from 'lucide-react';
import api from '../services/api';

const RESOURCES = [
  { key: 'donneurs', title: 'Donneurs', endpoint: '/medical/donneurs', idField: 'identifiantP2', fields: ['nomP', 'prenomP', 'sexeP', 'telephoneP', 'typeDonneur', 'statut'] },
  { key: 'greffes', title: 'Greffes', endpoint: '/medical/greffes', idField: 'identifiantG', fields: ['dateG', 'descriptionG', 'autresObservationsG'] },
  { key: 'nephropathies', title: 'Nephropathies', endpoint: '/medical/nephropathies', idField: 'identifiantNi', fields: ['typeCliniqueNi', 'causeNi', 'typeHistologiqueNi', 'stadeMaladieNi'] },
  { key: 'dialyses', title: 'Dialyses', endpoint: '/medical/dialyses', idField: 'identifiantD', fields: ['typeDialyse'] },
  { key: 'comorbidites', title: 'Comorbidites', endpoint: '/medical/comorbidites', idField: 'identifiantC', fields: ['diabete', 'cardiaque'] },
  { key: 'traitements', title: 'Traitements', endpoint: '/medical/traitements', idField: 'identifiantTis', fields: ['patient.identifiantP', 'dciTis', 'dureeTraitementTis'] },
  { key: 'medicaments', title: 'Medicaments', endpoint: '/medical/medicaments', idField: 'identifiantMed', fields: ['nomCommercialMed', 'typeMed', 'posologieMed', 'descriptionMed'] },
  { key: 'effets-secondaires', title: 'Effets secondaires', endpoint: '/medical/effets-secondaires', idField: 'identifiantEfs', fields: ['libelleEfs', 'descriptionEfs', 'recommendationEfs'] },
  { key: 'hospitalisations', title: 'Hospitalisations post-TR', endpoint: '/medical/hospitalisations', idField: 'numeroTr', fields: ['numeroTr', 'dateEntree', 'dateSortie', 'typeHospitalisation', 'rejetAiguCellulaire'] },
  { key: 'evolutions-renales', title: 'Evolutions renales', endpoint: '/medical/evolutions-renales', idField: 'dateCreation', fields: ['dateCreation', 'numeroTr', 'seanceDeDialyse', 'dateSeance', 'repriseFonctionGreffon'] },
];

const setNestedValue = (target, path, value) => {
  const parts = path.split('.');
  let cursor = target;
  for (let i = 0; i < parts.length - 1; i += 1) {
    cursor[parts[i]] = cursor[parts[i]] || {};
    cursor = cursor[parts[i]];
  }
  cursor[parts.at(-1)] = value === 'true' ? true : value === 'false' ? false : value;
};

const getNestedValue = (row, path) => path.split('.').reduce((acc, part) => acc?.[part], row);

export default function MedicalData() {
  const [activeKey, setActiveKey] = useState(RESOURCES[0].key);
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(false);
  const resource = useMemo(() => RESOURCES.find((item) => item.key === activeKey) || RESOURCES[0], [activeKey]);

  const loadRows = async () => {
    setLoading(true);
    try {
      const { data } = await api.get(resource.endpoint);
      setRows(Array.isArray(data) ? data : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setForm({});
    loadRows();
  }, [resource.endpoint]);

  const save = async (event) => {
    event.preventDefault();
    await api.post(resource.endpoint, form);
    setForm({});
    await loadRows();
  };

  const remove = async (row) => {
    const id = row?.[resource.idField];
    if (!id) return;
    await api.delete(`${resource.endpoint}/${id}`);
    await loadRows();
  };

  const inputStyle = { padding: '0.55rem', border: '1px solid var(--border-color)', borderRadius: 6, width: '100%' };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.5rem' }}>Donnees medicales</h1>
        <button onClick={loadRows} className="btn-secondary" style={{ width: 'auto', display: 'flex', gap: 8, alignItems: 'center' }}>
          <RefreshCw size={16} /> Actualiser
        </button>
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: '1rem' }}>
        {RESOURCES.map((item) => (
          <button key={item.key} onClick={() => setActiveKey(item.key)} style={{ border: '1px solid var(--border-color)', background: item.key === activeKey ? 'var(--primary-color)' : 'white', color: item.key === activeKey ? 'white' : 'var(--text-primary)', padding: '0.5rem 0.75rem', borderRadius: 6, cursor: 'pointer' }}>
            {item.title}
          </button>
        ))}
      </div>

      <form onSubmit={save} style={{ border: '1px solid var(--border-color)', borderRadius: 8, padding: '1rem', marginBottom: '1.5rem', background: '#f8fafc' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: '1rem', fontWeight: 700 }}>
          <Plus size={18} /> Ajouter {resource.title.toLowerCase()}
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.75rem' }}>
          {resource.fields.map((field) => (
            <label key={field} style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600 }}>
              {field}
              <input
                style={{ ...inputStyle, marginTop: 4 }}
                value={getNestedValue(form, field) ?? ''}
                onChange={(event) => setForm((previous) => {
                  const next = { ...previous };
                  setNestedValue(next, field, event.target.value);
                  return next;
                })}
              />
            </label>
          ))}
        </div>
        <button className="btn-primary" style={{ width: 'auto', marginTop: '1rem', display: 'flex', gap: 8, alignItems: 'center' }}>
          <Save size={16} /> Enregistrer
        </button>
      </form>

      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
          <thead>
            <tr style={{ background: '#1B4F8A', color: 'white' }}>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>{resource.idField}</th>
              {resource.fields.map((field) => <th key={field} style={{ padding: '0.75rem', textAlign: 'left' }}>{field}</th>)}
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={resource.fields.length + 2} style={{ padding: '1rem' }}>Chargement...</td></tr>
            ) : rows.map((row, index) => (
              <tr key={`${resource.key}-${row?.[resource.idField] ?? index}`} style={{ background: index % 2 === 0 ? '#F2F7FF' : 'white', borderBottom: '1px solid #e2e8f0' }}>
                <td style={{ padding: '0.75rem' }}>{row?.[resource.idField]}</td>
                {resource.fields.map((field) => <td key={field} style={{ padding: '0.75rem' }}>{String(getNestedValue(row, field) ?? '')}</td>)}
                <td style={{ padding: '0.75rem' }}>
                  <button onClick={() => remove(row)} style={{ border: 'none', background: '#dc2626', color: 'white', borderRadius: 6, padding: '0.35rem 0.55rem', cursor: 'pointer' }}>
                    <Trash2 size={15} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
