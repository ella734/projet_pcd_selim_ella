import { useState, useEffect } from "react";
import api from "../services/api";

const NIVEAU_CONFIG = {
  "1A": { color: "#1B4F8A", bg: "#EFF6FF", label: "1A — Forte recommandation, haute qualité de preuve" },
  "1B": { color: "#2E75B6", bg: "#DBEAFE", label: "1B — Forte recommandation, qualité modérée" },
  "1C": { color: "#3498db", bg: "#E0F2FE", label: "1C — Forte recommandation, faible qualité" },
  "2B": { color: "#e67e22", bg: "#FFF7ED", label: "2B — Recommandation conditionnelle, qualité modérée" },
  "2C": { color: "#e67e22", bg: "#FEF3C7", label: "2C — Recommandation conditionnelle, faible qualité" },
};

const TYPE_ICON = {
  SURVEILLANCE: "🔬",
  BIOLOGIE: "🧪",
  THERAPEUTIQUE: "💊",
};

export default function RecommandationsKdigo({ patientId }) {
  const [recommandations, setRecommandations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!patientId) return;
    setLoading(true);
    setError(null);

    api.get(`/graphe/recommandations/${patientId}`)
      .then(({ data }) => setRecommandations(Array.isArray(data) ? data : []))
      .catch(() => setError("Recommandations non disponibles (Neo4j)."))
      .finally(() => setLoading(false));
  }, [patientId]);

  // Grouper par comorbidité
  const grouped = recommandations.reduce((acc, r) => {
    const key = r.comorbidite || "Général";
    if (!acc[key]) acc[key] = [];
    acc[key].push(r);
    return acc;
  }, {});

  const styles = {
    container: { padding: "1rem 0" },
    header: {
      display: "flex",
      alignItems: "center",
      gap: "0.5rem",
      marginBottom: "1rem",
      paddingBottom: "0.5rem",
      borderBottom: "2px solid #BDD7EE",
    },
    title: { fontSize: "1rem", fontWeight: "bold", color: "#1B4F8A", margin: 0 },
    badge: {
      background: "#1B4F8A",
      color: "white",
      borderRadius: "12px",
      padding: "0.1rem 0.5rem",
      fontSize: "0.75rem",
      fontWeight: "bold",
    },
    groupTitle: {
      fontSize: "0.9rem",
      fontWeight: "bold",
      color: "#e67e22",
      margin: "1rem 0 0.5rem",
      display: "flex",
      alignItems: "center",
      gap: "0.4rem",
    },
    card: (niveau) => ({
      background: NIVEAU_CONFIG[niveau]?.bg || "#F8F9FA",
      border: `1px solid ${NIVEAU_CONFIG[niveau]?.color || "#ccc"}33`,
      borderLeft: `4px solid ${NIVEAU_CONFIG[niveau]?.color || "#888"}`,
      borderRadius: "8px",
      padding: "0.85rem 1rem",
      marginBottom: "0.6rem",
    }),
    condition: {
      fontSize: "0.8rem",
      fontWeight: "bold",
      color: "#333",
      marginBottom: "0.35rem",
    },
    conseil: {
      fontSize: "0.85rem",
      color: "#444",
      lineHeight: 1.5,
      marginBottom: "0.4rem",
    },
    footer: {
      display: "flex",
      alignItems: "center",
      gap: "0.5rem",
      flexWrap: "wrap",
    },
    niveauBadge: (niveau) => ({
      background: NIVEAU_CONFIG[niveau]?.color || "#888",
      color: "white",
      borderRadius: "4px",
      padding: "0.1rem 0.45rem",
      fontSize: "0.72rem",
      fontWeight: "bold",
    }),
    sourceBadge: {
      background: "#e2e8f0",
      color: "#555",
      borderRadius: "4px",
      padding: "0.1rem 0.45rem",
      fontSize: "0.72rem",
    },
    typeBadge: {
      background: "#f0f0f0",
      color: "#555",
      borderRadius: "4px",
      padding: "0.1rem 0.45rem",
      fontSize: "0.72rem",
    },
    empty: {
      padding: "1rem",
      background: "#F0FFF4",
      border: "1px solid #C6F6D5",
      borderRadius: "8px",
      color: "#27AE60",
      fontSize: "0.9rem",
      textAlign: "center",
    },
    errorBox: {
      padding: "0.75rem 1rem",
      background: "#FFF8E1",
      border: "1px solid #FFE082",
      borderRadius: "8px",
      color: "#795548",
      fontSize: "0.85rem",
    },
  };

  if (loading) return <p style={{ color: "#888", padding: "0.5rem 0" }}>Chargement des recommandations…</p>;

  if (error) return <div style={styles.errorBox}>{error}</div>;

  if (recommandations.length === 0) return (
    <div style={styles.empty}>
      ✅ Aucune recommandation spécifique — le patient n'a pas de comorbidités enregistrées dans le graphe.
      <br />
      <small style={{ opacity: 0.8 }}>Synchronisez via Admin → Sync pour charger les données Neo4j.</small>
    </div>
  );

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <span style={styles.title}>Recommandations KDIGO 2022</span>
        <span style={styles.badge}>{recommandations.length}</span>
        <span style={{ fontSize: "0.75rem", color: "#888", marginLeft: "auto" }}>
          Basées sur les comorbidités du patient · Graphe de connaissances Neo4j
        </span>
      </div>

      {Object.entries(grouped).map(([comorbidite, recs]) => (
        <div key={comorbidite}>
          <div style={styles.groupTitle}>
            🏷 {comorbidite}
          </div>
          {recs.map((r) => (
            <div key={r.id} style={styles.card(r.niveau)}>
              <div style={styles.condition}>
                {TYPE_ICON[r.type] || "📋"} {r.condition}
              </div>
              <div style={styles.conseil}>{r.conseil}</div>
              <div style={styles.footer}>
                <span style={styles.niveauBadge(r.niveau)}>
                  Niveau {r.niveau}
                </span>
                <span style={styles.sourceBadge}>{r.source}</span>
                {r.type && <span style={styles.typeBadge}>{r.type}</span>}
                {NIVEAU_CONFIG[r.niveau] && (
                  <span style={{ fontSize: "0.7rem", color: "#888", fontStyle: "italic" }}>
                    {NIVEAU_CONFIG[r.niveau].label}
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
