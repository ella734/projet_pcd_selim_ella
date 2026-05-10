import { useState, useEffect } from 'react';
import api from '../services/api';

/**
 * Composant GestionMedicaments
 * Permet au médecin d'ajouter/supprimer des médicaments
 * et de déclarer des interactions pour un patient dans Neo4j.
 *
 * Usage : <GestionMedicaments patientId={5} />
 */
export default function GestionMedicaments({ patientId }) {
  const [medicaments, setMedicaments]   = useState([]);
  const [interactions, setInteractions] = useState([]);
  const [newMed, setNewMed]             = useState('');
  const [med1, setMed1]                 = useState('');
  const [med2, setMed2]                 = useState('');
  const [message, setMessage]           = useState(null);
  const [loading, setLoading]           = useState(false);

  useEffect(() => {
    if (patientId) {
      chargerDonnees();
    }
  }, [patientId]);

  // Affiche un message temporaire 3 secondes
  const afficherMessage = (texte, type = 'success') => {
    setMessage({ texte, type });
    setTimeout(() => setMessage(null), 3000);
  };

  // Charge les médicaments et interactions depuis Neo4j
  const chargerDonnees = async () => {
    setLoading(true);
    try {
      const [medRes, intRes] = await Promise.all([
        api.get(`/alertes/medicaments/${patientId}`),
        api.get(`/alertes/interactions/${patientId}`)
      ]);
      setMedicaments(Array.isArray(medRes.data) ? medRes.data : []);
      setInteractions(Array.isArray(intRes.data) ? intRes.data : []);
    } catch (e) {
      afficherMessage('Erreur lors du chargement des données Neo4j', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Ajoute un médicament au patient
  const ajouterMedicament = async () => {
    if (!newMed.trim()) return;
    try {
      await api.post('/alertes/medicament', {
        patientId: String(patientId),
        medicament: newMed.trim()
      });
      afficherMessage(`✅ ${newMed.trim()} ajouté au patient`);
      setNewMed('');
      chargerDonnees();
    } catch (e) {
      afficherMessage('❌ Erreur lors de l\'ajout du médicament', 'error');
    }
  };

  // Supprime un médicament du patient
  const supprimerMedicament = async (med) => {
    try {
      await api.delete('/alertes/medicament', {
        data: { patientId: String(patientId), medicament: med }
      });
      afficherMessage(`🗑️ ${med} supprimé`);
      chargerDonnees();
    } catch (e) {
      afficherMessage('❌ Erreur lors de la suppression', 'error');
    }
  };

  // Déclare une interaction entre deux médicaments
  const ajouterInteraction = async () => {
    if (!med1.trim() || !med2.trim()) return;
    if (med1.trim().toLowerCase() === med2.trim().toLowerCase()) {
      afficherMessage('❌ Les deux médicaments doivent être différents', 'error');
      return;
    }
    try {
      await api.post('/alertes/interactions', {
        patientId: String(patientId),
        medicament1: med1.trim(),
        medicament2: med2.trim()
      });
      afficherMessage(`✅ Interaction ${med1.trim()} ↔ ${med2.trim()} enregistrée`);
      setMed1('');
      setMed2('');
      chargerDonnees();
    } catch (e) {
      afficherMessage('❌ Erreur lors de l\'enregistrement de l\'interaction', 'error');
    }
  };

  const styles = {
    container: {
      padding: '1.5rem',
      border: '1px solid #e2e8f0',
      borderRadius: '12px',
      background: '#FAFBFC',
      marginTop: '1rem'
    },
    title: {
      color: '#1B4F8A',
      fontSize: '1.1rem',
      fontWeight: 'bold',
      marginBottom: '1rem',
      borderBottom: '2px solid #BDD7EE',
      paddingBottom: '0.5rem'
    },
    section: {
      marginBottom: '1.5rem'
    },
    sectionTitle: {
      color: '#2E75B6',
      fontSize: '0.95rem',
      fontWeight: 'bold',
      marginBottom: '0.5rem'
    },
    input: {
      flex: 1,
      padding: '0.5rem 0.75rem',
      borderRadius: '6px',
      border: '1px solid #CBD5E1',
      fontSize: '0.9rem',
      outline: 'none'
    },
    btnBlue: {
      background: '#2E75B6',
      color: 'white',
      border: 'none',
      padding: '0.5rem 1rem',
      borderRadius: '6px',
      cursor: 'pointer',
      fontSize: '0.9rem',
      whiteSpace: 'nowrap'
    },
    btnRed: {
      background: '#C0392B',
      color: 'white',
      border: 'none',
      padding: '0.5rem 1rem',
      borderRadius: '6px',
      cursor: 'pointer',
      fontSize: '0.9rem',
      whiteSpace: 'nowrap'
    },
    tag: {
      display: 'inline-flex',
      alignItems: 'center',
      gap: '0.4rem',
      background: '#EFF6FF',
      color: '#1B4F8A',
      padding: '0.25rem 0.75rem',
      borderRadius: '20px',
      fontSize: '0.85rem',
      border: '1px solid #BDD7EE',
      margin: '0.25rem'
    },
    tagDelete: {
      cursor: 'pointer',
      color: '#C0392B',
      fontWeight: 'bold',
      fontSize: '1rem',
      lineHeight: 1
    },
    alertBox: {
      padding: '0.6rem 1rem',
      background: '#FFF5F5',
      border: '1px solid #FADBD8',
      borderLeft: '4px solid #C0392B',
      borderRadius: '6px',
      marginBottom: '0.4rem',
      fontSize: '0.9rem'
    },
    successBox: {
      padding: '0.6rem 1rem',
      background: '#F0FFF4',
      border: '1px solid #C6F6D5',
      borderLeft: '4px solid #27AE60',
      borderRadius: '6px',
      marginBottom: '0.4rem',
      fontSize: '0.9rem'
    },
    messageBox: (type) => ({
      padding: '0.75rem 1rem',
      borderRadius: '8px',
      marginBottom: '1rem',
      background: type === 'error' ? '#FFF5F5' : '#F0FFF4',
      border: `1px solid ${type === 'error' ? '#FADBD8' : '#C6F6D5'}`,
      color: type === 'error' ? '#C0392B' : '#27AE60',
      fontSize: '0.9rem'
    })
  };

  if (!patientId) {
    return (
      <div style={styles.container}>
        <p style={{ color: '#888', fontSize: '0.9rem' }}>
          Sélectionnez un patient pour gérer ses médicaments.
        </p>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.title}>
        💊 Médicaments et interactions — Patient #{patientId}
      </div>

      {/* Message de retour */}
      {message && (
        <div style={styles.messageBox(message.type)}>
          {message.texte}
        </div>
      )}

      {loading && (
        <p style={{ color: '#888', fontSize: '0.9rem' }}>Chargement...</p>
      )}

      {/* ── Section 1 : Ajouter un médicament ── */}
      <div style={styles.section}>
        <div style={styles.sectionTitle}>Ajouter un médicament</div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            value={newMed}
            onChange={e => setNewMed(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && ajouterMedicament()}
            placeholder="Ex: Tacrolimus, MMF, Ciclosporine..."
            style={styles.input}
          />
          <button onClick={ajouterMedicament} style={styles.btnBlue}>
            + Ajouter
          </button>
        </div>

        {/* Liste des médicaments actuels */}
        {medicaments.length > 0 && (
          <div style={{ marginTop: '0.75rem' }}>
            <span style={{ fontSize: '0.8rem', color: '#555' }}>
              Médicaments actuels :
            </span>
            <div style={{ marginTop: '0.3rem' }}>
              {medicaments.map((m, i) => (
                <span key={i} style={styles.tag}>
                  💊 {m}
                  <span
                    style={styles.tagDelete}
                    onClick={() => supprimerMedicament(m)}
                    title="Supprimer"
                  >
                    ×
                  </span>
                </span>
              ))}
            </div>
          </div>
        )}

        {medicaments.length === 0 && !loading && (
          <p style={{ color: '#888', fontSize: '0.85rem', marginTop: '0.5rem' }}>
            Aucun médicament enregistré pour ce patient.
          </p>
        )}
      </div>

      {/* ── Section 2 : Déclarer une interaction ── */}
      <div style={styles.section}>
        <div style={{ ...styles.sectionTitle, color: '#C0392B' }}>
          Déclarer une interaction médicamenteuse
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <input
            value={med1}
            onChange={e => setMed1(e.target.value)}
            placeholder="Médicament 1"
            style={styles.input}
          />
          <span style={{ color: '#C0392B', fontWeight: 'bold', fontSize: '1.2rem' }}>
            ↔
          </span>
          <input
            value={med2}
            onChange={e => setMed2(e.target.value)}
            placeholder="Médicament 2"
            style={styles.input}
          />
          <button onClick={ajouterInteraction} style={styles.btnRed}>
            Enregistrer
          </button>
        </div>
        <p style={{ color: '#888', fontSize: '0.8rem', marginTop: '0.3rem' }}>
          Les deux médicaments seront automatiquement ajoutés au patient s'ils n'existent pas encore.
        </p>
      </div>

      {/* ── Section 3 : Interactions détectées ── */}
      <div style={styles.section}>
        <div style={{ ...styles.sectionTitle, color: '#C0392B' }}>
          Interactions détectées par Neo4j
        </div>

        {interactions.length > 0 ? (
          interactions.map((inter, i) => (
            <div key={i} style={styles.alertBox}>
              ⚠️ <strong>{inter.medicament1}</strong>
              {' '}interagit avec{' '}
              <strong>{inter.medicament2}</strong>
            </div>
          ))
        ) : (
          <div style={styles.successBox}>
            ✅ Aucune interaction médicamenteuse détectée pour ce patient.
          </div>
        )}
      </div>
    </div>
  );
}
